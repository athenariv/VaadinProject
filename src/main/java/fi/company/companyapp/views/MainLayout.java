package fi.company.companyapp.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import java.util.Locale;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.theme.lumo.LumoUtility;
import fi.company.companyapp.push.Broadcaster;
import fi.company.companyapp.security.SecurityService;
import fi.company.companyapp.views.admin.AdminView;
import fi.company.companyapp.views.department.DepartmentView;
import fi.company.companyapp.views.employee.EmployeeView;
import fi.company.companyapp.views.history.HistoryView;
import fi.company.companyapp.views.profile.ProfileView;
import fi.company.companyapp.views.project.ProjectView;
import fi.company.companyapp.views.search.SearchView;
import org.springframework.beans.factory.annotation.Autowired;

@PageTitle("Company App")
public class MainLayout extends AppLayout {

    private final SecurityService securityService;
    private Registration broadcasterRegistration;
    private Button langBtn;

    @Autowired
    public MainLayout(SecurityService securityService) {
        this.securityService = securityService;
        createHeader();
        createDrawer();
    }

    @Override
    public void showRouterLayoutContent(HasElement content) {
        VerticalLayout wrapper = new VerticalLayout();
        wrapper.setWidthFull();
        wrapper.getStyle().set("min-height", "100%");
        wrapper.setPadding(false);
        wrapper.setSpacing(false);

        Component contentComponent = (Component) content;
        wrapper.add(contentComponent);
        wrapper.setFlexGrow(1, contentComponent);
        wrapper.add(buildFooter());

        setContent(wrapper);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        UI ui = attachEvent.getUI();
        // Set language button label to reflect current locale
        if ("fi".equals(ui.getLocale().getLanguage())) {
            langBtn.setText("EN");
        } else {
            langBtn.setText("FI");
        }
        broadcasterRegistration = Broadcaster.register(message ->
            ui.access(() -> {
                Notification n = Notification.show(message, 4000, Notification.Position.TOP_END);
                n.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
            })
        );
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (broadcasterRegistration != null) {
            broadcasterRegistration.remove();
            broadcasterRegistration = null;
        }
    }

    private void createHeader() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.addClassName("drawer-toggle");

        Span appName = new Span("CompanyApp");
        appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.FontWeight.BOLD);
        appName.getStyle().set("color", "var(--lumo-primary-color)");

        String username = securityService.getAuthenticatedUsername();
        Avatar avatar = new Avatar(username != null ? username : "?");
        avatar.addClassName(LumoUtility.Margin.Horizontal.SMALL);

        Span userInfo = new Span(username != null ? username : "Guest");
        userInfo.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

        Button logoutBtn = new Button("Logout", VaadinIcon.SIGN_OUT.create(), e -> securityService.logout());
        logoutBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        logoutBtn.addClassName("logout-button");

        langBtn = new Button("FI");
        langBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        langBtn.getStyle().set("font-weight", "bold");
        langBtn.getElement().setProperty("title", "Switch language");
        langBtn.addClickListener(e -> getUI().ifPresent(ui -> {
            if ("fi".equals(ui.getLocale().getLanguage())) {
                ui.setLocale(Locale.ENGLISH);
                langBtn.setText("FI");
            } else {
                ui.setLocale(new Locale("fi"));
                langBtn.setText("EN");
            }
        }));

        HorizontalLayout userArea = new HorizontalLayout(langBtn, avatar, userInfo, logoutBtn);
        userArea.setAlignItems(FlexComponent.Alignment.CENTER);
        userArea.addClassNames(LumoUtility.Margin.Left.AUTO);

        HorizontalLayout header = new HorizontalLayout(toggle, appName, userArea);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.addClassNames("app-header", LumoUtility.Padding.Horizontal.MEDIUM);
        header.getStyle()
            .set("background", "var(--lumo-base-color)")
            .set("border-bottom", "1px solid var(--lumo-contrast-10pct)");

        addToNavbar(header);
    }

    private void createDrawer() {
        VerticalLayout nav = new VerticalLayout();
        nav.setSizeFull();
        nav.setPadding(true);
        nav.setSpacing(false);

        nav.add(createNavLink("Home", HomeView.class, VaadinIcon.HOME));
        nav.add(createNavLink("Departments", DepartmentView.class, VaadinIcon.BUILDING));
        nav.add(createNavLink("Employees", EmployeeView.class, VaadinIcon.USERS));
        nav.add(createNavLink("Projects", ProjectView.class, VaadinIcon.BRIEFCASE));
        nav.add(createNavLink("Search", SearchView.class, VaadinIcon.SEARCH));
        nav.add(createNavLink("Profile", ProfileView.class, VaadinIcon.USER));

        if (securityService.hasRole("ROLE_SUPER") || securityService.hasRole("ROLE_ADMIN")) {
            nav.add(createNavLink("History", HistoryView.class, VaadinIcon.CLOCK));
        }

        if (securityService.hasRole("ROLE_ADMIN")) {
            nav.add(createNavLink("Admin", AdminView.class, VaadinIcon.COG));
        }

        addToDrawer(nav);
    }

    private <T extends com.vaadin.flow.component.Component> RouterLink createNavLink(String label, Class<T> view, VaadinIcon vaadinIcon) {
        Icon icon = vaadinIcon.create();
        icon.getStyle()
            .set("margin-right", "var(--lumo-space-s)")
            .set("flex-shrink", "0");
        icon.setSize("1em");

        Span labelSpan = new Span(label);

        RouterLink link = new RouterLink();
        link.setRoute(view);
        link.add(icon, labelSpan);
        link.addClassNames("nav-link");
        link.getStyle()
            .set("display", "flex")
            .set("align-items", "center")
            .set("padding", "var(--lumo-space-s) var(--lumo-space-m)")
            .set("text-decoration", "none")
            .set("color", "var(--lumo-body-text-color)")
            .set("border-radius", "var(--lumo-border-radius-m)");
        return link;
    }

    private Div buildFooter() {
        Div footer = new Div();
        footer.addClassName("app-footer");
        footer.setWidthFull();

        Span copyright = new Span("© 2026 CompanyApp");
        copyright.addClassName("app-footer__copyright");

        Span sep1 = new Span(" · ");
        sep1.getStyle().set("color", "var(--lumo-contrast-30pct)");

        Span author = new Span("Made by: Athena R.");
        author.addClassName("app-footer__author");

        Span sep2 = new Span(" · ");
        sep2.getStyle().set("color", "var(--lumo-contrast-30pct)");

        Anchor githubLink = new Anchor("https://github.com/athenariv/VaadinProject", "GitHub");
        githubLink.addClassName("app-footer__link");
        githubLink.setTarget("_blank");

        Span sep3 = new Span(" · ");
        sep3.getStyle().set("color", "var(--lumo-contrast-30pct)");

        Span tech = new Span("Vaadin 23 + Spring Boot");
        tech.addClassName("app-footer__tech");

        footer.add(copyright, sep1, author, sep2, githubLink, sep3, tech);
        return footer;
    }
}
