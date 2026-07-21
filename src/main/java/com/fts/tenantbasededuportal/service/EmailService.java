package com.fts.tenantbasededuportal.service;

import com.fts.tenantbasededuportal.exception.EmailDeliveryException;
import com.fts.tenantbasededuportal.util.constants.ApplicationConstants;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final EmailTemplateService emailTemplateService;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private void sendHtmlMail(final String toEmail, final String subject, final String htmlContent) {

        try {
            final MimeMessage message = this.mailSender.createMimeMessage();
            final MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(this.fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            this.mailSender.send(message);

        } catch (final Exception exception) {
            throw new EmailDeliveryException("Failed to send email.", exception);
        }
    }

    //method to send the generated otp to the given toEmail.
    public void sendOtpMail(final String toEmail, final String otp) {

        final String html = this.emailTemplateService.buildOtpTemplate(otp, ApplicationConstants.OTP_EXPIRY_MINUTES);
        this.sendHtmlMail(toEmail, "Your MFA Verification Code", html);
    }

    public void sendActivationMail(final String toEmail, final String activationLink){

        final String html = this.emailTemplateService.buildActivationTemplate(activationLink,
                ApplicationConstants.ACTIVATION_LINK_EXPIRY_HOURS);
        this.sendHtmlMail(toEmail, "Activate Your Account", html);
    }

    public void sendForgotPasswordMail(final String toEmail, final String resetLink){

        final String html = this.emailTemplateService.buildForgotPasswordTemplate(resetLink,
                ApplicationConstants.RESET_PASSWORD_EXPIRY_MINUTES);
        this.sendHtmlMail(toEmail, "Reset Your Password", html);
    }

    public void sendSuperAdminCredentialsMail(final String email, final String temporaryPassword, final String resetLink){

        final String html = this.emailTemplateService.buildSuperAdminTemplate(email, temporaryPassword, resetLink,
                                ApplicationConstants.ACTIVATION_LINK_EXPIRY_HOURS);
        this.sendHtmlMail(email, "Super Administrator Credentials", html);
    }
}
