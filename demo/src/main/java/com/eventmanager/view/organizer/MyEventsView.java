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
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

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
        setPadding(false);
        setSpacing(false);

        getStyle()
                .set("background", "#f5f7fa")
                .set("padding", "24px");

        // Centered page container
        Div page = new Div();
        page.getStyle()
                .set("max-width", "1200px")
                .set("margin", "0 auto")
                .set("width", "100%");

        page.add(buildHeader());
        page.add(buildFiltersCard());
        page.add(buildGridCard());

        add(page);

        loadMyEvents();
    }

    /* ================= HEADER ================= */

    private Div buildHeader() {
        Div header = new Div();
        header.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "6px")
                .set("margin-bottom", "18px");

        H2 title = new H2("📌 Mes Événements");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "2rem")
                .set("font-weight", "800")
                .set("color", "#111827");

        Paragraph subtitle = new Paragraph("Gérez, publiez et suivez vos événements. Utilisez les filtres pour trouver rapidement.");
        subtitle.getStyle()
                .set("margin", "0")
                .set("color", "#6b7280");

        header.add(title, subtitle);
        return header;
    }

    /* ================= FILTERS (CARD) ================= */

    private Div buildFiltersCard() {
        Div card = new Div();
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "14px")
                .set("box-shadow", "0 10px 30px rgba(0,0,0,0.06)")
                .set("border", "1px solid rgba(17,24,39,0.06)")
                .set("padding", "16px")
                .set("margin-bottom", "14px");

        // Title row
        HorizontalLayout topRow = new HorizontalLayout();
        topRow.setWidthFull();
        topRow.setAlignItems(FlexComponent.Alignment.CENTER);
        topRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        H3 filtersTitle = new H3("🔎 Filtres");
        filtersTitle.getStyle()
                .set("margin", "0")
                .set("font-size", "1.2rem")
                .set("font-weight", "800")
                .set("color", "#111827");

        Span hint = new Span("Astuce : filtre par titre, catégorie et statut");
        hint.getStyle()
                .set("color", "#6b7280")
                .set("font-size", "0.95rem");

        topRow.add(filtersTitle, hint);

        // Controls row
        keywordField = new TextField();
        keywordField.setPlaceholder("Rechercher par titre...");
        keywordField.setPrefixComponent(VaadinIcon.SEARCH.create());
        keywordField.setWidthFull();
        keywordField.addValueChangeListener(e -> applyFilters());

        categoryFilter = new ComboBox<>("Catégorie");
        categoryFilter.setItems(EventCategory.values());
        categoryFilter.setItemLabelGenerator(EventCategory::name);
        categoryFilter.setClearButtonVisible(true);
        categoryFilter.setWidth("220px");
        categoryFilter.addValueChangeListener(e -> applyFilters());

        statusFilter = new ComboBox<>("Statut");
        statusFilter.setItems(EventStatus.values());
        statusFilter.setClearButtonVisible(true);
        statusFilter.setWidth("220px");
        statusFilter.addValueChangeListener(e -> applyFilters());

        Button refresh = new Button("Actualiser", VaadinIcon.REFRESH.create());
        refresh.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        refresh.addClickListener(e -> loadMyEvents());

        Button create = new Button("Créer un événement", VaadinIcon.PLUS.create());
        create.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        create.getStyle().set("font-weight", "700");
        create.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("organizer/event/new")));

        HorizontalLayout controls = new HorizontalLayout(keywordField, categoryFilter, statusFilter, refresh, create);
        controls.setWidthFull();
        controls.setAlignItems(Alignment.END);
        controls.expand(keywordField);
        controls.getStyle()
                .set("flex-wrap", "wrap")
                .set("gap", "10px");

        card.add(topRow, spacer(10), controls);
        return card;
    }

    /* ================= GRID (CARD) ================= */

    private Div buildGridCard() {
        Div card = new Div();
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "14px")
                .set("box-shadow", "0 10px 30px rgba(0,0,0,0.06)")
                .set("border", "1px solid rgba(17,24,39,0.06)")
                .set("padding", "14px");

        // Grid header row
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        H3 tableTitle = new H3("📋 Liste de mes événements");
        tableTitle.getStyle()
                .set("margin", "0")
                .set("font-size", "1.2rem")
                .set("font-weight", "800")
                .set("color", "#111827");

        Span hint = new Span("Clique sur “Modifier” pour mettre à jour l’événement");
        hint.getStyle().set("color", "#6b7280");

        header.add(tableTitle, hint);

        grid = createGrid();
        grid.setAllRowsVisible(true); // no tiny table feeling
        setSizeFull();
        grid.setWidthFull();
        grid.setSizeFull();

        card.getStyle().set("width", "100%");

        card.add(header, spacer(10), grid);
        return card;
    }

    private Grid<Event> createGrid() {
        Grid<Event> g = new Grid<>(Event.class, false);
        g.setWidthFull();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        g.addColumn(Event::getId).setHeader("ID").setWidth("80px").setFlexGrow(0);

        g.addColumn(Event::getTitre)
                .setHeader("Titre")
                .setAutoWidth(true)
                .setFlexGrow(2);

        g.addColumn(e -> e.getCategorie() != null ? e.getCategorie().name() : "—")
                .setHeader("Catégorie")
                .setAutoWidth(true)
                .setFlexGrow(1);

        g.addColumn(Event::getVille)
                .setHeader("Ville")
                .setAutoWidth(true)
                .setFlexGrow(1);

        g.addColumn(e -> e.getDateDebut() != null ? e.getDateDebut().format(fmt) : "—")
                .setHeader("Début")
                .setAutoWidth(true)
                .setFlexGrow(1);

        g.addColumn(e -> e.getPrixUnitaire() != null ? String.format("%.2f DH", e.getPrixUnitaire()) : "—")
                .setHeader("Prix")
                .setAutoWidth(true)
                .setFlexGrow(0);

        g.addComponentColumn(this::statusPill)
                .setHeader("Statut")
                .setAutoWidth(true)
                .setFlexGrow(0);

        g.addComponentColumn(this::actionsColumn)
                .setHeader("Actions")
                .setAutoWidth(true)
                .setFlexGrow(0);

        return g;
    }

    private Span statusPill(Event e) {
        String text = e.getStatut() != null ? e.getStatut().name() : "—";
        Span pill = new Span(text);

        String bg;
        String color;

        if (e.getStatut() == EventStatus.PUBLIE) {
            bg = "rgba(34,197,94,0.15)";
            color = "#16a34a";
        } else if (e.getStatut() == EventStatus.BROUILLON) {
            bg = "rgba(245,158,11,0.16)";
            color = "#b45309";
        } else if (e.getStatut() == EventStatus.ANNULE) {
            bg = "rgba(239,68,68,0.15)";
            color = "#dc2626";
        } else if (e.getStatut() == EventStatus.TERMINE) {
            bg = "rgba(107,114,128,0.18)";
            color = "#374151";
        } else {
            bg = "rgba(102,126,234,0.14)";
            color = "#4f46e5";
        }

        pill.getStyle()
                .set("padding", "6px 10px")
                .set("border-radius", "999px")
                .set("font-weight", "800")
                .set("font-size", "0.8rem")
                .set("background", bg)
                .set("color", color)
                .set("border", "1px solid rgba(17,24,39,0.08)");

        return pill;
    }

    /* ================= ACTIONS ================= */

    private HorizontalLayout actionsColumn(Event event) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(false);
        actions.getStyle()
                .set("flex-wrap", "wrap")
                .set("gap", "8px");

        // Edit
        Button edit = new Button("Modifier", VaadinIcon.EDIT.create());
        edit.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        edit.getStyle().set("font-weight", "700");
        edit.addClickListener(e -> {
            try {
                openEditDialog(event);
            } catch (Exception ex) {
                Notification.show("Erreur ouverture formulaire: " + ex.getMessage(),
                                5000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        actions.add(edit);

        // Status actions
        if (event.getStatut() == EventStatus.BROUILLON) {
            Button publish = new Button("Publier", VaadinIcon.CHECK.create());
            publish.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
            publish.getStyle().set("font-weight", "700");
            publish.addClickListener(e -> updateStatus(event, EventStatus.PUBLIE));
            actions.add(publish);
        }

        if (event.getStatut() == EventStatus.PUBLIE) {
            Button backToDraft = new Button("Brouillon", VaadinIcon.ROTATE_LEFT.create());
            backToDraft.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
            backToDraft.getStyle().set("font-weight", "700");
            backToDraft.addClickListener(e -> updateStatus(event, EventStatus.BROUILLON));
            actions.add(backToDraft);

            Button cancel = new Button("Annuler", VaadinIcon.CLOSE.create());
            cancel.addThemeVariants(ButtonVariant.LUMO_ERROR);
            cancel.getStyle().set("font-weight", "700");
            cancel.addClickListener(e -> updateStatus(event, EventStatus.ANNULE));
            actions.add(cancel);
        }

        // Reservations
        Button reservations = new Button("Réservations", VaadinIcon.TICKET.create());
        reservations.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_TERTIARY);
        reservations.getStyle().set("font-weight", "700");
        reservations.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("organizer/event/" + event.getId() + "/reservations"))
        );
        actions.add(reservations);

        // Delete (kept last)
        Button delete = new Button("", VaadinIcon.TRASH.create());
        delete.getElement().setProperty("title", "Supprimer");
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        delete.addClickListener(e -> confirmDelete(event));
        actions.add(delete);

        return actions;
    }

    /* ================= DATA ================= */

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
        dialog.setWidth("520px");

        Div box = new Div();
        box.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "12px")
                .set("padding", "8px");

        Span text = new Span("Supprimer l'événement : " + event.getTitre() + " ?");
        text.getStyle().set("font-weight", "700");

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
        no.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout actions = new HorizontalLayout(yes, no);
        actions.setSpacing(true);

        box.add(text, actions);
        dialog.add(box);
        dialog.open();
    }

    /* ================= EDIT DIALOG  ================= */

    private void openEditDialog(Event original) {
        Event event = eventService.getEventById(original.getId());

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Modifier l'événement");
        dialog.setWidth("900px");
        dialog.setMaxWidth("95vw");

        // Make dialog content look modern (card-like)
        Div wrap = new Div();
        wrap.getStyle()
                .set("padding", "8px 8px 14px 8px")
                .set("max-width", "100%");

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

        // Editable copy
        Event edited = new Event();
        edited.setId(event.getId());
        edited.setOrganisateur(event.getOrganisateur());
        edited.setStatut(event.getStatut());
        edited.setDateCreation(event.getDateCreation());

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
        save.getStyle().set("font-weight", "800");

        save.addClickListener(e -> {
            try {
                binder.writeBean(edited);
                eventService.updateEvent(event.getId(), edited);

                Notification.show("Événement mis à jour", 2500, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                dialog.close();
                loadMyEvents();
            } catch (ValidationException ve) {
                // Binder shows errors
            } catch (Exception ex) {
                Notification.show(ex.getMessage(), 5000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        Button cancel = new Button("Annuler", e -> dialog.close());
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout buttons = new HorizontalLayout(save, cancel);
        buttons.setSpacing(true);
        buttons.getStyle().set("margin-top", "10px");

        wrap.add(form, buttons);
        dialog.add(wrap);
        dialog.open();
    }

    private Div spacer(int px) {
        Div s = new Div();
        s.getStyle().set("height", px + "px");
        return s;
    }
}
