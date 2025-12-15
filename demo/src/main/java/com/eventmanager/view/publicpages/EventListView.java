package com.eventmanager.view.publicpages;

import com.eventmanager.entity.Event;
import com.eventmanager.enums.EventCategory;
import com.eventmanager.service.IEventService;
import com.eventmanager.view.MainLayout;
import com.eventmanager.security.NavigationManager;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "events", layout = MainLayout.class)
@AnonymousAllowed
public class EventListView extends VerticalLayout {

    private final IEventService eventService;
    private final NavigationManager navigationManager;

    private TextField searchField;
    private TextField villeField;
    private ComboBox<EventCategory> categorieCombo;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private NumberField prixMinField;
    private NumberField prixMaxField;
    private ComboBox<String> sortCombo;

    private Div eventsGrid;
    private Span resultCount;
    private HorizontalLayout paginationLayout;

    private int currentPage = 0;
    private final int pageSize = 12;
    private List<Event> allFilteredEvents;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public EventListView(IEventService eventService, NavigationManager navigationManager) {
        this.eventService = eventService;
        this.navigationManager = navigationManager;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background", "#f5f5f7");

        add(
                createHeader(),
                createFiltersSection(),
                createMainContent()
        );

        loadEvents();
    }

    private Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setPadding(true);
        header.getStyle()
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("color", "white")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)");

        Icon icon = VaadinIcon.CALENDAR.create();
        icon.setSize("32px");

        H1 title = new H1("📅 Browse Events");
        title.getStyle().set("margin", "0");

        header.add(title);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        return header;
    }

    private Component createFiltersSection() {
        VerticalLayout filtersSection = new VerticalLayout();
        filtersSection.setWidthFull();
        filtersSection.setPadding(true);
        filtersSection.setSpacing(true);
        filtersSection.getStyle()
                .set("background", "white")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.08)")
                .set("max-width", "1200px")
                .set("margin", "20px auto")
                .set("border-radius", "8px");

        // Row 1
        HorizontalLayout row1 = new HorizontalLayout();
        row1.setWidthFull();

        searchField = new TextField("Search");
        searchField.setPlaceholder("Search events...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setWidthFull();
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> applyFilters());

        villeField = new TextField("City");
        villeField.setPlaceholder("City name...");
        villeField.setPrefixComponent(VaadinIcon.MAP_MARKER.create());
        villeField.setWidth("250px");
        villeField.setClearButtonVisible(true);
        villeField.addValueChangeListener(e -> applyFilters());

        row1.add(searchField, villeField);

        // Row 2
        HorizontalLayout row2 = new HorizontalLayout();
        row2.setWidthFull();

        categorieCombo = new ComboBox<>("Category");
        categorieCombo.setItems(EventCategory.values());
        categorieCombo.setItemLabelGenerator(this::getCategoryLabel);
        categorieCombo.setPlaceholder("All");
        categorieCombo.setWidth("200px");
        categorieCombo.addValueChangeListener(e -> applyFilters());

        startDatePicker = new DatePicker("From Date");
        startDatePicker.setPlaceholder("Start date");
        startDatePicker.setWidth("180px");
        startDatePicker.addValueChangeListener(e -> applyFilters());

        endDatePicker = new DatePicker("To Date");
        endDatePicker.setPlaceholder("End date");
        endDatePicker.setWidth("180px");
        endDatePicker.addValueChangeListener(e -> applyFilters());

        prixMinField = new NumberField("Min Price");
        prixMinField.setPlaceholder("0");
        prixMinField.setWidth("150px");
        prixMinField.addValueChangeListener(e -> applyFilters());

        prixMaxField = new NumberField("Max Price");
        prixMaxField.setPlaceholder("Max");
        prixMaxField.setWidth("150px");
        prixMaxField.addValueChangeListener(e -> applyFilters());

        sortCombo = new ComboBox<>("Sort");
        sortCombo.setItems("Date (Newest)", "Date (Oldest)", "Price (Low-High)", "Price (High-Low)", "Title");
        sortCombo.setValue("Date (Newest)");
        sortCombo.setWidth("170px");
        sortCombo.addValueChangeListener(e -> applySorting());

        Button resetBtn = new Button("Reset", VaadinIcon.REFRESH.create());
        resetBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        resetBtn.getStyle()
                .set("margin-top", "34px");
        resetBtn.addClickListener(e -> resetFilters());

        row1.add(categorieCombo);

        row2.add( startDatePicker, endDatePicker, prixMinField, prixMaxField, sortCombo, resetBtn);

        filtersSection.add(row1, row2);

        return filtersSection;
    }

    private Component createMainContent() {
        VerticalLayout mainContent = new VerticalLayout();
        mainContent.setSizeFull();
        mainContent.setPadding(false);
        mainContent.setSpacing(false);
        mainContent.getStyle()
                .set("max-width", "1200px")
                .set("margin", "0 auto")
                .set("padding", "0 20px");

        // Result count
        resultCount = new Span();
        resultCount.getStyle()
                .set("font-size", "0.95rem")
                .set("color", "#666")
                .set("display", "block")
                .set("margin-bottom", "16px");

        // Events grid
        eventsGrid = new Div();
        eventsGrid.setWidthFull();
        eventsGrid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fill, minmax(380px, 1fr))")
                .set("gap", "20px")
                .set("margin-bottom", "30px");

        // Pagination
        paginationLayout = createPagination();

        mainContent.add(resultCount, eventsGrid, paginationLayout);

        return mainContent;
    }

    private HorizontalLayout createPagination() {
        HorizontalLayout pagination = new HorizontalLayout();
        pagination.setWidthFull();
        pagination.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        pagination.setAlignItems(FlexComponent.Alignment.CENTER);
        pagination.setSpacing(true);
        pagination.getStyle()
                .set("padding", "20px 0")
                .set("margin-top", "20px");

        Button prevBtn = new Button(VaadinIcon.ANGLE_LEFT.create());
        prevBtn.setText("Previous");
        prevBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        prevBtn.setId("prev-btn");
        prevBtn.addClickListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                displayPage();
            }
        });

        Span pageInfo = new Span();
        pageInfo.setId("page-info");
        pageInfo.getStyle()
                .set("margin", "0 20px")
                .set("color", "#666");

        Button nextBtn = new Button(VaadinIcon.ANGLE_RIGHT.create());
        nextBtn.setText("Next");
        nextBtn.setIconAfterText(true);
        nextBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        nextBtn.setId("next-btn");
        nextBtn.addClickListener(e -> {
            int totalPages = (int) Math.ceil((double) allFilteredEvents.size() / pageSize);
            if (currentPage < totalPages - 1) {
                currentPage++;
                displayPage();
            }
        });

        pagination.add(prevBtn, pageInfo, nextBtn);

        return pagination;
    }

    private void loadEvents() {
        allFilteredEvents = eventService.getAvailableEvents();
        applySorting();
        currentPage = 0;
        displayPage();
    }

    private void applyFilters() {
        String keyword = emptyToNull(searchField.getValue());
        String ville = emptyToNull(villeField.getValue());
        EventCategory categorie = categorieCombo.getValue();
        Double prixMin = prixMinField.getValue();
        Double prixMax = prixMaxField.getValue();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        List<Event> events = eventService.getAvailableEvents();

        allFilteredEvents = events.stream()
                .filter(event -> {
                    if (keyword != null && !event.getTitre().toLowerCase().contains(keyword.toLowerCase())) return false;
                    if (ville != null && !event.getVille().toLowerCase().contains(ville.toLowerCase())) return false;
                    if (categorie != null && event.getCategorie() != categorie) return false;
                    if (prixMin != null && event.getPrixUnitaire() < prixMin) return false;
                    if (prixMax != null && event.getPrixUnitaire() > prixMax) return false;
                    if (startDate != null && event.getDateDebut().toLocalDate().isBefore(startDate)) return false;
                    if (endDate != null && event.getDateDebut().toLocalDate().isAfter(endDate)) return false;
                    return true;
                })
                .collect(Collectors.toList());

        applySorting();
        currentPage = 0;
        displayPage();
    }

    private void applySorting() {
        String sortBy = sortCombo.getValue();
        if (sortBy == null || allFilteredEvents == null) return;

        switch (sortBy) {
            case "Date (Newest)" -> allFilteredEvents.sort(Comparator.comparing(Event::getDateDebut).reversed());
            case "Date (Oldest)" -> allFilteredEvents.sort(Comparator.comparing(Event::getDateDebut));
            case "Price (Low-High)" -> allFilteredEvents.sort(Comparator.comparing(Event::getPrixUnitaire));
            case "Price (High-Low)" -> allFilteredEvents.sort(Comparator.comparing(Event::getPrixUnitaire).reversed());
            case "Title" -> allFilteredEvents.sort(Comparator.comparing(Event::getTitre));
        }

        displayPage();
    }

    private void displayPage() {
        eventsGrid.removeAll();

        if (allFilteredEvents == null || allFilteredEvents.isEmpty()) {
            eventsGrid.add(createEmptyState());
            resultCount.setText("No events found");
            updatePagination(0, 0);
            return;
        }

        int start = currentPage * pageSize;
        int end = Math.min(start + pageSize, allFilteredEvents.size());
        List<Event> pageEvents = allFilteredEvents.subList(start, end);

        pageEvents.forEach(event -> eventsGrid.add(createEventCard(event)));

        resultCount.setText(String.format("Showing %d-%d of %d events", start + 1, end, allFilteredEvents.size()));
        updatePagination(currentPage + 1, (int) Math.ceil((double) allFilteredEvents.size() / pageSize));
    }

    private void updatePagination(int current, int total) {
        Span pageInfo = (Span) paginationLayout.getComponentAt(1);
        pageInfo.setText(String.format("Page %d of %d", current, Math.max(1, total)));
    }

    private Div createEventCard(Event event) {
        Div card = new Div();
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.08)")
                .set("transition", "all 0.2s")
                .set("cursor", "pointer");

        card.getElement().addEventListener("mouseenter", e ->
                card.getStyle().set("box-shadow", "0 4px 12px rgba(0,0,0,0.15)"));
        card.getElement().addEventListener("mouseleave", e ->
                card.getStyle().set("box-shadow", "0 2px 8px rgba(0,0,0,0.08)"));

        // Content
        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);
        content.getStyle().set("padding", "20px");

        // Title
        H3 title = new H3(event.getTitre());
        title.getStyle()
                .set("margin", "0 0 16px 0")
                .set("font-size", "1.2rem")
                .set("font-weight", "600")
                .set("color", "#1a1a1a");

        // Date
        HorizontalLayout dateRow = createInfoRow(VaadinIcon.CALENDAR,
                event.getDateDebut().format(dateFormatter) + " • " + event.getDateDebut().format(timeFormatter));

        // Location
        HorizontalLayout locationRow = createInfoRow(VaadinIcon.MAP_MARKER, event.getVille());

        // Seats
        int availableSeats = eventService.getAvailableSeats(event);
        String seatsColor = availableSeats > 50 ? "#28a745" : (availableSeats > 10 ? "#ffc107" : "#dc3545");
        HorizontalLayout seatsRow = createInfoRow(VaadinIcon.USERS, availableSeats + " seats available");
        seatsRow.getChildren().forEach(c -> {
            if (c instanceof Span) {
                ((Span) c).getStyle().set("color", seatsColor);
            }
        });

        content.add(title, dateRow, locationRow, seatsRow);

        // Footer
        HorizontalLayout footer = new HorizontalLayout();
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        footer.setAlignItems(FlexComponent.Alignment.CENTER);
        footer.getStyle()
                .set("padding", "16px 20px 20px 20px");

        Span price = new Span(String.format("%.2f DH", event.getPrixUnitaire()));
        price.getStyle()
                .set("font-size", "1.5rem")
                .set("font-weight", "700")
                .set("color", "#007bff");

        Button detailsBtn = new Button("View Details", VaadinIcon.ARROW_RIGHT.create());
        detailsBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        detailsBtn.setIconAfterText(true);
        detailsBtn.addClickListener(e -> navigationManager.navigateToEventDetail(event.getId()));

        footer.add(price, detailsBtn);

        card.add(content, footer);

        return card;
    }

    private HorizontalLayout createInfoRow(VaadinIcon iconType, String text) {
        HorizontalLayout row = new HorizontalLayout();
        row.setSpacing(true);
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.getStyle().set("margin-bottom", "8px");

        Icon icon = iconType.create();
        icon.setSize("16px");
        icon.getStyle().set("color", "#666");

        Span span = new Span(text);
        span.getStyle()
                .set("font-size", "0.9rem")
                .set("color", "#666");

        row.add(icon, span);
        return row;
    }

    private Div createEmptyState() {
        Div empty = new Div();
        empty.getStyle()
                .set("text-align", "center")
                .set("padding", "60px 20px")
                .set("grid-column", "1 / -1");

        H3 message = new H3("📭 No events found");
        message.getStyle().set("color", "#999");

        empty.add(message);
        return empty;
    }

    private void resetFilters() {
        searchField.clear();
        villeField.clear();
        categorieCombo.clear();
        startDatePicker.clear();
        endDatePicker.clear();
        prixMinField.clear();
        prixMaxField.clear();
        sortCombo.setValue("Date (Newest)");
        loadEvents();
    }

    private String emptyToNull(String value) {
        return (value == null || value.trim().isEmpty()) ? null : value.trim();
    }

    private String getCategoryLabel(EventCategory category) {
        return switch (category) {
            case CONCERT -> "🎵 Concert";
            case THEATRE -> "🎭 Theatre";
            case CONFERENCE -> "🎤 Conference";
            case SPORT -> "⚽ Sport";
            case AUTRE -> "🎪 Other";
        };
    }
}