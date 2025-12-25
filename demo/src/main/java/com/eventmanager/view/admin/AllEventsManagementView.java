package com.eventmanager.view.admin;

import com.eventmanager.entity.Event;
import com.eventmanager.enums.EventCategory;
import com.eventmanager.enums.EventStatus;
import com.eventmanager.repository.EventRepository;
import com.eventmanager.service.IEventService;
import com.eventmanager.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "admin/events", layout = MainLayout.class)
@PageTitle("Gestion des Événements - Admin")
public class AllEventsManagementView extends VerticalLayout {

    private final IEventService eventService;
    private final EventRepository eventRepository;
    private Grid<Event> eventGrid;
    private List<Event> allEvents;
    private ListDataProvider<Event> dataProvider;
    private TextField searchField;
    private ComboBox<EventCategory> categoryFilter;
    private ComboBox<EventStatus> statusFilter;

    public AllEventsManagementView(IEventService eventService, EventRepository eventRepository) {
        this.eventService = eventService;
        this.eventRepository = eventRepository;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Header
        H2 title = new H2("🎭 Gestion de Tous les Événements");
        title.addClassNames(LumoUtility.FontSize.XXXLARGE, LumoUtility.Margin.Bottom.LARGE);
        add(title);

        // Filters
        add(createFiltersSection());

        // Grid
        add(createEventGrid());

        // Load data
        loadEvents();
    }

    private HorizontalLayout createFiltersSection() {
        HorizontalLayout filters = new HorizontalLayout();
        filters.setWidthFull();
        filters.setSpacing(true);
        filters.setAlignItems(Alignment.END);

        // Search field
        searchField = new TextField();
        searchField.setPlaceholder("Rechercher par titre...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setWidth("300px");
        searchField.addValueChangeListener(e -> filterEvents());

        // Category filter
        categoryFilter = new ComboBox<>("Catégorie");
        categoryFilter.setItems(EventCategory.values());
        categoryFilter.setItemLabelGenerator(cat -> cat.getIcon() + " " + cat.getLabel());
        categoryFilter.setClearButtonVisible(true);
        categoryFilter.setWidth("200px");
        categoryFilter.addValueChangeListener(e -> filterEvents());

        // Status filter
        statusFilter = new ComboBox<>("Statut");
        statusFilter.setItems(EventStatus.values());
        statusFilter.setClearButtonVisible(true);
        statusFilter.setWidth("150px");
        statusFilter.addValueChangeListener(e -> filterEvents());

        // Refresh button
        Button refreshButton = new Button("Actualiser", VaadinIcon.REFRESH.create());
        refreshButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        refreshButton.addClickListener(e -> loadEvents());

        filters.add(searchField, categoryFilter, statusFilter, refreshButton);
        filters.expand(searchField);

        return filters;
    }

    private Grid<Event> createEventGrid() {
        eventGrid = new Grid<>(Event.class, false);
        eventGrid.setSizeFull();

        eventGrid.addColumn(Event::getId).setHeader("ID").setWidth("80px").setFlexGrow(0);
        eventGrid.addColumn(Event::getTitre).setHeader("Titre").setAutoWidth(true);
        eventGrid.addColumn(event -> event.getCategorie().getIcon() + " " + event.getCategorie().getLabel())
                .setHeader("Catégorie").setAutoWidth(true);
        eventGrid.addColumn(event -> event.getDateDebut()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .setHeader("Date de début").setAutoWidth(true);
        eventGrid.addColumn(event -> event.getVille() + " - " + event.getLieu())
                .setHeader("Lieu").setAutoWidth(true);
        eventGrid.addColumn(event -> String.format("%.2f DH", event.getPrixUnitaire()))
                .setHeader("Prix").setAutoWidth(true);
        eventGrid.addColumn(event -> event.getCapaciteMax() + " places")
                .setHeader("Capacité").setAutoWidth(true);
        eventGrid.addColumn(event -> {
            String status = event.getStatut().toString();
            String color = switch (event.getStatut()) {
                case PUBLIE -> "#4caf50";
                case BROUILLON -> "#ff9800";
                case ANNULE -> "#f44336";
                case TERMINE -> "#9e9e9e";
            };
            Span statusSpan = new Span(status);
            statusSpan.getStyle()
                    .set("padding", "4px 8px")
                    .set("border-radius", "4px")
                    .set("background", color)
                    .set("color", "white")
                    .set("font-size", "0.75rem");
            return statusSpan;
        }).setHeader("Statut").setAutoWidth(true);
        eventGrid.addColumn(event -> {
            try {
                if (event.getOrganisateur() != null) {
                    return event.getOrganisateur().getPrenom() + " " + event.getOrganisateur().getNom();
                }
                return "N/A";
            } catch (Exception e) {
                return "N/A";
            }
        }).setHeader("Organisateur").setAutoWidth(true);

        // Actions column
        eventGrid.addComponentColumn(event -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);

            // View button
            Button viewButton = new Button("Voir", VaadinIcon.EYE.create());
            viewButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            viewButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("event/" + event.getId())));

            // Change status button
            Button statusButton = new Button("Statut", VaadinIcon.COG.create());
            statusButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            statusButton.addClickListener(e -> changeEventStatus(event));

            actions.add(viewButton, statusButton);
            return actions;
        }).setHeader("Actions").setAutoWidth(true);

        eventGrid.setAllRowsVisible(true);
        return eventGrid;
    }

    private void loadEvents() {
        // Use repository query that eagerly loads organisateur to avoid LazyInitializationException
        allEvents = eventRepository.findAllWithOrganisateur();
        dataProvider = new ListDataProvider<>(allEvents);
        eventGrid.setDataProvider(dataProvider);
    }

    private void filterEvents() {
        String search = searchField.getValue();
        EventCategory category = categoryFilter.getValue();
        EventStatus status = statusFilter.getValue();

        List<Event> filtered = allEvents;

        if (search != null && !search.isEmpty()) {
            String lowerSearch = search.toLowerCase();
            filtered = filtered.stream()
                    .filter(e -> e.getTitre().toLowerCase().contains(lowerSearch))
                    .toList();
        }

        if (category != null) {
            filtered = filtered.stream()
                    .filter(e -> e.getCategorie() == category)
                    .toList();
        }

        if (status != null) {
            filtered = filtered.stream()
                    .filter(e -> e.getStatut() == status)
                    .toList();
        }

        dataProvider = new ListDataProvider<>(filtered);
        eventGrid.setDataProvider(dataProvider);
    }

    private void changeEventStatus(Event event) {
        ComboBox<EventStatus> statusCombo = new ComboBox<>("Nouveau statut");
        statusCombo.setItems(EventStatus.values());
        statusCombo.setValue(event.getStatut());

        VerticalLayout dialogContent = new VerticalLayout(statusCombo);
        dialogContent.setSpacing(true);

        com.vaadin.flow.component.dialog.Dialog dialog = new com.vaadin.flow.component.dialog.Dialog();
        dialog.setHeaderTitle("Changer le statut de: " + event.getTitre());

        Button saveButton = new Button("Enregistrer", VaadinIcon.CHECK.create());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> {
            try {
                eventService.changeEventStatus(event.getId(), statusCombo.getValue());
                showSuccess("Statut mis à jour avec succès");
                dialog.close();
                loadEvents();
            } catch (Exception ex) {
                showError("Erreur: " + ex.getMessage());
            }
        });

        Button cancelButton = new Button("Annuler", VaadinIcon.CLOSE.create());
        cancelButton.addClickListener(e -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        dialogContent.add(buttons);
        dialog.add(dialogContent);
        dialog.open();
    }

    private void showSuccess(String message) {
        Notification.show(message, 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String message) {
        Notification.show(message, 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}