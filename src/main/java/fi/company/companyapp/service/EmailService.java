package fi.company.companyapp.service;

import fi.company.companyapp.entity.AppUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;

    public void notifyAdminNewUser(AppUser user) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo("admin@company.fi");
            message.setSubject("New user registered: " + user.getUsername());
            message.setText(String.format(
                "A new user has registered:\nUsername: %s\nEmail: %s\nName: %s %s",
                user.getUsername(), user.getEmail(), user.getFirstName(), user.getLastName()
            ));
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Failed to send admin notification email: {}", e.getMessage());
        }
    }

    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Password Reset Request");
            message.setText(String.format(
                "Your password reset token is: %s\n\nIf you did not request this, please ignore this email.",
                resetToken
            ));
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Failed to send password reset email: {}", e.getMessage());
        }
    }
}
