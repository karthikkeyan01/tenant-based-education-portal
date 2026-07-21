package com.fts.tenantbasededuportal.util.constants;

public final class SecurityConstants {

    private SecurityConstants() {}

    public static final String HAS_SUPER_ADMIN = "hasRole('" + RoleConstants.SUPER_ADMIN + "')";
    public static final String HAS_ORG_ADMIN = "hasRole('" + RoleConstants.ORG_ADMIN + "')";
    public static final String HAS_SUPER_OR_ORG_ADMIN = "hasAnyRole('" + RoleConstants.SUPER_ADMIN + "','" + RoleConstants.ORG_ADMIN + "')";
    public static final String IS_AUTHENTICATED = "isAuthenticated()";
}
