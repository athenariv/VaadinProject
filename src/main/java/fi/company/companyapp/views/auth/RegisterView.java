package fi.company.companyapp.views.auth;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;
import fi.company.companyapp.service.AppUserService;

import java.util.Set;

@Route("register")
@PageTitle("Register | CompanyApp")
@AnonymousAllowed
public class RegisterView extends VerticalLayout {

    public RegisterView(AppUserService appUserService) {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        getStyle().set("background", "var(--lumo-contrast-5pct)");

        Div card = new Div();
        card.addClassNames(
            LumoUtility.Background.BASE,
            LumoUtility.BorderRadius.LARGE,
            LumoUtility.Padding.XLARGE,
            LumoUtility.BoxShadow.LARGE
        );
        card.getStyle().set("min-width", "400px").set("max-width", "500px");

        H2 title = new H2("Create Account");
        title.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);

        TextField usernameField = new TextField("Username");
        usernameField.setWidthFull();
        usernameField.setRequiredIndicatorVisible(true);
        usernameField.setMinLength(3);

        TextField firstNameField = new TextField("First Name");
        firstNameField.setWidthFull();
        firstNameField.setRequiredIndicatorVisible(true);

        TextField lastNameField = new TextField("Last Name");
        lastNameField.setWidthFull();
        lastNameField.setRequiredIndicatorVisible(true);

        EmailField emailField = new EmailField("Email");
        emailField.setWidthFull();
        emailField.setRequiredIndicatorVisible(true);

        PasswordField passwordField = new PasswordField("Password");
        passwordField.setWidthFull();
        passwordField.setRequiredIndicatorVisible(true);
        passwordField.setMinLength(6);

        PasswordField confirmPasswordField = new PasswordField("Confirm Password");
        confirmPasswordField.setWidthFull();
        confirmPasswordField.setRequiredIndicatorVisible(true);

        Button registerBtn = new Button("Register", e -> {
            String password = passwordField.getValue();
            String confirm = confirmPasswordField.getValue();

            if (!password.equals(confirm)) {
                Notification.show("Passwords do not match").addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            try {
                appUserService.register(
                    usernameField.getValue(),
                    emailField.getValue(),
                    password,
                    firstNameField.getValue(),
                    lastNameField.getValue(),
                    Set.of("ROLE_USER")
                );
                Notification.show("Registration successful! Please log in.")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                getUI().ifPresent(ui -> ui.navigate("login"));
            } catch (IllegalArgumentException ex) {
                Notification.show(ex.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        registerBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerBtn.setWidthFull();

        Anchor loginLink = new Anchor("/login", "Already have an account? Login");
        loginLink.addClassNames(LumoUtility.FontSize.SMALL);

        card.add(title, usernameField, firstNameField, lastNameField,
                 emailField, passwordField, confirmPasswordField, registerBtn,
                 new Div(loginLink));
        add(card);
    }
}
