package com.grahambartley.totalstorage.storage;

public record ContainerAssignment(ContainerRole role, ContainerFilter filter, String name) {
  public static final ContainerAssignment DEFAULT =
      new ContainerAssignment(ContainerRole.DEFAULT, ContainerFilter.NONE, "");

  public ContainerAssignment {
    role = role == null ? ContainerRole.DEFAULT : role;
    filter = filter == null || role != ContainerRole.FILTERED ? ContainerFilter.NONE : filter;
    name = ContainerNameRule.sanitise(name);
  }

  public ContainerAssignment(ContainerRole role, ContainerFilter filter) {
    this(role, filter, "");
  }

  public static ContainerAssignment of(ContainerRole role) {
    return role == null || role == ContainerRole.DEFAULT
        ? DEFAULT
        : new ContainerAssignment(role, ContainerFilter.NONE, "");
  }

  public static ContainerAssignment filtered(ContainerFilter filter) {
    return new ContainerAssignment(ContainerRole.FILTERED, filter, "");
  }

  public boolean isDefault() {
    return DEFAULT.equals(this);
  }

  public boolean isNamed() {
    return !name.isEmpty();
  }

  public ContainerAssignment withRole(ContainerRole newRole) {
    return newRole == role ? this : new ContainerAssignment(newRole, filter, name);
  }

  public ContainerAssignment withFilter(ContainerFilter newFilter) {
    return new ContainerAssignment(role, newFilter, name);
  }

  public ContainerAssignment withName(String newName) {
    final ContainerAssignment renamed = new ContainerAssignment(role, filter, newName);
    return renamed.equals(this) ? this : renamed;
  }
}
