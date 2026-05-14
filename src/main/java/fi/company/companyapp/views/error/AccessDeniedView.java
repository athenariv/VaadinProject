package fi.company.companyapp.views.error;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.security.access.AccessDeniedException;

import javax.servlet.http.HttpServletResponse;

@Route("access-denied")
@PageTitle("Access Denied | CompanyApp")
@AnonymousAllowed
public class AccessDeniedView extends VerticalLayout implements HasErrorParameter<AccessDeniedException> {

    public AccessDeniedView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        addClassName("access-denied-view");

        var icon = VaadinIcon.LOCK.create();
        icon.setSize("64px");
        icon.getStyle().set("color", "var(--lumo-error-color)");
        icon.getStyle().set("margin-bottom", "var(--lumo-space-m)");

        H2 title = new H2("Access Denied");
        title.getStyle().set("color", "var(--lumo-error-color)");

        Paragraph message = new Paragraph("You don't have permission to access this page.");
        message.getStyle()
            .set("color", "var(--lumo-secondary-text-color)")
            .set("text-align", "center")
            .set("max-width", "400px");

        Button homeButton = new Button("Go to Home", VaadinIcon.HOME.create(),
                e -> getUI().ifPresent(ui -> ui.navigate("")));
        homeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button backButton = new Button("Go Back", VaadinIcon.ARROW_LEFT.create(),
                e -> getUI().ifPresent(ui -> ui.getPage().getHistory().back()));
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        add(icon, title, message, homeButton, backButton);
        setSpacing(true);
    }

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<AccessDeniedException> parameter) {
        return HttpServletResponse.SC_FORBIDDEN;
    }
}
