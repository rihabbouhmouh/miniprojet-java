package com.eventmanager.view.publicpages;

import com.eventmanager.entity.Event;
import com.eventmanager.enums.EventCategory;
import com.eventmanager.security.NavigationManager;
import com.eventmanager.service.IEventService;
import com.eventmanager.view.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Route(value = "events", layout = MainLayout.class)
@PageTitle("Événements - EventHub")
@AnonymousAllowed
public class EventListView extends VerticalLayout {

    private final IEventService eventService;
    private final NavigationManager navigationManager;

    // Filters
    private TextField searchField;
    private ComboBox<String> cityCombo;
    private ComboBox<EventCategory> categoryCombo;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private NumberField priceMinField;
    private NumberField priceMaxField;
    private ComboBox<String> sortCombo;

    // View mode
    private Tabs viewTabs;
    private Tab tabCards;
    private Tab tabGrid;

    // Content
    private Span resultCount;
    private Div cardsGrid;
    private Grid<Event> grid;

    // Pagination
    private HorizontalLayout paginationLayout;
    private Button prevBtn;
    private Button nextBtn;
    private Span pageInfo;
    private ComboBox<Integer> pageSizeCombo;

    private int currentPage = 0;
    private int pageSize = 12;

    private List<Event> allEvents = new ArrayList<>();
    private List<Event> filteredSorted = new ArrayList<>();

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public EventListView(IEventService eventService, NavigationManager navigationManager) {
        this.eventService = eventService;
        this.navigationManager = navigationManager;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background", "#f5f7fa");

        add(
                createHeader(),
                createFiltersCard(),
                createMainContent()
        );

        loadAllEvents();
    }

    /* ============================ UI SECTIONS ============================ */

    private Component createHeader() {
        Div header = new Div();
        header.setWidthFull();
        header.getStyle()
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("color", "white")
                .set("padding", "36px 20px")
                .set("box-shadow", "0 8px 24px rgba(0,0,0,0.08)");

        Div content = new Div();
        content.getStyle()
                .set("max-width", "1200px")
                .set("margin", "0 auto")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "space-between")
                .set("gap", "16px")
                .set("flex-wrap", "wrap");

        HorizontalLayout left = new HorizontalLayout();
        left.setSpacing(true);
        left.setAlignItems(FlexComponent.Alignment.CENTER);

        Icon icon = VaadinIcon.CALENDAR.create();
        icon.setSize("34px");

        H1 title = new H1("Explorer les événements");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "2.1rem")
                .set("font-weight", "700");

        left.add(icon, title);

        // View mode tabs
        tabCards = new Tab("Cards");
        tabGrid = new Tab("Grid");
        viewTabs = new Tabs(tabCards, tabGrid);
        viewTabs.setSelectedTab(tabCards);
        viewTabs.getStyle()
                .set("background", "rgba(255, 255, 255, 1)")
                .set("border-radius", "12px")
                .set("padding", "6px");

        viewTabs.addSelectedChangeListener(e -> displayPage());

        content.add(left, viewTabs);
        header.add(content);
        return header;
    }

    private Component createFiltersCard() {
        VerticalLayout wrapper = new VerticalLayout();
        wrapper.setWidthFull();
        wrapper.setPadding(false);
        wrapper.setSpacing(false);
        wrapper.getStyle().set("padding", "0 20px");

        Div card = new Div();
        card.getStyle()
                .set("max-width", "1200px")
                .set("margin", "-18px auto 0 auto")
                .set("background", "white")
                .set("border-radius", "16px")
                .set("box-shadow", "0 10px 30px rgba(0,0,0,0.08)")
                .set("padding", "18px");

        // Row 1: keyword + city + category
        HorizontalLayout row1 = new HorizontalLayout();
        row1.setWidthFull();
        row1.setSpacing(true);
        row1.getStyle().set("flex-wrap", "wrap");

        searchField = new TextField("Mot-clé");
        searchField.setPlaceholder("Titre, mot-clé...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setWidth("420px");
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> applyFilters());

        cityCombo = new ComboBox<>("Ville");
        cityCombo.setPlaceholder("Casablanca, Rabat...");
        cityCombo.setClearButtonVisible(true);
        cityCombo.setAllowCustomValue(true); // ✅ allow typing cities not in list
        cityCombo.setWidth("260px");
        cityCombo.addValueChangeListener(e -> applyFilters());
        cityCombo.addCustomValueSetListener(e -> {
            cityCombo.setValue(e.getDetail());
            applyFilters();
        });

        categoryCombo = new ComboBox<>("Catégorie");
        categoryCombo.setItems(EventCategory.values());
        categoryCombo.setItemLabelGenerator(this::getCategoryLabel);
        categoryCombo.setClearButtonVisible(true);
        categoryCombo.setWidth("220px");
        categoryCombo.addValueChangeListener(e -> applyFilters());

        row1.add(searchField, cityCombo, categoryCombo);

        // Row 2: dates + price + sort + buttons
        HorizontalLayout row2 = new HorizontalLayout();
        row2.setWidthFull();
        row2.setSpacing(true);
        row2.setAlignItems(Alignment.END);
        row2.getStyle().set("flex-wrap", "wrap");

        startDatePicker = new DatePicker("Du");
        startDatePicker.setWidth("170px");
        startDatePicker.addValueChangeListener(e -> applyFilters());

        endDatePicker = new DatePicker("Au");
        endDatePicker.setWidth("170px");
        endDatePicker.addValueChangeListener(e -> applyFilters());

        priceMinField = new NumberField("Prix min");
        priceMinField.setPlaceholder("0");
        priceMinField.setMin(0);
        priceMinField.setWidth("150px");
        priceMinField.addValueChangeListener(e -> applyFilters());

        priceMaxField = new NumberField("Prix max");
        priceMaxField.setPlaceholder("500");
        priceMaxField.setMin(0);
        priceMaxField.setWidth("150px");
        priceMaxField.addValueChangeListener(e -> applyFilters());

        sortCombo = new ComboBox<>("Trier par");
        sortCombo.setItems(
                "Date (récent)",
                "Date (ancien)",
                "Prix (croissant)",
                "Prix (décroissant)",
                "Popularité",
                "Titre"
        );
        sortCombo.setValue("Date (récent)");
        sortCombo.setWidth("200px");
        sortCombo.addValueChangeListener(e -> applySorting());

        Button resetBtn = new Button("Réinitialiser", VaadinIcon.REFRESH.create());
        resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        resetBtn.addClickListener(e -> resetFilters());

        Button refreshBtn = new Button("Actualiser", VaadinIcon.ROTATE_RIGHT.create());
        refreshBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        refreshBtn.addClickListener(e -> loadAllEvents());

        row2.add(startDatePicker, endDatePicker, priceMinField, priceMaxField, sortCombo, resetBtn, refreshBtn);

        card.add(row1, row2);
        wrapper.add(card);
        return wrapper;
    }

    private Component createMainContent() {
        VerticalLayout main = new VerticalLayout();
        main.setSizeFull();
        main.setPadding(false);
        main.setSpacing(false);
        main.getStyle()
                .set("max-width", "1200px")
                .set("margin", "0 auto")
                .set("padding", "20px");

        resultCount = new Span();
        resultCount.getStyle()
                .set("display", "block")
                .set("margin", "12px 0 16px 0")
                .set("color", "#666")
                .set("font-size", "0.95rem");

        // Cards container
        cardsGrid = new Div();
        cardsGrid.setWidthFull();
        cardsGrid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(3, minmax(0, 1fr))")
                .set("gap", "18px");

        // Make it responsive
        // (Vaadin inline style can’t do media queries, but grid auto-wrap works if we switch template)
        // We'll keep 3 columns desktop; on smaller screens it will overflow less if browser reduces.
        cardsGrid.getStyle().set("grid-template-columns", "repeat(auto-fill, minmax(320px, 1fr))");

        // Vaadin Grid (table view)
        grid = createGrid();
        grid.setVisible(false);

        paginationLayout = createPaginationBar();

        main.add(resultCount, cardsGrid, grid, paginationLayout);
        main.expand(cardsGrid);
        return main;
    }

    private Grid<Event> createGrid() {
        Grid<Event> g = new Grid<>(Event.class, false);
        g.setWidthFull();
        g.setHeight("600px"); // ✅ important: if you want it taller, increase this
        g.getStyle()
                .set("background", "white")
                .set("border-radius", "14px")
                .set("box-shadow", "0 8px 22px rgba(0,0,0,0.08)");

        g.addColumn(Event::getTitre).setHeader("Titre").setAutoWidth(true).setFlexGrow(2);
        g.addColumn(e -> getCategoryLabel(e.getCategorie())).setHeader("Catégorie").setAutoWidth(true);
        g.addColumn(Event::getVille).setHeader("Ville").setAutoWidth(true);
        g.addColumn(e -> e.getDateDebut() != null
                        ? e.getDateDebut().format(dateFormatter) + " " + e.getDateDebut().format(timeFormatter)
                        : "—")
                .setHeader("Date").setAutoWidth(true);

        g.addColumn(e -> e.getPrixUnitaire() != null ? String.format("%.2f DH", e.getPrixUnitaire()) : "—")
                .setHeader("Prix").setAutoWidth(true);

        g.addColumn(e -> {
            try {
                return eventService.getAvailableSeats(e) + " places";
            } catch (Exception ex) {
                return "—";
            }
        }).setHeader("Disponibilité").setAutoWidth(true);

        g.addComponentColumn(e -> {
            Button btn = new Button("Voir détails", VaadinIcon.ARROW_RIGHT.create());
            btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            btn.setIconAfterText(true);
            btn.addClickListener(x -> navigationManager.navigateToEventDetail(e.getId()));
            return btn;
        }).setHeader("Action").setAutoWidth(true).setFlexGrow(0);

        return g;
    }

    private HorizontalLayout createPaginationBar() {
        HorizontalLayout bar = new HorizontalLayout();
        bar.setWidthFull();
        bar.setJustifyContentMode(JustifyContentMode.CENTER);
        bar.setAlignItems(Alignment.CENTER);
        bar.setSpacing(true);
        bar.getStyle().set("padding", "18px 0");

        prevBtn = new Button("Précédent", VaadinIcon.ANGLE_LEFT.create());
        prevBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        prevBtn.addClickListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                displayPage();
            }
        });

        pageInfo = new Span();
        pageInfo.getStyle().set("color", "#666");

        nextBtn = new Button("Suivant", VaadinIcon.ANGLE_RIGHT.create());
        nextBtn.setIconAfterText(true);
        nextBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        nextBtn.addClickListener(e -> {
            int totalPages = getTotalPages();
            if (currentPage < totalPages - 1) {
                currentPage++;
                displayPage();
            }
        });

        pageSizeCombo = new ComboBox<>();
        pageSizeCombo.setItems(6, 12, 18, 24);
        pageSizeCombo.setValue(pageSize);
        pageSizeCombo.setWidth("120px");
        pageSizeCombo.setHelperText("Par page");
        pageSizeCombo.addValueChangeListener(e -> {
            if (e.getValue() != null && e.getValue() > 0) {
                pageSize = e.getValue();
                currentPage = 0;
                displayPage();
            }
        });

        bar.add(prevBtn, pageInfo, nextBtn, pageSizeCombo);
        return bar;
    }

    /* ============================ DATA / FILTERING ============================ */

    private void loadAllEvents() {
        allEvents = new ArrayList<>(eventService.getAvailableEvents());

        // ✅ City autocomplete items
        List<String> cities = allEvents.stream()
                .map(Event::getVille)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        cityCombo.setItems(cities);

        applyFilters();
    }

    private void applyFilters() {
        String keyword = emptyToNull(searchField.getValue());
        String city = emptyToNull(cityCombo.getValue());
        EventCategory category = categoryCombo.getValue();
        Double minPrice = priceMinField.getValue();
        Double maxPrice = priceMaxField.getValue();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        filteredSorted = allEvents.stream()
                .filter(e -> {
                    if (keyword != null) {
                        String t = safe(e.getTitre()).toLowerCase();
                        String d = safe(e.getDescription()).toLowerCase();
                        String k = keyword.toLowerCase();
                        if (!t.contains(k) && !d.contains(k)) return false;
                    }

                    if (city != null) {
                        if (!safe(e.getVille()).toLowerCase().contains(city.toLowerCase())) return false;
                    }

                    if (category != null && e.getCategorie() != category) return false;

                    if (minPrice != null && e.getPrixUnitaire() != null && e.getPrixUnitaire() < minPrice) return false;
                    if (maxPrice != null && e.getPrixUnitaire() != null && e.getPrixUnitaire() > maxPrice) return false;

                    if (startDate != null && e.getDateDebut() != null &&
                            e.getDateDebut().toLocalDate().isBefore(startDate)) return false;

                    if (endDate != null && e.getDateDebut() != null &&
                            e.getDateDebut().toLocalDate().isAfter(endDate)) return false;

                    return true;
                })
                .collect(Collectors.toList());

        applySorting(); // calls displayPage
        currentPage = 0;
        displayPage();
    }

    private void applySorting() {
        if (sortCombo.getValue() == null) return;

        switch (sortCombo.getValue()) {
            case "Date (récent)" -> filteredSorted.sort(Comparator.comparing(Event::getDateDebut,
                    Comparator.nullsLast(Comparator.naturalOrder())).reversed());

            case "Date (ancien)" -> filteredSorted.sort(Comparator.comparing(Event::getDateDebut,
                    Comparator.nullsLast(Comparator.naturalOrder())));

            case "Prix (croissant)" -> filteredSorted.sort(Comparator.comparing(Event::getPrixUnitaire,
                    Comparator.nullsLast(Comparator.naturalOrder())));

            case "Prix (décroissant)" -> filteredSorted.sort(Comparator.comparing(Event::getPrixUnitaire,
                    Comparator.nullsLast(Comparator.naturalOrder())).reversed());

            case "Titre" -> filteredSorted.sort(Comparator.comparing(e -> safe(e.getTitre()).toLowerCase()));

            case "Popularité" -> {
                // ✅ Popularity approximation using remaining seats (less remaining = more popular)
                filteredSorted.sort(Comparator.comparingInt(e -> {
                    try {
                        return eventService.getAvailableSeats(e);
                    } catch (Exception ex) {
                        return Integer.MAX_VALUE;
                    }
                }));
            }
        }
    }

    private void displayPage() {
        boolean isCards = viewTabs.getSelectedTab() == tabCards;

        cardsGrid.setVisible(isCards);
        grid.setVisible(!isCards);

        cardsGrid.removeAll();

        if (filteredSorted == null || filteredSorted.isEmpty()) {
            resultCount.setText("Aucun événement trouvé.");
            cardsGrid.add(createEmptyState());
            grid.setItems(Collections.emptyList());
            updatePagination();
            return;
        }

        int total = filteredSorted.size();
        int start = currentPage * pageSize;
        int end = Math.min(start + pageSize, total);

        if (start >= total) {
            currentPage = 0;
            start = 0;
            end = Math.min(pageSize, total);
        }

        List<Event> page = filteredSorted.subList(start, end);

        resultCount.setText(String.format("Affichage %d - %d sur %d événements", start + 1, end, total));

        if (isCards) {
            page.forEach(e -> cardsGrid.add(createEventCard(e)));
        } else {
            grid.setItems(page);
        }

        updatePagination();
    }

    private void updatePagination() {
        int totalPages = getTotalPages();
        int current = totalPages == 0 ? 0 : currentPage + 1;

        pageInfo.setText("Page " + current + " / " + Math.max(1, totalPages));
        prevBtn.setEnabled(currentPage > 0);
        nextBtn.setEnabled(currentPage < totalPages - 1);
    }

    private int getTotalPages() {
        if (filteredSorted == null || filteredSorted.isEmpty()) return 0;
        return (int) Math.ceil((double) filteredSorted.size() / pageSize);
    }

    /* ============================ CARD UI ============================ */

    private Div createEventCard(Event event) {
        Div card = new Div();
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "16px")
                .set("box-shadow", "0 6px 18px rgba(0,0,0,0.08)")
                .set("overflow", "hidden")
                .set("transition", "transform .15s ease, box-shadow .15s ease")
                .set("cursor", "pointer");

        card.getElement().addEventListener("mouseenter", e -> card.getStyle()
                .set("transform", "translateY(-4px)")
                .set("box-shadow", "0 14px 28px rgba(0,0,0,0.12)"));
        card.getElement().addEventListener("mouseleave", e -> card.getStyle()
                .set("transform", "translateY(0)")
                .set("box-shadow", "0 6px 18px rgba(0,0,0,0.08)"));

        // Top image (optional)
        Div img = new Div();
        img.getStyle()
                .set("height", "170px")
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("background-size", "cover")
                .set("background-position", "center");

        if (event.getImageUrl() != null && !event.getImageUrl().isBlank()) {
            img.getStyle().set("background-image", "url('" + event.getImageUrl() + "')");
        }

        Div body = new Div();
        body.getStyle().set("padding", "16px");

        H3 title = new H3(event.getTitre());
        title.getStyle()
                .set("margin", "0 0 10px 0")
                .set("font-size", "1.15rem")
                .set("font-weight", "700")
                .set("color", "#1a1a1a");

        Span category = new Span(getCategoryLabel(event.getCategorie()));
        category.getStyle()
                .set("display", "inline-block")
                .set("padding", "4px 10px")
                .set("border-radius", "999px")
                .set("font-size", "0.8rem")
                .set("background", "rgba(102,126,234,0.12)")
                .set("color", "#667eea")
                .set("font-weight", "600");

        HorizontalLayout info = new HorizontalLayout();
        info.setWidthFull();
        info.setSpacing(true);
        info.setAlignItems(FlexComponent.Alignment.CENTER);
        info.getStyle().set("margin-top", "12px");

        String dateText = (event.getDateDebut() != null)
                ? event.getDateDebut().format(dateFormatter) + " • " + event.getDateDebut().format(timeFormatter)
                : "—";

        Div lines = new Div();
        lines.getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "6px");

        lines.add(
                smallLine(VaadinIcon.CALENDAR, dateText),
                smallLine(VaadinIcon.MAP_MARKER, safe(event.getVille()))
        );

        int availableSeats = 0;
        try {
            availableSeats = eventService.getAvailableSeats(event);
        } catch (Exception ignored) {}

        Span seats = new Span(availableSeats + " places dispo");
        seats.getStyle()
                .set("font-weight", "700")
                .set("color", availableSeats > 50 ? "#2e7d32" : availableSeats > 10 ? "#ef6c00" : "#c62828");

        lines.add(seats);

        info.add(lines);

        HorizontalLayout footer = new HorizontalLayout();
        footer.setWidthFull();
        footer.setJustifyContentMode(JustifyContentMode.BETWEEN);
        footer.setAlignItems(FlexComponent.Alignment.CENTER);
        footer.getStyle().set("padding", "12px 16px 16px 16px");

        Span price = new Span(event.getPrixUnitaire() != null ? String.format("%.2f DH", event.getPrixUnitaire()) : "—");
        price.getStyle()
                .set("font-size", "1.3rem")
                .set("font-weight", "800")
                .set("color", "#667eea");

        Button details = new Button("Voir détails", VaadinIcon.ARROW_RIGHT.create());
        details.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        details.setIconAfterText(true);
        details.addClickListener(e -> navigationManager.navigateToEventDetail(event.getId()));

        // click card also navigates
        card.addClickListener(e -> navigationManager.navigateToEventDetail(event.getId()));

        body.add(category, title, info);
        card.add(img, body, footer);
        footer.add(price, details);

        return card;
    }

    private Component smallLine(VaadinIcon iconType, String text) {
        HorizontalLayout row = new HorizontalLayout();
        row.setSpacing(true);
        row.setAlignItems(FlexComponent.Alignment.CENTER);

        Icon icon = iconType.create();
        icon.setSize("14px");
        icon.getStyle().set("color", "#667");

        Span span = new Span(text);
        span.getStyle().set("color", "#666").set("font-size", "0.9rem");

        row.add(icon, span);
        return row;
    }

    private Div createEmptyState() {
        Div empty = new Div();
        empty.getStyle()
                .set("text-align", "center")
                .set("padding", "60px 20px")
                .set("grid-column", "1 / -1");

        H3 msg = new H3("📭 Aucun événement trouvé");
        msg.getStyle().set("color", "#999");

        empty.add(msg);
        return empty;
    }

    /* ============================ HELPERS ============================ */

    private void resetFilters() {
        searchField.clear();
        cityCombo.clear();
        categoryCombo.clear();
        startDatePicker.clear();
        endDatePicker.clear();
        priceMinField.clear();
        priceMaxField.clear();
        sortCombo.setValue("Date (récent)");
        currentPage = 0;
        applyFilters();
    }

    private String emptyToNull(String v) {
        return (v == null || v.trim().isBlank()) ? null : v.trim();
    }

    private String safe(String v) {
        return v == null ? "" : v;
    }

    private String getCategoryLabel(EventCategory category) {
        if (category == null) return "—";
        return switch (category) {
            case CONCERT -> "🎵 Concert";
            case THEATRE -> "🎭 Théâtre";
            case CONFERENCE -> "🎤 Conférence";
            case SPORT -> "⚽ Sport";
            case AUTRE -> "🎪 Autre";
        };
    }
}
