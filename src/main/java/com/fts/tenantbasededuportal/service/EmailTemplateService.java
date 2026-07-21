package com.fts.tenantbasededuportal.service;

import org.springframework.stereotype.Service;

@Service
public class EmailTemplateService {

    private String buildTemplate(final String title, final String greeting, final String message,
            final String buttonText, final String buttonLink, final String footer) {

        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>

            <body style="margin:0;padding:40px;background:#f4f6f9;font-family:Arial,Helvetica,sans-serif;">

                <table align="center"
                       cellpadding="0"
                       cellspacing="0"
                       width="650"
                       style="background:#ffffff;border-radius:10px;overflow:hidden;border:1px solid #e5e7eb;">

                    <tr>
                        <td align="center"
                            style="background:#2563eb;color:white;padding:25px;font-size:30px;font-weight:bold;letter-spacing:1px;">

                            Education Portal
                        </td>
                    </tr>

                    <tr>
                        <td style="padding:40px;color:#374151;font-size:16px;line-height:1.8;">

                            <h2 style="margin-top:0;margin-bottom:25px;color:#111827;text-align:center;font-size:28px;">

                                %s
                            </h2>

                            <p>%s</p>

                            <div style="margin-bottom:20px;">
                                    %s
                                </div>

                            <div style="text-align:center;margin:40px 0;">

                                <a href="%s"
                                   style="background:#2563eb;color:white;text-decoration:none;padding:15px 35px;border-radius:6px;font-weight:bold;display:inline-block;">

                                    %s </a>
                            </div>

                            <p>
                                If the button above doesn't work, copy and paste the link below into your browser.
                            </p>

                            <p>
                                <a href="%s">%s</a>
                            </p>

                            <hr style="margin:35px 0;border:none;border-top:1px solid #e5e7eb;">

                            <p style="color:#6b7280;font-size:14px;">
                                %s
                            </p>

                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(title, greeting, message, buttonLink, buttonText, buttonLink, buttonLink, footer);
    }

    public String buildActivationTemplate(final String activationLink, final int expiryHours) {

        return this.buildTemplate("Activate Your Account", "Hello!", "Your account has been created successfully.<br><br>"
                        + "This activation link expires in <strong>" + expiryHours + " hours</strong>.<br><br>" + "Click the button below to activate your account.",
                "Activate Account", activationLink, "If you did not request this account, you can safely ignore this email.");
    }

    public String buildForgotPasswordTemplate(final String resetLink, final int expiryMinutes) {

        return this.buildTemplate("Reset Your Password", "Hello!",
                "We received a request to reset your password. Click the button below to continue.",
                "Reset Password", resetLink, "This reset link expires in " + expiryMinutes + " minutes.");
    }

    public String buildSuperAdminTemplate(final String email, final String temporaryPassword, final String resetLink,
                                          final int expiryMinutes) {

        final String message = """
            Your Super Administrator account has been created.<br><br>

            <strong>Username:</strong> %s<br>
            <strong>Temporary Password:</strong> %s<br><br>

            For security reasons, we recommend changing your password immediately.
            """.formatted(email, temporaryPassword);

        return this.buildTemplate("Your Super Administrator Account", "Hello!", message, "Reset Password", resetLink,
                "This reset link expires in " + expiryMinutes + " minutes.");
    }

    public String buildOtpTemplate(final String otp, final int expiryMinutes) {

        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>

            <body style="margin:0;padding:40px;background:#f4f6f9;font-family:Arial,Helvetica,sans-serif;">

                <table align="center"
                       cellpadding="0"
                       cellspacing="0"
                       width="650"
                       style="background:#ffffff;border-radius:10px;overflow:hidden;border:1px solid #e5e7eb;">

                    <tr>
                        <td align="center"
                            style="background:#2563eb;color:white;padding:25px;font-size:30px;font-weight:bold;letter-spacing:1px;">

                            Education Portal

                        </td>
                    </tr>

                    <tr>
                        <td style="padding:40px;color:#374151;font-size:16px;line-height:1.8;">

                            <h2 style="margin-top:0;margin-bottom:25px;color:#111827;text-align:center;font-size:28px;">

                                Verify Your Identity

                            </h2>

                            <p>Hello!</p>

                            <p>
                                        Use the verification code below to complete your sign in.
                                    </p>
                                    <p style="
                                            text-align:center;
                                            font-size:16px;
                                            margin:25px 0;
                                            color:#dc2626;
                                            font-weight:bold;">
                                        This verification code expires in
                                        <strong>%d minutes</strong>.
                                    </p>
                                    <div style="
                                            margin:30px 0 40px;
                                            padding:20px;
                                            background:#eff6ff;
                                            border:2px dashed #2563eb;
                                            border-radius:8px;
                                            text-align:center;
                                            font-size:40px;
                                            font-weight:bold;
                                            letter-spacing:10px;
                                            color:#2563eb;">
                                        %s
                                    </div>
                                    <p style="text-align:center;color:#6b7280;margin-top:0;">
                                        For your security, do not share this code with anyone.
                                    </p>

                            <hr style="margin:35px 0;border:none;border-top:1px solid #e5e7eb;">

                            <p style="color:#6b7280;font-size:14px;text-align:center;">

                                If you did not request this verification code,
                                you can safely ignore this email.

                            </p>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(expiryMinutes, otp);
    }

}
