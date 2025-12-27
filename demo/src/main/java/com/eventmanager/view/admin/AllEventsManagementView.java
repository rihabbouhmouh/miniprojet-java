package com.eventmanager.view.admin;

import com.eventmanager.entity.Event;
import com.eventmanager.enums.EventCategory;
import com.eventmanager.enums.EventStatus;
import com.eventmanager.repository.EventRepository;
import com.eventmanager.service.IEventService;
import com.eventmanager.view.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Route(value = "admin/events", layout = MainLayout.class)
@PageTitle("Admin - Tous les événements")
public class AllEventsManagementView extends VerticalLayout {

    private final IEventService eventService;
    private final EventRepository eventRepository;

    private Grid<Event> grid;
    private List<Event> allEvents = new ArrayList<>();
    private ListDataProvider<Event> dataProvider;

    private TextField searchField;
    private ComboBox<EventCategory> categoryFilter;
    private ComboBox<EventStatus> statusFilter;

    private Span countSpan;

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public AllEventsManagementView(IEventService eventService, EventRepository eventRepository) {
        this.eventService = eventService;
        this.eventRepository = eventRepository;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        addClassNames(LumoUtility.Padding.LARGE, LumoUtility.Gap.MEDIUM);

        add(buildHeader(), buildToolbar(), buildGridCard());

        loadEvents();
    }

    // ---------- UI BUILDERS ----------

    private Component buildHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        Div left = new Div();
        H2 title = new H2("Gestion des événements");
        title.getStyle().set("margin", "0");
        Span subtitle = new Span("Tous les événements (tous organisateurs) — filtres + actions rapides");
        subtitle.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);

        left.add(title, subtitle);

        countSpan = new Span("—");
        countSpan.getStyle()
                .set("padding", "6px 10px")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "999px")
                .set("font-weight", "800")
                .set("background", "var(--lumo-base-color)");

        header.add(left, countSpan);
        return header;
    }

    private Component buildToolbar() {
        HorizontalLayout bar = new HorizontalLayout();
        bar.setWidthFull();
        bar.setSpacing(true);
        bar.setAlignItems(Alignment.END);

        bar.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "16px")
                .set("padding", "14px")
                .set("background", "var(--lumo-base-color)")
                .set("box-shadow", "var(--lumo-box-shadow-s)");

        searchField = new TextField();
        searchField.setLabel("Recherche");
        searchField.setPlaceholder("Titre, ville, organisateur…");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setWidth("360px");
        searchField.addValueChangeListener(e -> applyFilters());

        categoryFilter = new ComboBox<>("Catégorie");
        categoryFilter.setItems(EventCategory.values());
        categoryFilter.setItemLabelGenerator(cat -> cat.getIcon() + " " + cat.getLabel());
        categoryFilter.setClearButtonVisible(true);
        categoryFilter.setWidth("220px");
        categoryFilter.addValueChangeListener(e -> applyFilters());

        statusFilter = new ComboBox<>("Statut");
        statusFilter.setItems(EventStatus.values());
        statusFilter.setClearButtonVisible(true);
        statusFilter.setWidth("180px");
        statusFilter.addValueChangeListener(e -> applyFilters());

        Button reset = new Button("Réinitialiser", VaadinIcon.ROTATE_LEFT.create());
        reset.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        reset.addClickListener(e -> {
            searchField.clear();
            categoryFilter.clear();
            statusFilter.clear();
            applyFilters();
        });

        Button refresh = new Button("Actualiser", VaadinIcon.REFRESH.create());
        refresh.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        refresh.addClickListener(e -> loadEvents());

        bar.add(searchField, categoryFilter, statusFilter, reset, refresh);
        bar.expand(searchField);
        return bar;
    }

    private Component buildGridCard() {
        VerticalLayout card = new VerticalLayout();
        card.setSizeFull();
        card.setPadding(false);
        card.setSpacing(false);

        card.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "18px")
                .set("overflow", "hidden")
                .set("background", "var(--lumo-base-color)")
                .set("box-shadow", "var(--lumo-box-shadow-m)");

        grid = new Grid<>(Event.class, false);
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(Event::getId)
                .setHeader("ID")
                .setWidth("80px")
                .setFlexGrow(0);

        grid.addColumn(Event::getTitre)
                .setHeader("Titre")
                .setAutoWidth(true)
                .setFlexGrow(2);

        grid.addColumn(e -> e.getCategorie() != null ? (e.getCategorie().getIcon() + " " + e.getCategorie().getLabel()) : "—")
                .setHeader("Catégorie")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addColumn(e -> e.getDateDebut() != null ? e.getDateDebut().format(dtf) : "—")
                .setHeader("Début")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addColumn(e -> safe(e.getVille()) + " — " + safe(e.getLieu()))
                .setHeader("Lieu")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addColumn(e -> e.getPrixUnitaire() != null ? String.format("%.2f DH", e.getPrixUnitaire()) : "—")
                .setHeader("Prix")
                .setWidth("120px")
                .setFlexGrow(0);

        grid.addColumn(e -> (e.getCapaciteMax() != null ? e.getCapaciteMax() : 0) + " places")
                .setHeader("Capacité")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addComponentColumn(this::statusPill)
                .setHeader("Statut")
                .setWidth("180px")
                .setFlexGrow(0);

        grid.addColumn(e -> {
                    try {
                        if (e.getOrganisateur() != null) {
                            return safe(e.getOrganisateur().getPrenom()) + " " + safe(e.getOrganisateur().getNom());
                        }
                    } catch (Exception ignored) {}
                    return "—";
                })
                .setHeader("Organisateur")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addComponentColumn(this::actionsCell)
                .setHeader("Actions")
                .setAutoWidth(true)
                .setFlexGrow(0);

        // Optional: visually dim cancelled/finished
        grid.setClassNameGenerator(e -> {
            if (e == null || e.getStatut() == null) return "";
            if (e.getStatut() == EventStatus.ANNULE || e.getStatut() == EventStatus.TERMINE) return "evt-row-muted";
            return "";
        });

        card.add(grid);
        return card;
    }

    // ---------- DATA ----------

    private void loadEvents() {
        // Must fetch organisateur to avoid lazy exceptions
        allEvents = eventRepository.findAllWithOrganisateur();
        dataProvider = new ListDataProvider<>(allEvents);
        grid.setDataProvider(dataProvider);
        applyFilters();
    }

    private void applyFilters() {
        if (dataProvider == null) return;

        String q = searchField.getValue() != null ? searchField.getValue().trim().toLowerCase() : "";
        EventCategory cat = categoryFilter.getValue();
        EventStatus st = statusFilter.getValue();

        dataProvider.clearFilters();

        dataProvider.addFilter(e -> {
            boolean okQ = true;
            if (!q.isEmpty()) {
                String org = "";
                if (e.getOrganisateur() != null) {
                    org = (safe(e.getOrganisateur().getPrenom()) + " " + safe(e.getOrganisateur().getNom())).toLowerCase();
                }
                okQ =
                        safe(e.getTitre()).toLowerCase().contains(q)
                                || safe(e.getVille()).toLowerCase().contains(q)
                                || safe(e.getLieu()).toLowerCase().contains(q)
                                || org.contains(q);
            }

            boolean okCat = (cat == null) || (e.getCategorie() == cat);
            boolean okSt = (st == null) || (e.getStatut() == st);

            return okQ && okCat && okSt;
        });

        countSpan.setText(dataProvider.getItems().size() + " événement(s)");
        // countSpan currently shows total
    }

    // ---------- CELLS ----------

    private Component statusPill(Event e) {
        EventStatus st = e.getStatut();
        String label = st != null ? st.name() : "—";

        Span pill = new Span(label);
        pill.getStyle()
                .set("padding", "4px 10px")
                .set("border-radius", "999px")
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("font-weight", "800")
                .set("font-size", "0.80rem");

        if (st == EventStatus.PUBLIE) {
            pill.getStyle().set("background", "var(--lumo-success-color-10pct)")
                    .set("border-color", "var(--lumo-success-color-50pct)");
        } else if (st == EventStatus.BROUILLON) {
            pill.getStyle().set("background", "var(--lumo-warning-color-10pct)")
                    .set("border-color", "var(--lumo-warning-color-50pct)");
        } else if (st == EventStatus.ANNULE) {
            pill.getStyle().set("background", "var(--lumo-error-color-10pct)")
                    .set("border-color", "var(--lumo-error-color-50pct)");
        } else {
            pill.getStyle().set("background", "var(--lumo-contrast-5pct)")
                    .set("border-color", "var(--lumo-contrast-20pct)")
                    .set("color", "var(--lumo-secondary-text-color)");
        }

        return pill;
    }

    private Component actionsCell(Event e) {
        boolean isPublished = e.getStatut() == EventStatus.PUBLIE;

        Button view = iconBtn("Voir", VaadinIcon.EYE, ButtonVariant.LUMO_TERTIARY);
        view.addClickListener(ev -> UI.getCurrent().navigate("event/" + e.getId()));

        Button edit = iconBtn("Modifier", VaadinIcon.EDIT, ButtonVariant.LUMO_TERTIARY);
        edit.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        edit.setEnabled(e.getStatut() != EventStatus.PUBLIE);
        edit.addClickListener(ev -> openEditDialog(e));

        Button publish = iconBtn("Publier", VaadinIcon.UPLOAD, ButtonVariant.LUMO_PRIMARY);
        publish.setEnabled(e.getStatut() == EventStatus.BROUILLON);
        publish.addClickListener(ev -> confirmStatusChange(e, EventStatus.PUBLIE,
                "Publier l’événement ?",
                "Une fois publié, vous ne pourrez plus changer son statut (règle admin)."));

        Button cancel = iconBtn("Annuler", VaadinIcon.CLOSE_CIRCLE, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        // rule: if published, admin cannot modify status => disable cancel
        cancel.setEnabled(!isPublished && e.getStatut() != EventStatus.ANNULE && e.getStatut() != EventStatus.TERMINE);
        cancel.addClickListener(ev -> confirmStatusChange(e, EventStatus.ANNULE,
                "Annuler l’événement ?",
                "Cette action va annuler l’événement."));

        Button delete = iconBtn("Supprimer", VaadinIcon.TRASH, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        // Usually we don’t allow deleting published events either
        delete.setEnabled(!isPublished);
        delete.addClickListener(ev -> confirmDelete(e));

        HorizontalLayout row = new HorizontalLayout(view, edit, publish, cancel, delete);
        row.setSpacing(true);
        row.getStyle().set("flex-wrap", "wrap");
        return row;
    }

    private Button iconBtn(String text, VaadinIcon icon, ButtonVariant... variants) {
        Button b = new Button(text, icon.create());
        b.addThemeVariants(variants);
        b.getStyle().set("border-radius", "12px");
        return b;
    }

    // ---------- ACTIONS ----------

    private void confirmStatusChange(Event e, EventStatus target, String header, String body) {
        // Core rule: published cannot change status
        if (e.getStatut() == EventStatus.PUBLIE) {
            showError("Impossible: un événement publié ne peut plus changer de statut.");
            return;
        }

        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(header);
        dialog.setText(body);

        dialog.setCancelable(true);
        dialog.setConfirmText("Confirmer");
        dialog.setConfirmButtonTheme(target == EventStatus.ANNULE ? "error primary" : "primary");

        dialog.addConfirmListener(ev -> {
            try {
                eventService.changeEventStatus(e.getId(), target);
                showSuccess("Statut mis à jour: " + target);
                loadEvents();
            } catch (Exception ex) {
                showError(ex.getMessage() != null ? ex.getMessage() : "Erreur lors de la mise à jour du statut");
            }
        });

        dialog.open();
    }

    private void confirmDelete(Event e) {
        if (e.getStatut() == EventStatus.PUBLIE) {
            showError("Impossible: un événement publié ne peut pas être supprimé.");
            return;
        }

        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Supprimer l’événement ?");
        dialog.setText("Cette action est irréversible.");

        dialog.setCancelable(true);
        dialog.setConfirmText("Supprimer");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(ev -> {
            try {
                eventService.deleteEvent(e.getId());
                showSuccess("Événement supprimé.");
                loadEvents();
            } catch (Exception ex) {
                showError(ex.getMessage() != null ? ex.getMessage() : "Erreur lors de la suppression");
            }
        });

        dialog.open();
    }

    private String safe(String v) {
        return (v == null || v.isBlank()) ? "—" : v;
    }

    private void showSuccess(String message) {
        Notification.show(message, 2500, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String message) {
        Notification.show(message, 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }


    private void openEditDialog(Event event) {
    // Optional: block editing published events
    if (event.getStatut() == EventStatus.PUBLIE) {
        showError("Impossible de modifier un événement publié.");
        return;
    }

    Dialog dialog = new com.vaadin.flow.component.dialog.Dialog();
    dialog.setHeaderTitle("Modifier l’événement");

    // Create a PATCH object 
    Event patch = new Event();
    patch.setTitre(event.getTitre());
    patch.setDescription(event.getDescription());
    patch.setCategorie(event.getCategorie());
    patch.setDateDebut(event.getDateDebut());
    patch.setDateFin(event.getDateFin());
    patch.setLieu(event.getLieu());
    patch.setVille(event.getVille());
    patch.setCapaciteMax(event.getCapaciteMax());
    patch.setPrixUnitaire(event.getPrixUnitaire());
    // do NOT set imageUrl => no photo change

    // Fields
    TextField titre = new TextField("Titre");
    titre.setWidthFull();

    TextArea description = new TextArea("Description");
    description.setWidthFull();
    description.setMaxHeight("160px");

    ComboBox<EventCategory> categorie = new ComboBox<>("Catégorie");
    categorie.setItems(EventCategory.values());
    categorie.setItemLabelGenerator(c -> c.getIcon() + " " + c.getLabel());
    categorie.setWidthFull();

    DateTimePicker dateDebut =
            new DateTimePicker("Date début");
    dateDebut.setWidthFull();

    DateTimePicker dateFin =
            new DateTimePicker("Date fin");
    dateFin.setWidthFull();

    TextField ville = new TextField("Ville");
    ville.setWidthFull();

    TextField lieu = new TextField("Lieu");
    lieu.setWidthFull();

    IntegerField capacite = new IntegerField("Capacité max");
    capacite.setMin(1);
    capacite.setStepButtonsVisible(true);
    capacite.setWidthFull();

    NumberField prix = new NumberField("Prix unitaire (DH)");
    prix.setMin(0);
    prix.setWidthFull();

    // Binder
    Binder<Event> binder = new Binder<>(Event.class);

    binder.forField(titre)
            .asRequired("Titre obligatoire")
            .bind(Event::getTitre, Event::setTitre);

    binder.forField(description)
            .bind(Event::getDescription, Event::setDescription);

    binder.forField(categorie)
            .asRequired("Catégorie obligatoire")
            .bind(Event::getCategorie, Event::setCategorie);

    binder.forField(dateDebut)
            .asRequired("Date début obligatoire")
            .bind(Event::getDateDebut, Event::setDateDebut);

    binder.forField(dateFin)
            .asRequired("Date fin obligatoire")
            .bind(Event::getDateFin, Event::setDateFin);

    binder.forField(ville)
            .asRequired("Ville obligatoire")
            .bind(Event::getVille, Event::setVille);

    binder.forField(lieu)
            .asRequired("Lieu obligatoire")
            .bind(Event::getLieu, Event::setLieu);

    binder.forField(capacite)
            .asRequired("Capacité obligatoire")
            .bind(Event::getCapaciteMax, Event::setCapaciteMax);

    binder.forField(prix)
            .asRequired("Prix obligatoire")
            .bind(Event::getPrixUnitaire, Event::setPrixUnitaire);

    binder.setBean(patch);

    // Layout
    FormLayout form = new FormLayout();
    form.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("700px", 2)
    );

    form.add(titre, 2);
    form.add(categorie, 2);
    form.add(dateDebut, dateFin);
    form.add(ville, lieu);
    form.add(capacite, prix);
    form.add(description, 2);

    Button save = new Button("Enregistrer", VaadinIcon.CHECK.create());
    save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    save.getStyle().set("border-radius", "12px");

    Button cancel = new Button("Annuler", VaadinIcon.CLOSE.create());
    cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    cancel.getStyle().set("border-radius", "12px");

    save.addClickListener(e -> {
        // small validation: end after start
        if (patch.getDateDebut() != null && patch.getDateFin() != null &&
                !patch.getDateFin().isAfter(patch.getDateDebut())) {
            showError("La date de fin doit être après la date de début.");
            return;
        }

        if (!binder.validate().isOk()) {
            showError("Veuillez corriger le formulaire.");
            return;
        }

        try {
            eventService.updateEvent(event.getId(), patch);
            showSuccess("Événement mis à jour.");
            dialog.close();
            loadEvents();
        } catch (Exception ex) {
            showError(ex.getMessage() != null ? ex.getMessage() : "Erreur lors de la mise à jour.");
        }
    });

    cancel.addClickListener(e -> dialog.close());

    HorizontalLayout actions = new HorizontalLayout(cancel, save);
    actions.setWidthFull();
    actions.setJustifyContentMode(JustifyContentMode.END);

    VerticalLayout content = new VerticalLayout(form, actions);
    content.setSpacing(true);
    content.setPadding(false);

    dialog.add(content);
    dialog.setWidth("900px");
    dialog.open();
}

}
