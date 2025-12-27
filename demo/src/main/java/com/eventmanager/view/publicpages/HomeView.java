package com.eventmanager.view.publicpages;

import com.eventmanager.entity.Event;
import com.eventmanager.enums.EventCategory;
import com.eventmanager.service.IEventService;
import com.eventmanager.view.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "home", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@PageTitle("EventHub - Discover Amazing Events")
@CssImport("./styles/home-view.css")
public class HomeView extends VerticalLayout {

    private final IEventService eventService;

    private TextField keywordField;
    private TextField cityField;
    private ComboBox<EventCategory> categoryField;
    private DatePicker dateField;

    private Div featuredGrid;
    private Div eventsGrid;

    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

    public HomeView(IEventService eventService) {
        this.eventService = eventService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        addClassName("home");

        add(
                buildHero(),
                buildSearchCard(),
                buildAllEventsSection(),
                buildFeaturedSection(),
                buildFooter()
        );

        reloadFeatured();
        reloadAllEvents();
    }

    /* ---------------------------
       HERO
     --------------------------- */
    private Component buildHero() {
        Div hero = new Div();
        hero.addClassName("home-hero");

        Div inner = new Div();
        inner.addClassName("home-hero-inner");

        H1 title = new H1("Discover events you’ll actually want to attend.");
        title.addClassName("home-hero-title");

        Paragraph subtitle = new Paragraph("Concerts, theatre, conferences, festivals — browse, filter, and book in seconds.");
        subtitle.addClassName("home-hero-subtitle");

        HorizontalLayout actions = new HorizontalLayout();
        actions.addClassName("home-hero-actions");

        Button browseBtn = new Button("Browse events", VaadinIcon.SEARCH.create());
        browseBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        browseBtn.addClassName("btn-pill");
        browseBtn.addClickListener(e ->
                getUI().ifPresent(ui ->
                        ui.getPage().executeJs("document.querySelector('.home-section-all')?.scrollIntoView({behavior:'smooth'})")
                )
        );

        Button featuredBtn = new Button("See featured", VaadinIcon.STAR.create());
        featuredBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        featuredBtn.addClassName("btn-pill");
        featuredBtn.addClickListener(e ->
                getUI().ifPresent(ui ->
                        ui.getPage().executeJs("document.querySelector('.home-section-featured')?.scrollIntoView({behavior:'smooth'})")
                )
        );

        actions.add(browseBtn, featuredBtn);

        inner.add(title, subtitle, actions);
        hero.add(inner);

        return hero;
    }

    /* ---------------------------
       SEARCH CARD
     --------------------------- */
    private Component buildSearchCard() {
        Div wrap = new Div();
        wrap.addClassName("home-search-wrap");

        Div card = new Div();
        card.addClassName("home-search-card");

        Div header = new Div();
        header.addClassName("home-search-header");

        H3 h = new H3("Search events");
        h.addClassName("home-search-title");

        Span hint = new Span("Use filters to find the perfect event.");
        hint.addClassName("home-search-hint");

        header.add(h, hint);

        // Fields
        keywordField = new TextField("Title / keyword");
        keywordField.setPlaceholder("e.g. Jazz, Expo, AI...");
        keywordField.setPrefixComponent(VaadinIcon.SEARCH.create());
        keywordField.setClearButtonVisible(true);

        cityField = new TextField("City");
        cityField.setPlaceholder("e.g. Rabat");
        cityField.setPrefixComponent(VaadinIcon.MAP_MARKER.create());
        cityField.setClearButtonVisible(true);

        categoryField = new ComboBox<>("Category");
        categoryField.setItems(EventCategory.values());
        categoryField.setItemLabelGenerator(EventCategory::getLabel);
        categoryField.setClearButtonVisible(true);

        dateField = new DatePicker("Date");
        dateField.setClearButtonVisible(true);

        FormLayout form = new FormLayout();
        form.addClassName("home-search-form");
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("700px", 2),
                new FormLayout.ResponsiveStep("1050px", 4)
        );

        form.add(keywordField, cityField, categoryField, dateField);

        // Buttons
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.addClassName("home-search-actions");

        Button searchBtn = new Button("Search", VaadinIcon.SEARCH.create());
        searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchBtn.addClassName("btn-pill");
        searchBtn.addClickListener(e -> performSearch());

        Button resetBtn = new Button("Reset", VaadinIcon.REFRESH.create());
        resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        resetBtn.addClassName("btn-pill");
        resetBtn.addClickListener(e -> {
            keywordField.clear();
            cityField.clear();
            categoryField.clear();
            dateField.clear();
            reloadAllEvents();
        });

        buttons.add(searchBtn, resetBtn);

        card.add(header, form, buttons);
        wrap.add(card);
        return wrap;
    }

    /* ---------------------------
       FEATURED SECTION
     --------------------------- */
    private Component buildFeaturedSection() {
        Div section = new Div();
        section.addClassNames("home-section", "home-section-featured");

        Div head = new Div();
        head.addClassName("home-section-head");

        H2 title = new H2("Featured events");
        title.addClassName("home-section-title");

        Span badge = new Span("POPULAR");
        badge.addClassName("pill-badge");

        head.add(title, badge);

        featuredGrid = new Div();
        featuredGrid.addClassName("events-grid");

        section.add(head, featuredGrid);
        return section;
    }

    /* ---------------------------
       ALL EVENTS SECTION
     --------------------------- */
    private Component buildAllEventsSection() {
        Div section = new Div();
        section.addClassNames("home-section", "home-section-all");

        Div head = new Div();
        head.addClassName("home-section-head");

        H2 title = new H2("All upcoming events");
        title.addClassName("home-section-title");

        head.add(title);

        eventsGrid = new Div();
        eventsGrid.addClassName("events-grid");

        section.add(head, eventsGrid);
        return section;
    }

    /* ---------------------------
       FOOTER
     --------------------------- */
    private Component buildFooter() {
        Footer footer = new Footer();
        footer.addClassName("home-footer");

        Div inner = new Div();
        inner.addClassName("home-footer-inner");

        Paragraph p = new Paragraph("© 2026 EventHub — All rights reserved.");
        p.addClassName("home-footer-text");

        inner.add(p);
        footer.add(inner);

        return footer;
    }

    /* ---------------------------
       DATA LOADERS
     --------------------------- */
    private void reloadFeatured() {
        featuredGrid.removeAll();
        try {
            List<Event> featured = eventService.getPopularEvents(6);
            if (featured == null || featured.isEmpty()) {
                featuredGrid.add(emptyState("No featured events yet."));
                return;
            }
            featured.forEach(e -> featuredGrid.add(eventCard(e, true)));
        } catch (Exception ex) {
            featuredGrid.add(errorState("Error loading featured events: " + ex.getMessage()));
        }
    }

    private void reloadAllEvents() {
        eventsGrid.removeAll();
        try {
            List<Event> events = eventService.getAvailableEvents();
            if (events == null || events.isEmpty()) {
                eventsGrid.add(emptyState("No events available right now."));
                return;
            }
            events.forEach(e -> eventsGrid.add(eventCard(e, false)));
        } catch (Exception ex) {
            eventsGrid.add(errorState("Error loading events: " + ex.getMessage()));
        }
    }

    private void performSearch() {
        eventsGrid.removeAll();

        String keyword = safeTrim(keywordField.getValue());
        String city = safeTrim(cityField.getValue());
        EventCategory category = categoryField.getValue();
        LocalDate date = dateField.getValue();

        try {
            List<Event> results = eventService.searchEventsByFilters(
                    keyword.isEmpty() ? null : keyword,
                    city.isEmpty() ? null : city,
                    category,
                    null,
                    null
            );

            if (date != null) {
                results = results.stream()
                        .filter(e -> e.getDateDebut() != null && e.getDateDebut().toLocalDate().equals(date))
                        .toList();
            }

            if (results.isEmpty()) {
                eventsGrid.add(emptyState("No events match your search."));
                return;
            }

            results.forEach(e -> eventsGrid.add(eventCard(e, false)));

        } catch (Exception ex) {
            eventsGrid.add(errorState("Search error: " + ex.getMessage()));
        }
    }

    /* ---------------------------
       EVENT CARD
     --------------------------- */
    private Component eventCard(Event event, boolean featured) {
        Div card = new Div();
        card.addClassName("event-card");

        // Image area
        Div img = new Div();
        img.addClassName("event-card-image");

        String url = event.getImageUrl();
        if (url != null && !url.isBlank()) {
            img.getStyle().set("background-image", "url('" + url + "')");
        } else {
            // fallback gradient
            String c = (event.getCategorie() != null ? event.getCategorie().getColor() : "#6b7280");
            img.getStyle().set("background-image", "linear-gradient(135deg, " + c + ", #a5b4fc)");
        }

        // Badges
        if (featured) {
            Span f = new Span("FEATURED");
            f.addClassNames("event-badge", "event-badge-featured");
            img.add(f);
        }

        Span cat = new Span(event.getCategorie() != null ? event.getCategorie().getLabel() : "Other");
        cat.addClassNames("event-badge", "event-badge-category");
        img.add(cat);

        // Body
        Div body = new Div();
        body.addClassName("event-card-body");

        H3 title = new H3(event.getTitre() != null ? event.getTitre() : "Untitled event");
        title.addClassName("event-title");

        Div meta1 = metaRow(VaadinIcon.CALENDAR.create(), safeDateTime(event.getDateDebut()));
        Div meta2 = metaRow(VaadinIcon.MAP_MARKER.create(), safeLocation(event));

        int available = 0;
        try {
            available = eventService.getAvailableSeats(event);
        } catch (Exception ignored) {}

        Div meta3 = metaRow(VaadinIcon.USERS.create(), available + " seats available");
        meta3.addClassName(available <= 0 ? "meta-danger" : available < 20 ? "meta-warn" : "meta-ok");

        Div divider = new Div();
        divider.addClassName("event-divider");

        Div footer = new Div();
        footer.addClassName("event-card-footer");

        Div priceBox = new Div();
        priceBox.addClassName("price-box");

        Span priceLabel = new Span("Price");
        priceLabel.addClassName("price-label");

        Span priceValue = new Span(String.format("%.2f DH", event.getPrixUnitaire() != null ? event.getPrixUnitaire() : 0.0));
        priceValue.addClassName("price-value");

        priceBox.add(priceLabel, priceValue);

        Button view = new Button("View", VaadinIcon.ARROW_RIGHT.create());
        view.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        view.addClassName("btn-view");
        view.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("event/" + event.getId())));

        footer.add(priceBox, view);

        body.add(title, meta1, meta2, meta3, divider, footer);

        card.add(img, body);

        // Entire card clickable too
        card.getElement().addEventListener("click", e ->
                getUI().ifPresent(ui -> ui.navigate("event/" + event.getId()))
        );

        return card;
    }

    private Div metaRow(Icon icon, String text) {
        Div row = new Div();
        row.addClassName("event-meta");

        icon.setSize("18px");
        icon.getStyle().set("opacity", "0.85");

        Span t = new Span(text);
        t.addClassName("event-meta-text");

        row.add(icon, t);
        return row;
    }

    private Component emptyState(String msg) {
        Div box = new Div();
        box.addClassName("state-box");

        Icon icon = VaadinIcon.INFO_CIRCLE.create();
        icon.setSize("36px");
        icon.getStyle().set("opacity", "0.35");

        Span t = new Span(msg);
        t.addClassName("state-text");

        box.add(icon, t);
        return box;
    }

    private Component errorState(String msg) {
        Div box = new Div();
        box.addClassNames("state-box", "state-error");

        Icon icon = VaadinIcon.WARNING.create();
        icon.setSize("36px");

        Span t = new Span(msg);
        t.addClassName("state-text");

        box.add(icon, t);
        return box;
    }

    private String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    private String safeLocation(Event e) {
        String v = (e.getVille() != null ? e.getVille() : "");
        String l = (e.getLieu() != null ? e.getLieu() : "");
        String out = (v + (v.isBlank() || l.isBlank() ? "" : " • ") + l).trim();
        return out.isBlank() ? "Location not specified" : out;
    }

    private String safeDateTime(LocalDateTime dt) {
        if (dt == null) return "Date not specified";
        return dt.format(dateFmt) + " • " + dt.format(timeFmt);
    }
}
