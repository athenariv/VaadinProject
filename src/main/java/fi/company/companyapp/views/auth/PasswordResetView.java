package fi.company.companyapp.views.auth;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;
import fi.company.companyapp.service.AppUserService;
import fi.company.companyapp.service.EmailService;

import java.util.UUID;

@Route("reset-password")
@PageTitle("Reset Password | CompanyApp")
@AnonymousAllowed
public class PasswordResetView extends VerticalLayout {

    public PasswordResetView(AppUserService appUserService, EmailService emailService) {

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        Div card = new Div();
        card.addClassNames(
            LumoUtility.Background.BASE,
            LumoUtility.BorderRadius.LARGE,
            LumoUtility.Padding.XLARGE,
            LumoUtility.BoxShadow.MEDIUM
        );
        card.getStyle().set("width", "400px");

        H2 title = new H2("Reset Password");
        title.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);

        // Step 1: request reset token
        EmailField emailField = new EmailField("Your Email Address");
        emailField.setWidthFull();
        emailField.setRequiredIndicatorVisible(true);

        Paragraph infoText = new Paragraph("Enter your email and we will send you a reset token.");
        infoText.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

        // Step 2: enter token and new password
        TextField tokenField = new TextField("Reset Token (from email)");
        tokenField.setWidthFull();
        tokenField.setVisible(false);

        PasswordField newPasswordField = new PasswordField("New Password");
        newPasswordField.setWidthFull();
        newPasswordField.setVisible(false);

        // Simulated token storage (in production, use DB-backed token)
        String[] sentToken = {null};
        String[] sentEmail = {null};

        Button requestBtn = new Button("Send Reset Token", e -> {
            String email = emailField.getValue();
            if (email == null || email.isBlank()) {
                Notification.show("Please enter your email").addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            if (!appUserService.findByEmail(email).isPresent()) {
                Notification.show("No account found with this email").addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            String token = UUID.randomUUID().toString();
            sentToken[0] = token;
            sentEmail[0] = email;
            emailService.sendPasswordResetEmail(email, token);
            Notification.show("Reset token sent to " + email).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            tokenField.setVisible(true);
            newPasswordField.setVisible(true);
        });
        requestBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        requestBtn.setWidthFull();

        Button resetBtn = new Button("Reset Password", e -> {
            if (sentToken[0] == null || !sentToken[0].equals(tokenField.getValue())) {
                Notification.show("Invalid or missing token").addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            String newPwd = newPasswordField.getValue();
            if (newPwd.length() < 6) {
                Notification.show("Password must be at least 6 characters").addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            appUserService.resetPasswordByEmail(sentEmail[0], newPwd);
            Notification.show("Password reset successfully! Please login.").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            getUI().ifPresent(ui -> ui.navigate("login"));
        });
        resetBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        resetBtn.setWidthFull();
        resetBtn.setVisible(false);

        tokenField.addValueChangeListener(e -> resetBtn.setVisible(!e.getValue().isBlank()));

        Anchor backToLogin = new Anchor("login", "Back to Login");
        backToLogin.addClassNames(LumoUtility.Margin.Top.MEDIUM, LumoUtility.FontSize.SMALL);

        card.add(title, infoText, emailField, requestBtn, tokenField, newPasswordField, resetBtn, backToLogin);
        add(card);
    }
}
