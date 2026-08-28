package com.grahambartley.toomanychests;

import com.grahambartley.toomanychests.network.ModNetworkingClient;
import net.fabricmc.api.ClientModInitializer;

public class TooManyChestsClient implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    ModNetworkingClient.register();
  }
}
