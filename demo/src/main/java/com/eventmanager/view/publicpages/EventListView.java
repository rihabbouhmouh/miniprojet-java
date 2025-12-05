package com.eventmanager.view.publicpages;

import com.eventmanager.entity.Event;
import com.eventmanager.enums.EventCategory;
import com.eventmanager.service.IEventService;
import com.eventmanager.view.MainLayout;
import com.eventmanager.security.NavigationManager;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "events", layout = MainLayout.class)
@PageTitle("Événements - Event Manager")
@AnonymousAllowed
public class EventListView extends VerticalLayout {

    private final IEventService eventService;
    private final NavigationManager navigationManager;

    private TextField searchField;
    private TextField villeField;
    private ComboBox<EventCategory> categorieCombo;
    private NumberField prixMinField;
    private NumberField prixMaxField;

    private VerticalLayout eventsContainer;

    public EventListView(IEventService eventService, NavigationManager navigationManager) {
        this.eventService = eventService;
        this.navigationManager = navigationManager;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        createHeader();
        createFilters();
        createEventsContainer();
        loadEvents(null, null, null, null, null);
    }

    private void createHeader() {
        add(new H2("📅 Tous les événements"));
    }

    private void createFilters() {
        VerticalLayout filtersSection = new VerticalLayout();
        filtersSection.setWidthFull();
        filtersSection.getStyle().set("background", "var(--lumo-contrast-5pct)").set("border-radius", "8px");

        searchField = new TextField("Recherche", "Mot-clé dans le titre…");
        villeField = new TextField("Ville", "Casablanca, Rabat…");
        categorieCombo = new ComboBox<>("Catégorie", EventCategory.values());
        prixMinField = new NumberField("Prix min");
        prixMaxField = new NumberField("Prix max");

        Button searchButton = new Button("Rechercher", VaadinIcon.SEARCH.create(), e -> applyFilters());
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button resetButton = new Button("Réinitialiser", VaadinIcon.REFRESH.create(), e -> resetFilters());

        filtersSection.add(
                new HorizontalLayout(searchField, villeField),
                new HorizontalLayout(categorieCombo, prixMinField, prixMaxField),
                new HorizontalLayout(searchButton, resetButton)
        );
        add(filtersSection);
    }

    private void createEventsContainer() {
        eventsContainer = new VerticalLayout();
        add(eventsContainer);
    }

    private void applyFilters() {
        loadEvents(
                emptyToNull(searchField.getValue()),
                emptyToNull(villeField.getValue()),
                categorieCombo.getValue() != null ? categorieCombo.getValue().name() : null,
                prixMinField.getValue(),
                prixMaxField.getValue()
        );
    }

    private void resetFilters() {
        searchField.clear();
        villeField.clear();
        categorieCombo.clear();
        prixMinField.clear();
        prixMaxField.clear();
        loadEvents(null, null, null, null, null);
    }

    private void loadEvents(String keyword, String ville, String categorie, Double prixMin, Double prixMax) {
        eventsContainer.removeAll();
        List<Event> events = (keyword == null && ville == null && categorie == null && prixMin == null && prixMax == null)
                ? eventService.getAvailableEvents()
                : eventService.getEventsWithFilters(keyword, ville, categorie, prixMin, prixMax);

        if (events.isEmpty()) {
            eventsContainer.add(new Span("Aucun événement trouvé."));
            return;
        }

        events.forEach(event -> eventsContainer.add(createEventRow(event)));
    }

    private HorizontalLayout createEventRow(Event event) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(Alignment.CENTER);
        row.getStyle().set("border", "1px solid #ccc").set("border-radius", "8px").set("padding", "10px");

        VerticalLayout info = new VerticalLayout();
        info.setSpacing(false);

        Span title = new Span(event.getTitre());
        title.getStyle().set("font-weight", "bold").set("font-size", "1.2em");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");
        Span details = new Span(String.format("%s • %s, %s",
                event.getDateDebut().format(formatter), event.getLieu(), event.getVille()));

        int places = eventService.getAvailableSeats(event);
        Span placesSpan = new Span(places + " places disponibles");
        placesSpan.getStyle().set("color", places <= 10 ? "red" : "green");

        info.add(title, details, placesSpan);

        Button detailsButton = new Button("Détails", VaadinIcon.EYE.create(), e -> navigationManager.navigateToEventDetail(event.getId()));
        detailsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        row.add(info, detailsButton);
        return row;
    }

    private String emptyToNull(String value) {
        return (value == null || value.trim().isEmpty()) ? null : value.trim();
    }
}
