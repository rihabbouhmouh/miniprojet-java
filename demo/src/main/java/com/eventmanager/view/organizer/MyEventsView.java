package com.eventmanager.view.organizer;

import com.eventmanager.entity.Event;
import com.eventmanager.entity.User;
import com.eventmanager.enums.EventCategory;
import com.eventmanager.enums.EventStatus;
import com.eventmanager.security.AuthenticatedUser;
import com.eventmanager.service.IEventService;
import com.eventmanager.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.data.binder.ValidationException;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "organizer/events", layout = MainLayout.class)
@PageTitle("Mes Événements - Organisateur")
public class MyEventsView extends VerticalLayout {

    private final AuthenticatedUser authenticatedUser;
    private final IEventService eventService;

    private Grid<Event> grid;
    private ListDataProvider<Event> dataProvider;

    private TextField keywordField;
    private ComboBox<EventCategory> categoryFilter;
    private ComboBox<EventStatus> statusFilter;

    private User organizer;

    public MyEventsView(AuthenticatedUser authenticatedUser, IEventService eventService) {
        this.authenticatedUser = authenticatedUser;
        this.eventService = eventService;

        this.organizer = authenticatedUser.get()
                .orElseThrow(() -> new IllegalStateException("Utilisateur non authentifié"));

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("📌 Mes Événements");
        title.addClassNames(LumoUtility.FontSize.XXXLARGE, LumoUtility.Margin.Bottom.LARGE);

        add(title);
        add(createFiltersBar());
        add(createGrid());

        loadMyEvents();
    }

    private HorizontalLayout createFiltersBar() {
        keywordField = new TextField();
        keywordField.setPlaceholder("Rechercher par titre...");
        keywordField.setPrefixComponent(VaadinIcon.SEARCH.create());
        keywordField.addValueChangeListener(e -> applyFilters());

        categoryFilter = new ComboBox<>("Catégorie");
        categoryFilter.setItems(EventCategory.values());
        categoryFilter.setClearButtonVisible(true);
        categoryFilter.addValueChangeListener(e -> applyFilters());

        statusFilter = new ComboBox<>("Statut");
        statusFilter.setItems(EventStatus.values());
        statusFilter.setClearButtonVisible(true);
        statusFilter.addValueChangeListener(e -> applyFilters());

        Button refresh = new Button("Actualiser", VaadinIcon.REFRESH.create());
        refresh.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        refresh.addClickListener(e -> loadMyEvents());

        Button create = new Button("Créer un événement", VaadinIcon.PLUS_CIRCLE.create());
        create.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        create.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("organizer/event/new")));

        HorizontalLayout bar = new HorizontalLayout(keywordField, categoryFilter, statusFilter, refresh, create);
        bar.setWidthFull();
        bar.setAlignItems(Alignment.END);
        bar.expand(keywordField);
        bar.getStyle().set("flex-wrap", "wrap");
        return bar;
    }

    private Grid<Event> createGrid() {
        grid = new Grid<>(Event.class, false);
        grid.setSizeFull();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        grid.addColumn(Event::getId).setHeader("ID").setWidth("80px").setFlexGrow(0);
        grid.addColumn(Event::getTitre).setHeader("Titre").setAutoWidth(true);
        grid.addColumn(Event::getCategorie).setHeader("Catégorie").setAutoWidth(true);
        grid.addColumn(Event::getVille).setHeader("Ville").setAutoWidth(true);
        grid.addColumn(e -> e.getDateDebut() != null ? e.getDateDebut().format(fmt) : "—")
                .setHeader("Début").setAutoWidth(true);
        grid.addColumn(e -> e.getDateFin() != null ? e.getDateFin().format(fmt) : "—")
                .setHeader("Fin").setAutoWidth(true);
        grid.addColumn(e -> e.getPrixUnitaire() != null ? String.format("%.2f DH", e.getPrixUnitaire()) : "—")
                .setHeader("Prix").setAutoWidth(true);
        grid.addComponentColumn(this::statusBadge).setHeader("Statut").setAutoWidth(true);

        grid.addComponentColumn(this::actionsColumn).setHeader("Actions").setAutoWidth(true);

        return grid;
    }

    private Span statusBadge(Event e) {
        String s = e.getStatut() != null ? e.getStatut().name() : "—";
        Span badge = new Span(s);

        String bg = switch (e.getStatut()) {
            case PUBLIE -> "#4caf50";
            case BROUILLON -> "#ff9800";
            case ANNULE -> "#f44336";
            default -> "#607d8b";
        };

        badge.getStyle()
                .set("padding", "4px 8px")
                .set("border-radius", "4px")
                .set("color", "white")
                .set("background", bg);
        return badge;
    }

    private HorizontalLayout actionsColumn(Event event) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        actions.getStyle().set("flex-wrap", "wrap");
        actions.getStyle().set("gap", "0.25rem");

    Button edit = new Button("Modifier", VaadinIcon.EDIT.create());
    edit.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    edit.addClickListener(e -> {
        try {
            openEditDialog(event);
        } catch (Exception ex) {
            Notification.show("Erreur ouverture formulaire: " + ex.getMessage(),
                    5000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            ex.printStackTrace();
        }
    });


        actions.add(edit);

        if (event.getStatut() == EventStatus.BROUILLON) {
            Button publish = new Button("Publier", VaadinIcon.CHECK.create());
            publish.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
            publish.addClickListener(e -> updateStatus(event, EventStatus.PUBLIE));
            actions.add(publish);
        }

        if (event.getStatut() == EventStatus.PUBLIE) {
            Button backToDraft = new Button("Brouillon", VaadinIcon.ROTATE_LEFT.create());
            backToDraft.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
            backToDraft.addClickListener(e -> updateStatus(event, EventStatus.BROUILLON));
            actions.add(backToDraft);

            Button cancel = new Button("Annuler", VaadinIcon.CLOSE.create());
            cancel.addThemeVariants(ButtonVariant.LUMO_ERROR);
            cancel.addClickListener(e -> updateStatus(event, EventStatus.ANNULE));
            actions.add(cancel);
        }

        Button delete = new Button("Supprimer", VaadinIcon.TRASH.create());
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        delete.addClickListener(e -> confirmDelete(event));
        actions.add(delete);

        return actions;
    }

    private void loadMyEvents() {
        List<Event> myEvents = eventService.getEventsByOrganizer(organizer.getId());
        dataProvider = new ListDataProvider<>(myEvents);
        grid.setDataProvider(dataProvider);
        applyFilters();
    }

    private void applyFilters() {
        if (dataProvider == null) return;
        dataProvider.clearFilters();

        String keyword = keywordField.getValue();
        if (keyword != null && !keyword.isBlank()) {
            String k = keyword.toLowerCase();
            dataProvider.addFilter(e -> e.getTitre() != null && e.getTitre().toLowerCase().contains(k));
        }

        if (categoryFilter.getValue() != null) {
            dataProvider.addFilter(e -> e.getCategorie() == categoryFilter.getValue());
        }

        if (statusFilter.getValue() != null) {
            dataProvider.addFilter(e -> e.getStatut() == statusFilter.getValue());
        }
    }

    private void updateStatus(Event event, EventStatus newStatus) {
        try {
            eventService.changeEventStatus(event.getId(), newStatus);
            Notification.show("Statut mis à jour", 2500, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            loadMyEvents();
        } catch (Exception ex) {
            Notification.show(ex.getMessage(), 5000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void confirmDelete(Event event) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Confirmer suppression");

        Span text = new Span("Supprimer l'événement : " + event.getTitre() + " ?");
        Button yes = new Button("Oui, supprimer", VaadinIcon.TRASH.create(), e -> {
            try {
                eventService.deleteEvent(event.getId());
                Notification.show("Événement supprimé", 2500, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                loadMyEvents();
            } catch (Exception ex) {
                Notification.show(ex.getMessage(), 5000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        yes.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button no = new Button("Annuler", e -> dialog.close());

        dialog.add(new VerticalLayout(text, new HorizontalLayout(yes, no)));
        dialog.open();
    }

private void openEditDialog(Event original) {
    // Keep reload from DB if you want (safe)
    Event event = eventService.getEventById(original.getId());

    Dialog dialog = new Dialog();
    dialog.setHeaderTitle("Modifier l'événement");
    dialog.setWidth("900px");
    dialog.setMaxWidth("95vw");

    TextField titre = new TextField("Titre");
    TextArea description = new TextArea("Description");
    ComboBox<EventCategory> categorie = new ComboBox<>("Catégorie");
    DateTimePicker debut = new DateTimePicker("Date début");
    DateTimePicker fin = new DateTimePicker("Date fin");
    TextField lieu = new TextField("Lieu");
    TextField ville = new TextField("Ville");

    IntegerField capacite = new IntegerField("Capacité max");
    capacite.setMin(1);
    capacite.setStepButtonsVisible(true);

    NumberField prix = new NumberField("Prix unitaire");
    TextField imageUrl = new TextField("Image URL");

    categorie.setItems(EventCategory.values());
    description.setWidthFull();
    description.setMinHeight("120px");

    // ✅ Create editable copy WITH ALL VALUES (IMPORTANT)
    Event edited = new Event();
    edited.setId(event.getId());
    edited.setOrganisateur(event.getOrganisateur()); // keep organizer
    edited.setStatut(event.getStatut());             // keep status
    edited.setDateCreation(event.getDateCreation()); // keep creation date (optional)

    edited.setTitre(event.getTitre());
    edited.setDescription(event.getDescription());
    edited.setCategorie(event.getCategorie());
    edited.setDateDebut(event.getDateDebut());
    edited.setDateFin(event.getDateFin());
    edited.setLieu(event.getLieu());
    edited.setVille(event.getVille());
    edited.setCapaciteMax(event.getCapaciteMax());
    edited.setPrixUnitaire(event.getPrixUnitaire());
    edited.setImageUrl(event.getImageUrl());

    Binder<Event> binder = new Binder<>(Event.class);

    binder.forField(titre).asRequired("Titre obligatoire")
            .bind(Event::getTitre, Event::setTitre);

    binder.forField(description)
            .bind(Event::getDescription, Event::setDescription);

    binder.forField(categorie).asRequired("Catégorie obligatoire")
            .bind(Event::getCategorie, Event::setCategorie);

    binder.forField(debut).asRequired("Date début obligatoire")
            .bind(Event::getDateDebut, Event::setDateDebut);

    binder.forField(fin).asRequired("Date fin obligatoire")
            .bind(Event::getDateFin, Event::setDateFin);

    binder.forField(lieu).asRequired("Lieu obligatoire")
            .bind(Event::getLieu, Event::setLieu);

    binder.forField(ville).asRequired("Ville obligatoire")
            .bind(Event::getVille, Event::setVille);

    binder.forField(capacite).asRequired("Capacité obligatoire")
            .withValidator(v -> v != null && v > 0, "Capacité doit être > 0")
            .bind(Event::getCapaciteMax, Event::setCapaciteMax);

    binder.forField(prix).asRequired("Prix obligatoire")
            .withValidator(v -> v != null && v >= 0, "Prix doit être ≥ 0")
            .bind(Event::getPrixUnitaire, Event::setPrixUnitaire);

    binder.forField(imageUrl)
            .bind(Event::getImageUrl, Event::setImageUrl);

    // ✅ Now bind the bean AFTER it already has values
    binder.setBean(edited);

    FormLayout form = new FormLayout(
            titre, categorie, ville, lieu,
            debut, fin,
            capacite, prix,
            imageUrl,
            description
    );

    form.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("800px", 2)
    );
    form.setColspan(description, 2);

    Button save = new Button("Enregistrer", VaadinIcon.CHECK.create());
    save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    save.addClickListener(e -> {
        try {
            binder.writeBean(edited); // ✅ ensures latest values are in edited
            eventService.updateEvent(event.getId(), edited);

            Notification.show("Événement mis à jour", 2500, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            dialog.close();
            loadMyEvents();
        } catch (ValidationException ve) {
            // binder will show field errors automatically
        } catch (Exception ex) {
            Notification.show(ex.getMessage(), 5000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    });

    Button cancel = new Button("Annuler", e -> dialog.close());

    dialog.add(new VerticalLayout(form, new HorizontalLayout(save, cancel)));
    dialog.open();
}


}
