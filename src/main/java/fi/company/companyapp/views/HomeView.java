package fi.company.companyapp.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import fi.company.companyapp.security.SecurityService;
import javax.annotation.security.PermitAll;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Home | CompanyApp")
@PermitAll
public class HomeView extends VerticalLayout implements LocaleChangeObserver {

    private final H1 heroTitle = new H1();
    private final Paragraph heroSubtitle = new Paragraph();
    private final Paragraph welcomeText = new Paragraph();
    private final H3 deptCardTitle = new H3();
    private final Paragraph deptCardDesc = new Paragraph();
    private final H3 empCardTitle = new H3();
    private final Paragraph empCardDesc = new Paragraph();
    private final H3 projCardTitle = new H3();
    private final Paragraph projCardDesc = new Paragraph();
    private final H3 searchCardTitle = new H3();
    private final Paragraph searchCardDesc = new Paragraph();
    private final String username;

    public HomeView(SecurityService securityService) {
        addClassNames("home-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setPadding(true);
        setSpacing(true);

        username = securityService.getAuthenticatedUsername();

        // Hero section
        Div hero = new Div();
        hero.addClassNames(
            LumoUtility.Background.PRIMARY,
            LumoUtility.BorderRadius.LARGE,
            LumoUtility.Padding.XLARGE,
            LumoUtility.BoxShadow.MEDIUM,
            LumoUtility.Width.FULL
        );
        hero.getStyle().set("text-align", "center").set("color", "white");
        heroTitle.addClassNames(LumoUtility.FontSize.XXXLARGE, LumoUtility.Margin.Bottom.SMALL);
        heroSubtitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.TextColor.BODY);
        heroSubtitle.getStyle().set("opacity", "0.9");
        hero.add(heroTitle, heroSubtitle);

        // Info cards
        Div cards = new Div();
        cards.addClassNames(LumoUtility.Display.FLEX, LumoUtility.Gap.MEDIUM);
        cards.getStyle().set("flex-wrap", "wrap").set("justify-content", "center");
        cards.add(
            buildCard("🏢", deptCardTitle, deptCardDesc, "departments"),
            buildCard("👥", empCardTitle, empCardDesc, "employees"),
            buildCard("📋", projCardTitle, projCardDesc, "projects"),
            buildCard("🔍", searchCardTitle, searchCardDesc, "search")
        );

        welcomeText.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

        add(hero, cards, welcomeText);
        updateTexts();
    }

    private Div buildCard(String icon, H3 title, Paragraph desc, String route) {
        title.addClassNames(LumoUtility.FontSize.MEDIUM, LumoUtility.Margin.Vertical.SMALL);
        desc.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

        Span iconSpan = new Span(icon);
        iconSpan.getStyle().set("font-size", "3rem");

        Div card = new Div(iconSpan, title, desc);
        card.addClassNames(
            LumoUtility.Background.BASE,
            LumoUtility.BorderRadius.MEDIUM,
            LumoUtility.Padding.LARGE,
            LumoUtility.BoxShadow.SMALL,
            LumoUtility.Margin.SMALL
        );
        card.getStyle()
            .set("width", "220px")
            .set("text-align", "center")
            .set("cursor", "pointer")
            .set("transition", "transform 0.2s ease, box-shadow 0.2s ease");
        card.addClassName("hover-card");
        card.addClickListener(e -> UI.getCurrent().navigate(route));
        return card;
    }

    private void updateTexts() {
        heroTitle.setText(getTranslation("home.welcome"));
        heroSubtitle.setText(getTranslation("home.subtitle"));
        welcomeText.setText(getTranslation("home.loggedin") + " " + (username != null ? username : "Guest"));
        deptCardTitle.setText(getTranslation("home.card.departments"));
        deptCardDesc.setText(getTranslation("home.card.departments.desc"));
        empCardTitle.setText(getTranslation("home.card.employees"));
        empCardDesc.setText(getTranslation("home.card.employees.desc"));
        projCardTitle.setText(getTranslation("home.card.projects"));
        projCardDesc.setText(getTranslation("home.card.projects.desc"));
        searchCardTitle.setText(getTranslation("home.card.search"));
        searchCardDesc.setText(getTranslation("home.card.search.desc"));
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        updateTexts();
    }
}

