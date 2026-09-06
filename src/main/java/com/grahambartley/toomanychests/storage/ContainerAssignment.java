package com.grahambartley.toomanychests.storage;

/**
 * Everything one network has decided about one container: the role it gave it, and for a {@code
 * FILTERED} container the filter that role needs to mean anything. See ADR 0004.
 *
 * <p>A role that cannot use a filter never carries one. Demoting a filtered container to general or
 * excluded therefore drops what it accepted rather than leaving it stored where nothing reads it,
 * which is what keeps two containers with the same effective configuration comparing equal.
 *
 * @param role what the network may do to this container
 * @param filter what a filtered container accepts, always empty for every other role
 */
public record ContainerAssignment(ContainerRole role, ContainerFilter filter) {
  public static final ContainerAssignment DEFAULT =
      new ContainerAssignment(ContainerRole.DEFAULT, ContainerFilter.NONE);

  public ContainerAssignment {
    role = role == null ? ContainerRole.DEFAULT : role;
    filter = filter == null || role != ContainerRole.FILTERED ? ContainerFilter.NONE : filter;
  }

  public static ContainerAssignment of(ContainerRole role) {
    return role == null || role == ContainerRole.DEFAULT
        ? DEFAULT
        : new ContainerAssignment(role, ContainerFilter.NONE);
  }

  public static ContainerAssignment filtered(ContainerFilter filter) {
    return new ContainerAssignment(ContainerRole.FILTERED, filter);
  }

  /** A container the network has been told nothing about, which is what a new one starts as. */
  public boolean isDefault() {
    return DEFAULT.equals(this);
  }

  public ContainerAssignment withRole(ContainerRole newRole) {
    return newRole == role ? this : new ContainerAssignment(newRole, filter);
  }

  public ContainerAssignment withFilter(ContainerFilter newFilter) {
    return new ContainerAssignment(role, newFilter);
  }
}
