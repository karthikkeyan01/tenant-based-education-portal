package com.fts.tenantbasededuportal.service;

import com.fts.tenantbasededuportal.exception.BadRequestException;
import com.fts.tenantbasededuportal.util.constants.ApplicationConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    //method to send the generated otp to the given toEmail.
    public void sendOtpMail(final String toEmail, final String otp) {

        try{

            final SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(this.fromEmail);

            message.setTo(toEmail);

            message.setSubject("Your MFA Verification Code");

            message.setText("Your Verification Code is: " + otp
            + "\n\nThis code expires in "
            +ApplicationConstants.OTP_EXPIRY_MINUTES
            + " minutes.");

            this.mailSender.send(message);
        }
        catch (Exception e){

            throw new BadRequestException("Failed to send verification email.");
        }
    }

    public void sendActivationMail(final String toEmail,
                                   final String activationLink){

        try{
            final SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(this.fromEmail);

            message.setTo(toEmail);

            message.setSubject("Activate your Account");

            message.setText("Welcome!!\n\n"
            + "Please activate your account using the link below:\n\n"
            + activationLink
            +"\n\nThis link expires in "
            +ApplicationConstants.ACTIVATION_LINK_EXPIRY_HOURS
            + " hours.");

            this.mailSender.send(message);
        }
        catch (final Exception exception){

            throw new BadRequestException("Failed to send activation email.");
        }
    }

    public void sendForgotPasswordMail
            (final String toEmail, final String resetLink){

        try{

            final SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(this.fromEmail);

            message.setTo(toEmail);

            message.setSubject("Reset Your Password");

            message.setText("You requested to reset your password.\n\n"
            + "Use the link below:\n\n"
            + resetLink
            + "\n\nThis link expires in "
            + ApplicationConstants.RESET_PASSWORD_EXPIRY_MINUTES
            + " minutes.");

            this.mailSender.send(message);
        }
        catch (final Exception exception){

            throw new BadRequestException("Failed to send forgot password email.");
        }
    }

    public void sendSuperAdminCredentialsMail(
            final String email, final String temporaryPassword, final String resetLink){

        try{
            final SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(this.fromEmail);

            message.setTo(email);

            message.setSubject("Super Admin Credentials And Reset Password Link");

            message.setText("Your Super Administrator account has been created.\n\n"
            + "Username: " + email + "\n"
            + "Password: " + temporaryPassword
            + "\n\nWe recommend you to reset your account password using the link below:\n\n"
            + resetLink
            + "\n\nThis link expires in "
            + ApplicationConstants.RESET_PASSWORD_EXPIRY_MINUTES + " minutes");

            this.mailSender.send(message);
        }
        catch (final Exception exception){

            throw new BadRequestException("Failed to send SuperAdmin Credentials email.");
        }
    }
}
