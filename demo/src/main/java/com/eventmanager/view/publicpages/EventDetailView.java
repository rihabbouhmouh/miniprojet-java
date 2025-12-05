package com.eventmanager.view.publicpages;

import com.eventmanager.entity.Event;
import com.eventmanager.enums.EventStatus;
import com.eventmanager.service.IEventService;
import com.eventmanager.view.MainLayout;
import com.eventmanager.repository.EventRepository;
import com.eventmanager.security.AuthenticatedUser;
import com.eventmanager.security.NavigationManager;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.time.format.DateTimeFormatter;

@Route(value = "event", layout = MainLayout.class)
@PageTitle("Détail de l'événement")
@AnonymousAllowed
public class EventDetailView extends VerticalLayout implements HasUrlParameter<Long> {

    private final EventRepository eventRepository;
    private final IEventService eventService;
    private final NavigationManager navigationManager;
    private final AuthenticatedUser authenticatedUser;

    private Event event;

    public EventDetailView(EventRepository eventRepository,
                           IEventService eventService,
                           NavigationManager navigationManager,
                           AuthenticatedUser authenticatedUser) {
        this.eventRepository = eventRepository;
        this.eventService = eventService;
        this.navigationManager = navigationManager;
        this.authenticatedUser = authenticatedUser;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, Long eventId) {
        // Récupérer l'événement par ID
        this.event = eventRepository.findById(eventId).orElse(null);

        if (this.event == null) {
            showError("Événement non trouvé");
            navigationManager.navigateToEvents();
            return;
        }

        createEventDetail();
    }

    private void createEventDetail() {
        removeAll();

        // Bouton retour
        Button backButton = new Button("Retour aux événements", VaadinIcon.ARROW_LEFT.create());
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.addClickListener(e -> navigationManager.navigateToEvents());

        // Container principal
        VerticalLayout container = new VerticalLayout();
        container.setWidthFull();
        container.setPadding(true);
        container.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-m)");

        // Badge catégorie et statut
        HorizontalLayout badges = new HorizontalLayout();
        badges.setSpacing(true);

        Span categoryBadge = new Span(event.getCategorie().name());
        categoryBadge.getElement().getThemeList().add("badge");
        categoryBadge.getStyle()
                .set("background", "var(--lumo-primary-color-10pct)")
                .set("color", "var(--lumo-primary-text-color)")
                .set("padding", "6px 12px")
                .set("border-radius", "var(--lumo-border-radius-s)");

        Span statusBadge = new Span(event.getStatut().name());
        statusBadge.getElement().getThemeList().add("badge");
        String statusColor = switch (event.getStatut()) {
            case PUBLIE -> "var(--lumo-success-color)";
            case BROUILLON -> "var(--lumo-contrast-60pct)";
            case ANNULE -> "var(--lumo-error-color)";
            case TERMINE -> "var(--lumo-contrast-40pct)";
        };
        statusBadge.getStyle()
                .set("background", statusColor + "20")
                .set("color", statusColor)
                .set("padding", "6px 12px")
                .set("border-radius", "var(--lumo-border-radius-s)");

        badges.add(categoryBadge, statusBadge);

        // Titre
        H1 title = new H1(event.getTitre());
        title.getStyle().set("margin", "var(--lumo-space-m) 0");

        // Description
        if (event.getDescription() != null && !event.getDescription().isEmpty()) {
            Paragraph description = new Paragraph(event.getDescription());
            description.getStyle()
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("white-space", "pre-wrap");
            container.add(description);
        }

        // Informations principales
        VerticalLayout infoSection = new VerticalLayout();
        infoSection.setSpacing(true);
        infoSection.setPadding(true);
        infoSection.getStyle()
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-m)");

        H3 infoTitle = new H3("📋 Informations");
        infoTitle.getStyle().set("margin", "0");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");

        HorizontalLayout dateInfo = createInfoRow(VaadinIcon.CALENDAR, "Date de début",
                event.getDateDebut().format(formatter));
        HorizontalLayout endDateInfo = createInfoRow(VaadinIcon.CALENDAR_CLOCK, "Date de fin",
                event.getDateFin().format(formatter));
        HorizontalLayout locationInfo = createInfoRow(VaadinIcon.MAP_MARKER, "Lieu",
                event.getLieu() + ", " + event.getVille());
        HorizontalLayout capacityInfo = createInfoRow(VaadinIcon.USERS, "Capacité maximale",
                event.getCapaciteMax() + " places");

        // Places disponibles via eventService
        HorizontalLayout availableInfo = createInfoRow(VaadinIcon.CHECK_CIRCLE, "Places disponibles",
                eventService.getAvailableSeats(event) + " places");

        infoSection.add(infoTitle, dateInfo, endDateInfo, locationInfo, capacityInfo, availableInfo);

        // Prix et réservation
        VerticalLayout priceSection = new VerticalLayout();
        priceSection.setPadding(true);
        priceSection.setAlignItems(Alignment.CENTER);
        priceSection.getStyle()
                .set("background", "var(--lumo-primary-color-10pct)")
                .set("border-radius", "var(--lumo-border-radius-m)");

        H3 priceTitle = new H3("💰 Prix");
        priceTitle.getStyle().set("margin", "0");

        Span price = new Span(String.format("%.0f DH", event.getPrixUnitaire()));
        price.getStyle()
                .set("font-size", "3em")
                .set("font-weight", "bold")
                .set("color", "var(--lumo-primary-text-color)");

        Span pricePerPlace = new Span("par place");
        pricePerPlace.getStyle().set("color", "var(--lumo-secondary-text-color)");

        // Bouton de réservation
        Button reserveButton = new Button("Réserver maintenant", VaadinIcon.TICKET.create());
        reserveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        reserveButton.addClickListener(e -> handleReservation());

        // Désactiver si pas de places disponibles ou événement non publié
        if (eventService.getAvailableSeats(event) <= 0 || event.getStatut() != EventStatus.PUBLIE) {
            reserveButton.setEnabled(false);
            reserveButton.setText("Indisponible");
        }


        priceSection.add(priceTitle, price, pricePerPlace, reserveButton);

        // Organisateur
        VerticalLayout organizerSection = new VerticalLayout();
        organizerSection.setPadding(true);
        organizerSection.getStyle()
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-m)");

        H3 organizerTitle = new H3("👤 Organisateur");
        organizerTitle.getStyle().set("margin", "0");

        String organizerName = event.getOrganisateur().getPrenom() + " " + event.getOrganisateur().getNom();
        Paragraph organizerInfo = new Paragraph(organizerName);
        organizerInfo.getStyle().set("font-weight", "500");

        organizerSection.add(organizerTitle, organizerInfo);

        container.add(badges, title, infoSection, priceSection, organizerSection);
        add(backButton, container);
    }

    private HorizontalLayout createInfoRow(VaadinIcon icon, String label, String value) {
        HorizontalLayout row = new HorizontalLayout();
        row.setAlignItems(Alignment.CENTER);
        row.setSpacing(true);

        Span iconSpan = new Span(icon.create());
        iconSpan.getStyle().set("color", "var(--lumo-primary-text-color)");

        Span labelSpan = new Span(label + ":");
        labelSpan.getStyle()
                .set("font-weight", "500")
                .set("min-width", "150px");

        Span valueSpan = new Span(value);
        valueSpan.getStyle().set("color", "var(--lumo-secondary-text-color)");

        row.add(iconSpan, labelSpan, valueSpan);
        return row;
    }

    private void handleReservation() {
        if (!authenticatedUser.isAuthenticated()) {
            showError("Vous devez être connecté pour réserver");
            navigationManager.navigateToLogin();
            return;
        }

        navigationManager.navigateToReservation(event.getId());
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
