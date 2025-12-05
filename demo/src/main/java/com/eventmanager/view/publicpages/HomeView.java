package com.eventmanager.view.publicpages;

import com.eventmanager.entity.Event;
import com.eventmanager.enums.EventCategory;
import com.eventmanager.service.IEventService;
import com.eventmanager.view.MainLayout;
import com.eventmanager.security.NavigationManager;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "home", layout = MainLayout.class)
@PageTitle("Accueil - Event Manager")
@AnonymousAllowed
public class HomeView extends VerticalLayout {

    private final IEventService eventService;
    private final NavigationManager navigationManager;

    public HomeView(IEventService eventService,
                    NavigationManager navigationManager) {
        this.eventService = eventService;
        this.navigationManager = navigationManager;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        createHeroSection();
        createFeaturedEventsSection();
        createCallToActionSection();
    }

    private void createHeroSection() {
        VerticalLayout hero = new VerticalLayout();
        hero.setWidthFull();
        hero.setPadding(true);
        hero.setSpacing(true);
        hero.setAlignItems(Alignment.CENTER);
        hero.getStyle()
                .set("background", "linear-gradient(135deg, var(--lumo-primary-color) 0%, var(--lumo-primary-color-50pct) 100%)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("color", "white")
                .set("padding", "var(--lumo-space-xl)");

        H1 title = new H1("🎭 Bienvenue sur Event Manager");
        title.getStyle().set("margin", "0").set("color", "white");

        Paragraph subtitle = new Paragraph(
                "Découvrez et réservez les meilleurs événements culturels au Maroc"
        );
        subtitle.getStyle()
                .set("font-size", "var(--lumo-font-size-xl)")
                .set("text-align", "center")
                .set("color", "white")
                .set("margin", "var(--lumo-space-m) 0");

        Button exploreButton = new Button("Voir tous les événements", VaadinIcon.CALENDAR.create());
        exploreButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_CONTRAST);
        exploreButton.addClickListener(e -> navigationManager.navigateToEvents());

        hero.add(title, subtitle, exploreButton);
        add(hero);
    }

    private void createFeaturedEventsSection() {
        H2 sectionTitle = new H2("📅 Événements à la une");
        sectionTitle.getStyle().set("margin-top", "var(--lumo-space-xl)");
        add(sectionTitle);

        // Récupérer les événements populaires
        List<Event> popularEvents = eventService.getFeaturedEvents(6);

        if (popularEvents.isEmpty()) {
            Paragraph noEvents = new Paragraph("Aucun événement disponible pour le moment.");
            noEvents.getStyle().set("color", "var(--lumo-secondary-text-color)");
            add(noEvents);
        } else {
            HorizontalLayout eventsGrid = new HorizontalLayout();
            eventsGrid.setWidthFull();
            eventsGrid.setSpacing(true);
            eventsGrid.getStyle().set("flex-wrap", "wrap");

            for (Event event : popularEvents) {
                eventsGrid.add(createEventCard(event));
            }

            add(eventsGrid);
        }

        Button viewAllButton = new Button("Voir tous les événements", VaadinIcon.ARROW_RIGHT.create());
        viewAllButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        viewAllButton.addClickListener(e -> navigationManager.navigateToEvents());
        add(viewAllButton);
    }

    private VerticalLayout createEventCard(Event event) {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("300px");
        card.setPadding(true);
        card.setSpacing(false);
        card.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("cursor", "pointer")
                .set("transition", "transform 0.2s, box-shadow 0.2s");

        // Effet hover
        card.getElement().addEventListener("mouseenter", e -> {
            card.getStyle()
                    .set("transform", "translateY(-4px)")
                    .set("box-shadow", "var(--lumo-box-shadow-m)");
        });
        card.getElement().addEventListener("mouseleave", e -> {
            card.getStyle()
                    .set("transform", "translateY(0)")
                    .set("box-shadow", "none");
        });

        // Catégorie
        Span category = new Span(event.getCategorie() != null ? event.getCategorie().name() : EventCategory.AUTRE.name());
        category.getElement().getThemeList().add("badge");
        category.getStyle()
                .set("background", "var(--lumo-primary-color-10pct)")
                .set("color", "var(--lumo-primary-text-color)")
                .set("padding", "4px 8px")
                .set("border-radius", "var(--lumo-border-radius-s)")
                .set("font-size", "var(--lumo-font-size-s)");

        // Titre
        Span title = new Span(event.getTitre());
        title.getStyle()
                .set("font-weight", "bold")
                .set("font-size", "var(--lumo-font-size-l)")
                .set("margin", "var(--lumo-space-s) 0");

        // Date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Span date = new Span(VaadinIcon.CALENDAR.create(),
                new Span(" " + (event.getDateDebut() != null ? event.getDateDebut().format(formatter) : "N/A")));
        date.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        // Lieu
        Span location = new Span(VaadinIcon.MAP_MARKER.create(),
                new Span(" " + (event.getVille() != null ? event.getVille() : "N/A")));
        location.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        // Prix
        Span price = new Span(event.getPrixUnitaire() != null ? String.format("%.0f DH", event.getPrixUnitaire()) : "Gratuit");
        price.getStyle()
                .set("font-weight", "bold")
                .set("color", "var(--lumo-primary-text-color)")
                .set("font-size", "var(--lumo-font-size-xl)")
                .set("margin-top", "var(--lumo-space-s)");

        // Bouton
        Button detailsButton = new Button("Voir détails", VaadinIcon.EYE.create());
        detailsButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        detailsButton.setWidthFull();
        detailsButton.addClickListener(e -> navigationManager.navigateToEventDetail(event.getId()));

        card.add(category, title, date, location, price, detailsButton);
        return card;
    }

    private void createCallToActionSection() {
        VerticalLayout cta = new VerticalLayout();
        cta.setWidthFull();
        cta.setPadding(true);
        cta.setSpacing(true);
        cta.setAlignItems(Alignment.CENTER);
        cta.getStyle()
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("margin-top", "var(--lumo-space-xl)")
                .set("padding", "var(--lumo-space-xl)");

        H2 ctaTitle = new H2("Organisez vos propres événements !");
        Paragraph ctaText = new Paragraph("Vous êtes un organisateur ? Créez vos événements et partagez-les avec notre communauté.");
        Button createEventButton = new Button("Créer un événement", VaadinIcon.PLUS.create());
        createEventButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createEventButton.addClickListener(e -> navigationManager.navigateToCreateEvent());

        cta.add(ctaTitle, ctaText, createEventButton);
        add(cta);
    }
}
