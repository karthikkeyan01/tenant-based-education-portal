package com.fts.tenantbasededuportal.util.constants;

public final class AuditActionConstants {

    private AuditActionConstants() {}

    public static final String REGISTER = "REGISTER";

    public static final String LOGIN = "LOGIN";
    public static final String LOGOUT = "LOGOUT";

    public static final String RESEND_OTP = "RESEND_OTP";

    public static final String ACTIVATE_ACCOUNT = "ACTIVATE_ACCOUNT";
    public static final String RESEND_ACTIVATION_EMAIL = "RESEND_ACTIVATION_EMAIL";

    public static final String RESET_PASSWORD = "RESET_PASSWORD";
    public static final String FORGOT_PASSWORD = "FORGOT_PASSWORD";

    public static final String UPDATE_PROFILE = "UPDATE_PROFILE";
    public static final String CHANGE_PASSWORD = "CHANGE_PASSWORD";

    public static final String CREATE_USER = "CREATE_USER";
    public static final String ACTIVATE_USER = "ACTIVATE_USER";
    public static final String DEACTIVATE_USER = "DEACTIVATE_USER";
    public static final String BULK_UPLOAD_USERS = "BULK_UPLOAD_USERS";

    public static final String CREATE_ORGANIZATION = "CREATE_ORGANIZATION";
    public static final String UPDATE_ORGANIZATION = "UPDATE_ORGANIZATION";
    public static final String ACTIVATE_ORGANIZATION = "ACTIVATE_ORGANIZATION";
    public static final String DEACTIVATE_ORGANIZATION = "DEACTIVATE_ORGANIZATION";
}
