package com.grahambartley.toomanychests.storage;

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
