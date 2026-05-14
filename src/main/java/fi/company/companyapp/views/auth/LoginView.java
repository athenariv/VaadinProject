package fi.company.companyapp.views.auth;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;

@Route("login")
@PageTitle("Login | CompanyApp")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm loginForm = new LoginForm();

    public LoginView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        addClassName("login-view");

        getStyle().set("background", "var(--lumo-contrast-5pct)");

        Div card = new Div();
        card.addClassNames(
            LumoUtility.Background.BASE,
            LumoUtility.BorderRadius.LARGE,
            LumoUtility.Padding.XLARGE,
            LumoUtility.BoxShadow.LARGE
        );
        card.getStyle().set("min-width", "400px").set("text-align", "center");

        H1 title = new H1("CompanyApp");
        title.addClassNames(LumoUtility.FontSize.XXLARGE, LumoUtility.TextColor.PRIMARY);

        Paragraph subtitle = new Paragraph("Please log in to continue");
        subtitle.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.Margin.Bottom.MEDIUM);

        loginForm.setAction("login");
        loginForm.setForgotPasswordButtonVisible(false);

        Div divider = new Div(new Span("or continue with"));
        divider.addClassNames(LumoUtility.Margin.Vertical.MEDIUM, LumoUtility.TextColor.SECONDARY);

        Div oauthButtons = new Div();
        oauthButtons.addClassNames(LumoUtility.Display.FLEX, LumoUtility.Gap.MEDIUM);
        oauthButtons.getStyle().set("justify-content", "center");

        Button githubBtn = new Button("GitHub");
        githubBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        githubBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.getPage().setLocation("/oauth2/authorization/github")));
        githubBtn.addClassName("oauth-button");

        Button googleBtn = new Button("Google");
        googleBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        googleBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.getPage().setLocation("/oauth2/authorization/google")));
        googleBtn.addClassName("oauth-button");

        oauthButtons.add(githubBtn, googleBtn);

        Anchor registerLink = new Anchor("/register", "Don't have an account? Register");
        registerLink.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.Margin.Top.MEDIUM);

        card.add(title, subtitle, loginForm, divider, oauthButtons, new Div(registerLink));
        add(card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (event.getLocation().getQueryParameters().getParameters().containsKey("error")) {
            loginForm.setError(true);
        }
    }
}
