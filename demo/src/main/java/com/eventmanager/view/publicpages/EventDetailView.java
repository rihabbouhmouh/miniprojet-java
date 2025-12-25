package com.eventmanager.view.publicpages;

import com.eventmanager.entity.Event;
import com.eventmanager.entity.User;
import com.eventmanager.enums.EventCategory;
import com.eventmanager.enums.EventStatus;
import com.eventmanager.enums.UserRole;
import com.eventmanager.service.IEventService;
import com.eventmanager.view.MainLayout;
import com.eventmanager.repository.EventRepository;
import com.eventmanager.security.AuthenticatedUser;
import com.eventmanager.security.NavigationManager;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.time.LocalDateTime;
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
    private boolean isAdmin = false;
    private boolean isEditing = false;
    
    // Edit form fields
    private TextField titreField;
    private TextArea descriptionField;
    private ComboBox<EventCategory> categorieComboBox;
    private DateTimePicker dateDebutPicker;
    private DateTimePicker dateFinPicker;
    private TextField lieuField;
    private TextField villeField;
    private IntegerField capaciteMaxField;
    private NumberField prixUnitaireField;
    private TextField imageUrlField;
    private ComboBox<EventStatus> statutComboBox;
    
    private Button editButton;
    private Button saveButton;
    private Button cancelButton;
    
    private Binder<Event> binder;

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

                // Check if user is admin
        this.isAdmin = authenticatedUser.get()
                .map(user -> user.getRole() == UserRole.ADMIN)
                .orElse(false);

        createEventDetail();
    }

    private void createEventDetail() {
        removeAll();

        // Bouton retour
        Button backButton = new Button("Retour aux événements", VaadinIcon.ARROW_LEFT.create());
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.addClickListener(e -> {
            if (isAdmin) {
                navigationManager.navigateTo("admin/events");
            } else {
                navigationManager.navigateToEvents();
            }
        });
        
        // Edit button for admin
        HorizontalLayout headerButtons = new HorizontalLayout(backButton);
        if (isAdmin && !isEditing) {
            editButton = new Button("Modifier l'événement", VaadinIcon.EDIT.create());
            editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            editButton.addClickListener(e -> enableEditing());
            headerButtons.add(editButton);
        }
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

        if (isEditing) {
            // Show edit form
            add(headerButtons, createEditForm());
        } else {
            // Show read-only view

        container.add(badges, title, infoSection, priceSection, organizerSection);
            add(headerButtons, container);
        }
    }
        private VerticalLayout createEditForm() {
        VerticalLayout formContainer = new VerticalLayout();
        formContainer.setPadding(true);
        formContainer.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-s)");

        H2 formTitle = new H2("✏️ Modifier l'événement");
        formTitle.getStyle().set("margin", "0 0 var(--lumo-space-l) 0");

        // Create form fields
        titreField = new TextField("Titre");
        titreField.setWidthFull();
        titreField.setRequired(true);

        descriptionField = new TextArea("Description");
        descriptionField.setWidthFull();
        descriptionField.setMaxHeight("150px");

        categorieComboBox = new ComboBox<>("Catégorie");
        categorieComboBox.setItems(EventCategory.values());
        categorieComboBox.setItemLabelGenerator(EventCategory::getLabel);
        categorieComboBox.setWidthFull();
        categorieComboBox.setRequired(true);

        dateDebutPicker = new DateTimePicker("Date de début");
        dateDebutPicker.setWidthFull();

        dateFinPicker = new DateTimePicker("Date de fin");
        dateFinPicker.setWidthFull();

        lieuField = new TextField("Lieu");
        lieuField.setWidthFull();
        lieuField.setRequired(true);

        villeField = new TextField("Ville");
        villeField.setWidthFull();
        villeField.setRequired(true);

        capaciteMaxField = new IntegerField("Capacité maximale");
        capaciteMaxField.setWidthFull();
        capaciteMaxField.setMin(1);
        capaciteMaxField.setRequired(true);

        prixUnitaireField = new NumberField("Prix unitaire (DH)");
        prixUnitaireField.setWidthFull();
        prixUnitaireField.setMin(0);
        prixUnitaireField.setRequired(true);

        imageUrlField = new TextField("URL de l'image");
        imageUrlField.setWidthFull();

        statutComboBox = new ComboBox<>("Statut");
        statutComboBox.setItems(EventStatus.values());
        statutComboBox.setWidthFull();
        statutComboBox.setRequired(true);

        // Form layout
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );

        formLayout.add(titreField, 2);
        formLayout.add(descriptionField, 2);
        formLayout.add(categorieComboBox, 1);
        formLayout.add(statutComboBox, 1);
        formLayout.add(dateDebutPicker, 1);
        formLayout.add(dateFinPicker, 1);
        formLayout.add(lieuField, 1);
        formLayout.add(villeField, 1);
        formLayout.add(capaciteMaxField, 1);
        formLayout.add(prixUnitaireField, 1);
        formLayout.add(imageUrlField, 2);

        // Action buttons
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);

        saveButton = new Button("Enregistrer", VaadinIcon.CHECK.create());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> saveEvent());

        cancelButton = new Button("Annuler", VaadinIcon.CLOSE.create());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.addClickListener(e -> cancelEditing());

        buttonLayout.add(saveButton, cancelButton);

        formContainer.add(formTitle, formLayout, buttonLayout);

        // Bind data
        binder = new Binder<>(Event.class);
        binder.bind(titreField, Event::getTitre, Event::setTitre);
        binder.bind(descriptionField, Event::getDescription, Event::setDescription);
        binder.bind(categorieComboBox, Event::getCategorie, Event::setCategorie);
        binder.bind(dateDebutPicker, Event::getDateDebut, Event::setDateDebut);
        binder.bind(dateFinPicker, Event::getDateFin, Event::setDateFin);
        binder.bind(lieuField, Event::getLieu, Event::setLieu);
        binder.bind(villeField, Event::getVille, Event::setVille);
        binder.bind(capaciteMaxField, Event::getCapaciteMax, Event::setCapaciteMax);
        binder.bind(prixUnitaireField, Event::getPrixUnitaire, Event::setPrixUnitaire);
        binder.bind(imageUrlField, Event::getImageUrl, Event::setImageUrl);
        binder.bind(statutComboBox, Event::getStatut, Event::setStatut);
        
        binder.readBean(event);

        return formContainer;
    }
    
    private void enableEditing() {
        isEditing = true;
        createEventDetail();
    }
    
    private void saveEvent() {
        try {
            if (binder.writeBeanIfValid(event)) {
                // Update event using service
                eventService.updateEvent(event.getId(), event);
                
                // Update status if changed
                if (statutComboBox.getValue() != null) {
                    eventService.changeEventStatus(event.getId(), statutComboBox.getValue());
                }
                
                showSuccess("Événement mis à jour avec succès");
                isEditing = false;
                // Reload event from database
                this.event = eventRepository.findById(event.getId()).orElse(event);
                createEventDetail();
            } else {
                showError("Veuillez corriger les erreurs dans le formulaire");
            }
        } catch (Exception e) {
            showError("Erreur lors de la mise à jour: " + e.getMessage());
        }
    }
    
    private void cancelEditing() {
        isEditing = false;
        // Reload event from database to reset changes
        this.event = eventRepository.findById(event.getId()).orElse(event);
        createEventDetail();
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

        
    private void showSuccess(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
}
