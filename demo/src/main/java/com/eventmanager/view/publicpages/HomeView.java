package com.eventmanager.view.publicpages;

import com.eventmanager.entity.Event;
import com.eventmanager.enums.EventCategory;
import com.eventmanager.service.IEventService;
import com.eventmanager.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * HomeView - Main landing page
 * Shows featured events, search functionality, and filters
 * Accessible without authentication
 */
@Route(value = "home", layout = MainLayout.class)
public class HomeView extends VerticalLayout {

    private final IEventService eventService;

    // UI Components
    private TextField searchField;
    private ComboBox<EventCategory> categoryFilter;
    private DatePicker dateFilter;
    private TextField cityFilter;
    private Button searchButton;
    private Button resetButton;

    private Div featuredEventsContainer;
    private Div allEventsContainer;

    // Formatters
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public HomeView(IEventService eventService) {
        this.eventService = eventService;

        // Configure main layout
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        addClassName("home-view");

        // Build the page
        add(
                createHeroSection(),
                createSearchSection(),
                createAllEventsSection(),
                createFeaturedSection(),
                createFooter()
        );

        // Load initial data
        loadFeaturedEvents();
        loadAllEvents();
    }

    /**
     * Create header with logo and navigation
     */
    private HorizontalLayout createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setPadding(true);
        header.setSpacing(true);
        header.addClassName("header");
        // a gradient white navbar
        header.getStyle()
                .set("background", "white")
                .set("color", "white")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)");

        // Logo
        H2 logo = new H2("🎭 EventHub");
        logo.getStyle()
                .set("margin", "0")
                // make the emoji bigger
                .set("font-size", "2rem")
                .set("cursor", "pointer");

        // Navigation buttons
        Button loginButton = new Button("Login", VaadinIcon.SIGN_IN.create());
        loginButton.addClassName("home-login-btn");
        loginButton.getStyle()
                .set("margin-bottom", "10px")
                .set("font-weight", "600")
                .set("font-size", "1rem")
                .set("padding", "14px")
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("border", "none")
                .set("box-shadow", "0 4px 12px rgba(0,0,0,0.1)")
                .set("color", "white")
                .set("border-radius", "12px")
                .set("cursor", "pointer");
        loginButton.addClickListener(e -> {
            // Navigate to login page
            getUI().ifPresent(ui -> ui.navigate("login"));
        });

        Button registerButton = new Button("Register", VaadinIcon.USER.create());
        registerButton.addClassName("home-register-btn");
        registerButton.addClickListener(e -> {
            // Navigate to register page
            getUI().ifPresent(ui -> ui.navigate("register"));
        });

        HorizontalLayout navButtons = new HorizontalLayout(loginButton, registerButton);
        navButtons.setSpacing(true);

        header.add(logo);
        header.add(navButtons);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        return header;
    }

    /**
     * Create hero section with welcome message
     */
    private VerticalLayout createHeroSection() {
        VerticalLayout hero = new VerticalLayout();
        hero.setWidthFull();
        hero.setPadding(true);
        hero.setSpacing(true);
        hero.getStyle()
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("color", "white")
                .set("padding", "60px 20px")
                .set("text-align", "center");

        H1 title = new H1("🎉 Discover Amazing Events");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "3rem")
                .set("line-height", "1.2")
                .set("letter-spacing", "-0.05em")
                .set("text-shadow", "0 4px 12px rgba(0,0,0,0.2)")
                .set("opacity", "0.9")
                .set("font-weight", "bold");

        Paragraph subtitle = new Paragraph("Find concerts, theatre shows, conferences and more near you");
        subtitle.getStyle()
                .set("font-size", "1.2rem")
                .set("opacity", "0.9")
                .set("max-width", "600px")
                .set("margin", "10px auto");

        hero.add(title, subtitle);
        hero.setAlignItems(FlexComponent.Alignment.CENTER);

        return hero;
    }

    /**
     * Create search and filter section
     */
    private VerticalLayout createSearchSection() {
        VerticalLayout searchSection = new VerticalLayout();
        searchSection.setWidthFull();
        searchSection.setPadding(true);
        searchSection.setSpacing(true);
        searchSection.getStyle()
                .set("background", "white")
                .set("box-shadow", "0 4px 12px rgba(0,0,0,0.1)")
                .set("border-radius", "12px")
                .set("margin", "-30px auto 40px")
                .set("max-width", "1200px")
                .set("padding", "30px");

        // Title
        H3 searchTitle = new H3("🔍 Search Events");
        searchTitle.getStyle().set("margin-top", "0");

        // Search fields
        HorizontalLayout searchRow = new HorizontalLayout();
        searchRow.setWidthFull();
        searchRow.setSpacing(true);

        // Keyword search
        searchField = new TextField();
        searchField.setPlaceholder("Search by title or keyword...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setWidthFull();
        searchField.getStyle().set("min-width", "250px");

        // City filter
        cityFilter = new TextField();
        cityFilter.setPlaceholder("City");
        cityFilter.setPrefixComponent(VaadinIcon.MAP_MARKER.create());
        cityFilter.setWidth("200px");

        // Category filter
        categoryFilter = new ComboBox<>();
        categoryFilter.setPlaceholder("Category");
        categoryFilter.setItems(EventCategory.values());
        categoryFilter.setItemLabelGenerator(cat -> cat.getLabel() + " " + cat.getIcon());
        categoryFilter.setWidth("200px");

        // Date filter
        dateFilter = new DatePicker();
        dateFilter.setPlaceholder("Select date");
        dateFilter.setWidth("200px");

        // Buttons
        searchButton = new Button("Search", VaadinIcon.SEARCH.create());
        searchButton.getStyle().set("cursor", "pointer");
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchButton.addClickListener(e -> performSearch());

        resetButton = new Button("Reset", VaadinIcon.REFRESH.create());
        resetButton.getStyle().set("cursor", "pointer");
        resetButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        resetButton.addClickListener(e -> resetFilters());

        searchRow.add(searchField, cityFilter, categoryFilter, dateFilter, searchButton, resetButton);
        searchRow.setAlignItems(FlexComponent.Alignment.END);

        searchSection.add(searchTitle, searchRow);

        return searchSection;
    }

    /**
     * Create featured events section
     */
    private VerticalLayout createFeaturedSection() {
        VerticalLayout featuredSection = new VerticalLayout();
        featuredSection.setWidthFull();
        featuredSection.setPadding(true);
        featuredSection.setSpacing(true);
        featuredSection.getStyle()
                .set("max-width", "1200px")
                .set("margin", "0 auto");

        H2 title = new H2("⭐ Featured Events");
        title.getStyle()
                .set("color", "#667eea")
                .set("margin-bottom", "20px");

        featuredEventsContainer = new Div();
        featuredEventsContainer.setWidthFull();
        featuredEventsContainer.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fill, minmax(350px, 1fr))")
                .set("gap", "24px");

        featuredSection.add(title, featuredEventsContainer);

        return featuredSection;
    }

    /**
     * Create all events section
     */
    private VerticalLayout createAllEventsSection() {
        VerticalLayout allEventsSection = new VerticalLayout();
        allEventsSection.setWidthFull();
        allEventsSection.setPadding(true);
        allEventsSection.setSpacing(true);
        allEventsSection.getStyle()
                .set("max-width", "1200px")
                .set("margin", "40px auto")
                .set("background", "#f8f9fa")
                .set("border-radius", "12px")
                .set("padding", "30px");

        H2 title = new H2("📅 All Upcoming Events");
        title.getStyle()
                .set("color", "#333")
                .set("margin-bottom", "20px");

        allEventsContainer = new Div();
        allEventsContainer.setWidthFull();
        allEventsContainer.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fill, minmax(300px, 1fr))")
                .set("gap", "20px");

        allEventsSection.add(title, allEventsContainer);

        return allEventsSection;
    }

    /**
     * Create footer
     */
    private Footer createFooter() {
        Footer footer = new Footer();
        footer.setWidthFull(); // occupe toute la largeur

        footer.getStyle()
                .set("background", "#1e2a38")
                .set("color", "white")
                .set("padding", "40px 20px")
                .set("margin-top", "40px");

        // Conteneur centré
        Div content = new Div();
        content.getStyle()
                .set("max-width", "1200px")
                .set("margin", "0 auto")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center")
                .set("gap", "20px")
                .set("text-align", "center");

        // ---- Liens ----
        HorizontalLayout links = new HorizontalLayout();
        links.setSpacing(true);
        links.setPadding(false);
        links.setMargin(false);

        Anchor about = new Anchor("#", "About");
        Anchor contact = new Anchor("#", "Contact");
        Anchor privacy = new Anchor("#", "Privacy Policy");

        about.getStyle().set("color", "white");
        contact.getStyle().set("color", "white");
        privacy.getStyle().set("color", "white");

        links.add(about, contact, privacy);

        // ---- Séparateur ----
        Div separator = new Div();
        separator.getStyle()
                .set("width", "80%")
                .set("height", "1px")
                .set("background", "rgba(255,255,255,0.2)");

        // ---- Texte Copyright ----
        Paragraph footerText = new Paragraph("© 2024 EventHub - All rights reserved");
        footerText.getStyle()
                .set("margin", "0")
                .set("opacity", "0.8");

        content.add(links, separator, footerText);
        footer.add(content);

        return footer;
    }



    /**
     * Load all available events
     */
    private void loadAllEvents() {
        allEventsContainer.removeAll();

        try {
            List<Event> events = eventService.getAvailableEvents();

            if (events.isEmpty()) {
                Paragraph noEvents = new Paragraph("No events available at the moment.");
                noEvents.getStyle().set("color", "#666");
                allEventsContainer.add(noEvents);
                return;
            }

            events.forEach(event -> {
                Div eventCard = createEventCard(event, false);
                allEventsContainer.add(eventCard);
            });

        } catch (Exception e) {
            Paragraph error = new Paragraph("Error loading events: " + e.getMessage());
            error.getStyle().set("color", "red");
            allEventsContainer.add(error);
        }
    }

    /**
     * Load featured/popular events
     */
    private void loadFeaturedEvents() {
        featuredEventsContainer.removeAll();

        try {
            List<Event> featuredEvents = eventService.getPopularEvents(6); // Get top 6

            if (featuredEvents.isEmpty()) {
                Paragraph noEvents = new Paragraph("No featured events at the moment.");
                noEvents.getStyle().set("color", "#666");
                featuredEventsContainer.add(noEvents);
                return;
            }

            featuredEvents.forEach(event -> {
                Div eventCard = createEventCard(event, true);
                featuredEventsContainer.add(eventCard);
            });

        } catch (Exception e) {
            Paragraph error = new Paragraph("Error loading featured events: " + e.getMessage());
            error.getStyle().set("color", "red");
            featuredEventsContainer.add(error);
        }
    }



    /**
     * Create event card component
     */
    private Div createEventCard(Event event, boolean isFeatured) {
        Div card = new Div();
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("overflow", "hidden")
                .set("box-shadow", isFeatured ? "0 8px 24px rgba(0,0,0,0.15)" : "0 4px 12px rgba(0,0,0,0.1)")
                .set("transition", "transform 0.2s, box-shadow 0.2s")
                .set("cursor", "pointer")
                .set("border", isFeatured ? "2px solid #ffd700" : "none");

        // Hover effect
        card.getElement().addEventListener("mouseenter", e -> {
            card.getStyle()
                    .set("transform", "translateY(-8px)")
                    .set("box-shadow", "0 12px 32px rgba(0,0,0,0.2)");
        });
        card.getElement().addEventListener("mouseleave", e -> {
            card.getStyle()
                    .set("transform", "translateY(0)")
                    .set("box-shadow", isFeatured ? "0 8px 24px rgba(0,0,0,0.15)" : "0 4px 12px rgba(0,0,0,0.1)");
        });

        // Click to view details
        card.addClickListener(e -> {
            getUI().ifPresent(ui -> ui.navigate("event/" + event.getId()));
        });

        // Event Image
        Div imageContainer = new Div();
        imageContainer.getStyle()
                .set("width", "100%")
                .set("height", "200px")
                .set("background", "linear-gradient(135deg, " + event.getCategorie().getColor() + " 0%, #667eea 100%)")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("position", "relative");

        // Category badge
        Span categoryBadge = new Span(event.getCategorie().getIcon() + " " + event.getCategorie().getLabel());
        categoryBadge.getStyle()
                .set("position", "absolute")
                .set("top", "10px")
                .set("right", "10px")
                .set("background", "white")
                .set("color", event.getCategorie().getColor())
                .set("padding", "4px 12px")
                .set("border-radius", "20px")
                .set("font-size", "0.85rem")
                .set("font-weight", "600")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.2)");

        // Event icon (if no image)
        Span iconSpan = new Span(event.getCategorie().getIcon());
        iconSpan.getStyle()
                .set("font-size", "4rem")
                .set("color", "white");

        imageContainer.add(iconSpan, categoryBadge);

        // Event Details
        VerticalLayout details = new VerticalLayout();
        details.setPadding(true);
        details.setSpacing(true);

        // Title
        H3 title = new H3(event.getTitre());
        title.getStyle()
                .set("margin", "0 0 8px 0")
                .set("font-size", "1.3rem")
                .set("color", "#333")
                .set("overflow", "hidden")
                .set("text-overflow", "ellipsis")
                .set("white-space", "nowrap");

        // Date and time
        HorizontalLayout dateTimeLayout = new HorizontalLayout();
        dateTimeLayout.setSpacing(true);
        dateTimeLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        Icon calendarIcon = VaadinIcon.CALENDAR.create();
        calendarIcon.setSize("16px");
        calendarIcon.setColor("#667eea");

        Span dateSpan = new Span(event.getDateDebut().format(dateFormatter) + " at " +
                event.getDateDebut().format(timeFormatter));
        dateSpan.getStyle()
                .set("font-size", "0.9rem")
                .set("color", "#666");

        dateTimeLayout.add(calendarIcon, dateSpan);

        // Location
        HorizontalLayout locationLayout = new HorizontalLayout();
        locationLayout.setSpacing(true);
        locationLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        Icon locationIcon = VaadinIcon.MAP_MARKER.create();
        locationIcon.setSize("16px");
        locationIcon.setColor("#e74c3c");

        Span locationSpan = new Span(event.getVille() + " - " + event.getLieu());
        locationSpan.getStyle()
                .set("font-size", "0.9rem")
                .set("color", "#666")
                .set("overflow", "hidden")
                .set("text-overflow", "ellipsis")
                .set("white-space", "nowrap");

        locationLayout.add(locationIcon, locationSpan);

        // Available seats
        HorizontalLayout seatsLayout = new HorizontalLayout();
        seatsLayout.setSpacing(true);
        seatsLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        Icon seatsIcon = VaadinIcon.USERS.create();
        seatsIcon.setSize("16px");
        seatsIcon.setColor("#27ae60");

        int availableSeats = eventService.getAvailableSeats(event);
        Span seatsSpan = new Span(availableSeats + " seats available");
        seatsSpan.getStyle()
                .set("font-size", "0.9rem")
                .set("color", availableSeats > 0 ? "#27ae60" : "#e74c3c")
                .set("font-weight", "600");

        seatsLayout.add(seatsIcon, seatsSpan);

        // Price and Book button
        HorizontalLayout footer = new HorizontalLayout();
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        footer.setAlignItems(FlexComponent.Alignment.CENTER);

        Span priceSpan = new Span(String.format("%.2f DH", event.getPrixUnitaire()));
        priceSpan.getStyle()
                .set("font-size", "1.5rem")
                .set("font-weight", "bold")
                .set("color", "#667eea");

        Button viewButton = new Button("View Details", VaadinIcon.ARROW_RIGHT.create());
        viewButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        viewButton.addClickListener(e -> {
            e.getSource().getUI().ifPresent(ui -> ui.navigate("event/" + event.getId()));
        });

        footer.add(priceSpan, viewButton);

        details.add(title, dateTimeLayout, locationLayout, seatsLayout, footer);

        card.add(imageContainer, details);

        return card;
    }

    /**
     * Perform search with filters
     */
    private void performSearch() {
        allEventsContainer.removeAll();

        String keyword = searchField.getValue();
        String city = cityFilter.getValue();
        EventCategory category = categoryFilter.getValue();
        LocalDate selectedDate = dateFilter.getValue();

        try {
            List<Event> results;

            // If date is selected, convert to LocalDateTime
            LocalDateTime startDate = selectedDate != null ?
                    selectedDate.atStartOfDay() : null;

            // Use the search method
            results = eventService.searchEventsByFilters(
                    keyword != null && !keyword.isEmpty() ? keyword : null,
                    city != null && !city.isEmpty() ? city : null,
                    category,
                    null, // minPrice
                    null  // maxPrice
            );

            // Filter by date if provided
            if (startDate != null) {
                results = results.stream()
                        .filter(e -> e.getDateDebut().toLocalDate().equals(selectedDate))
                        .toList();
            }

            if (results.isEmpty()) {
                Paragraph noResults = new Paragraph("😕 No events found matching your criteria.");
                noResults.getStyle()
                        .set("text-align", "center")
                        .set("color", "#666")
                        .set("padding", "40px");
                allEventsContainer.add(noResults);
                return;
            }

            results.forEach(event -> {
                Div eventCard = createEventCard(event, false);
                allEventsContainer.add(eventCard);
            });

        } catch (Exception e) {
            Paragraph error = new Paragraph("Error searching events: " + e.getMessage());
            error.getStyle().set("color", "red");
            allEventsContainer.add(error);
        }
    }

    /**
     * Reset all filters
     */
    private void resetFilters() {
        searchField.clear();
        cityFilter.clear();
        categoryFilter.clear();
        dateFilter.clear();
        loadAllEvents();
    }
}