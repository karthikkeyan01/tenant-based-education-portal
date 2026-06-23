package com.fts.tenantbasededuportal.service;

import com.fts.tenantbasededuportal.exception.BadRequestException;
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

            message.setFrom(fromEmail);

            message.setTo(toEmail);

            message.setSubject("Your MFA Verification Code");

            message.setText("Your Verification Code is: " + otp
            + "\n\nThis code expires in 5 minutes.");

            this.mailSender.send(message);
        }
        catch (Exception e){

            throw new BadRequestException("Failed to send verification email.");
        }
    }
}
