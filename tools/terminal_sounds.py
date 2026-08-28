#!/usr/bin/env python3
"""Synthesise the terminal block's sound set and write it as Vorbis OGG.

Usage:
  terminal_sounds.py <out-dir>

The terminal sits in a base and is heard constantly, so every cue here is short, soft and
mechanical rather than musical. Tones are sine based with fast exponential decays; there is no
sawtooth or square anywhere, because their upper harmonics are what make a repeated UI blip
fatiguing.

Every file is mono. Minecraft only pans and attenuates mono sounds by distance; a stereo file
plays flat at full volume wherever the player stands, which for a block cue is wrong.

All four are peak normalised to the same level, so relative loudness is decided once at the
call site by the volume passed to the play call rather than being baked unevenly into the assets.
"""
import os
import shutil
import subprocess
import sys
import wave

import numpy as np

RATE = 44100
PEAK = 0.5


def envelope(length, attack, release):
    """Attack and release ramps, cosine shaped so neither end starts with a click."""
    out = np.ones(length)
    attack_samples = max(1, int(attack * RATE))
    release_samples = max(1, int(release * RATE))
    out[:attack_samples] = 0.5 - 0.5 * np.cos(np.linspace(0, np.pi, attack_samples))
    out[length - release_samples:] = 0.5 + 0.5 * np.cos(np.linspace(0, np.pi, release_samples))
    return out


def tone(duration, start_hz, end_hz=None, decay=0.0, harmonic=0.0):
    length = int(duration * RATE)
    time = np.arange(length) / RATE
    sweep = np.linspace(start_hz, end_hz if end_hz else start_hz, length)
    phase = 2 * np.pi * np.cumsum(sweep) / RATE
    wave_out = np.sin(phase)
    if harmonic:
        wave_out += harmonic * np.sin(2 * phase)
    if decay:
        wave_out *= np.exp(-time / decay)
    return wave_out


def hiss(duration, decay, cutoff=0.25):
    """Filtered noise for mechanical body. A one pole low pass keeps it from reading as static."""
    length = int(duration * RATE)
    noise = np.random.default_rng(7).normal(0, 1, length)
    filtered = np.zeros(length)
    for index in range(1, length):
        filtered[index] = filtered[index - 1] + cutoff * (noise[index] - filtered[index - 1])
    return filtered * np.exp(-np.arange(length) / RATE / decay)


def pad(signal, duration):
    length = int(duration * RATE)
    out = np.zeros(length)
    out[: min(len(signal), length)] = signal[:length]
    return out


def power_on():
    duration = 0.9
    body = pad(tone(duration, 82, 138, decay=1.4), duration) * 0.9
    body += pad(tone(duration, 164, 276, decay=0.7), duration) * 0.25
    body += pad(hiss(duration, 0.35), duration) * 0.18
    return body * envelope(len(body), 0.18, 0.25)


def power_off():
    duration = 0.55
    body = pad(tone(duration, 138, 68, decay=0.28), duration) * 0.9
    body += pad(tone(duration, 276, 136, decay=0.16), duration) * 0.2
    body += pad(hiss(0.09, 0.05), duration) * 0.3
    return body * envelope(len(body), 0.006, 0.22)


def screen_open():
    duration = 0.26
    body = pad(tone(0.06, 620, decay=0.035, harmonic=0.15), duration)
    second = pad(tone(0.09, 930, decay=0.05, harmonic=0.12), duration)
    body += np.roll(second, int(0.07 * RATE))
    body += pad(hiss(0.03, 0.015), duration) * 0.22
    return body * envelope(len(body), 0.004, 0.05)


def deposit_confirm():
    duration = 0.18
    body = pad(tone(0.12, 784, decay=0.045), duration)
    body += np.roll(pad(tone(0.1, 1176, decay=0.035), duration), int(0.035 * RATE)) * 0.55
    return body * envelope(len(body), 0.004, 0.05)


SOUNDS = {
    "power_on": power_on,
    "power_off": power_off,
    "screen_open": screen_open,
    "deposit_confirm": deposit_confirm,
}


def encode(wav_path, ogg_path):
    """Vorbis via oggenc, falling back to ffmpeg. Not every ffmpeg build carries libvorbis."""
    if shutil.which("oggenc"):
        subprocess.run(["oggenc", "-Q", "-q", "5", "-o", ogg_path, wav_path], check=True)
        return
    subprocess.run(
        ["ffmpeg", "-y", "-loglevel", "error", "-i", wav_path, "-ac", "1", "-c:a", "libvorbis", "-q:a", "5", ogg_path],
        check=True,
    )


def write(name, samples, out_dir):
    samples = samples / np.max(np.abs(samples)) * PEAK
    pcm = (samples * 32767).astype("<i2")
    wav_path = os.path.join(out_dir, name + ".wav")
    ogg_path = os.path.join(out_dir, name + ".ogg")
    with wave.open(wav_path, "wb") as handle:
        handle.setnchannels(1)
        handle.setsampwidth(2)
        handle.setframerate(RATE)
        handle.writeframes(pcm.tobytes())
    encode(wav_path, ogg_path)
    os.remove(wav_path)
    print(f"wrote {ogg_path} ({len(samples) / RATE:.2f}s)")


def main():
    if len(sys.argv) != 2:
        raise SystemExit(__doc__)
    out_dir = sys.argv[1]
    os.makedirs(out_dir, exist_ok=True)
    for name, builder in SOUNDS.items():
        write(name, builder(), out_dir)


if __name__ == "__main__":
    main()
