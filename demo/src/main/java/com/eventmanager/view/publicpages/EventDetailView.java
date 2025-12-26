package com.eventmanager.view.publicview;

import com.eventmanager.entity.Event;
import com.eventmanager.security.AuthenticatedUser;
import com.eventmanager.service.IEventService;
import com.eventmanager.view.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.time.format.DateTimeFormatter;

@Route(value = "event/:id", layout = MainLayout.class)
@PageTitle("Détails de l'événement")
@AnonymousAllowed
public class EventDetailView extends VerticalLayout implements BeforeEnterObserver {

    private final IEventService eventService;
    private final AuthenticatedUser authenticatedUser;

    private Event event;

    public EventDetailView(IEventService eventService, AuthenticatedUser authenticatedUser) {
        this.eventService = eventService;
        this.authenticatedUser = authenticatedUser;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent e) {
        Long eventId = e.getRouteParameters().get("id")
                .map(Long::valueOf)
                .orElse(null);

        if (eventId == null) {
            notifyError("ID événement manquant");
            e.forwardTo("events"); // change if your list route is different
            return;
        }

        try {
            this.event = eventService.getEventById(eventId);
        } catch (Exception ex) {
            notifyError("Événement introuvable");
            e.forwardTo("events"); // change if your list route is different
            return;
        }

        buildUI();
    }

    private void buildUI() {
        removeAll();

        // Page container (card)
        VerticalLayout card = new VerticalLayout();
        card.setWidthFull();
        card.setMaxWidth("1100px");
        card.getStyle()
                .set("margin", "0 auto")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "14px")
                .set("box-shadow", "var(--lumo-box-shadow-m)")
                .set("padding", "18px")
                .set("background", "var(--lumo-base-color)");

        // Header: Title + badges
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        H1 title = new H1(s(event.getTitre(), "Détails de l'événement"));
        title.getStyle().set("margin", "0");

        HorizontalLayout badges = new HorizontalLayout();
        badges.setSpacing(true);

        if (event.getCategorie() != null) {
            Span cat = badge(event.getCategorie().name(), "var(--lumo-primary-color-10pct)", "var(--lumo-primary-text-color)");
            badges.add(cat);
        }
        if (event.getStatut() != null) {
            String bg;
            String fg;
            switch (event.getStatut()) {
                case PUBLIE -> { bg = "var(--lumo-success-color-10pct)"; fg = "var(--lumo-success-text-color)"; }
                case BROUILLON -> { bg = "var(--lumo-contrast-10pct)"; fg = "var(--lumo-body-text-color)"; }
                case ANNULE -> { bg = "var(--lumo-error-color-10pct)"; fg = "var(--lumo-error-text-color)"; }
                default -> { bg = "var(--lumo-contrast-10pct)"; fg = "var(--lumo-body-text-color)"; }
            }
            Span st = badge(event.getStatut().name(), bg, fg);
            badges.add(st);
        }

        header.add(title, badges);

        // Image block (optional)
        Component imageBlock = buildImageBlock();

        // Content: left info + right reservation card
        HorizontalLayout content = new HorizontalLayout();
        content.setWidthFull();
        content.setSpacing(true);
        content.getStyle().set("flex-wrap", "wrap");

        VerticalLayout left = buildLeftColumn();
        VerticalLayout right = buildRightColumn();

        content.add(left, right);
        content.setFlexGrow(2, left);
        content.setFlexGrow(1, right);

        card.add(header);

        if (imageBlock != null) card.add(imageBlock);

        card.add(content);

        // Google Maps (bonus) - simple iframe based on lieu+ville
        card.add(buildMapBlock());

        // Back button (simple)
        Button back = new Button("Retour", VaadinIcon.ARROW_LEFT.create());
        back.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        back.addClickListener(ev -> getUI().ifPresent(ui -> ui.navigate("events"))); // adjust route if needed

        VerticalLayout page = new VerticalLayout(back, card);
        page.setWidthFull();
        page.setPadding(false);
        page.setSpacing(true);
        page.setAlignItems(Alignment.CENTER);

        add(page);
    }

    private Component buildImageBlock() {
        String url = event.getImageUrl();
        if (url == null || url.isBlank()) return null;

        Image img = new Image(url, s(event.getTitre(), "image"));
        img.setWidthFull();
        img.setHeight(360, Unit.PIXELS);
        img.getStyle()
                .set("object-fit", "cover")
                .set("border-radius", "12px")
                .set("margin-top", "10px");

        return img;
    }

    private VerticalLayout buildLeftColumn() {
        VerticalLayout left = new VerticalLayout();
        left.setPadding(false);
        left.setSpacing(true);
        left.setMinWidth("320px");

        // Description
        H3 descTitle = new H3("📄 Description");
        Paragraph desc = new Paragraph(s(event.getDescription(), "Aucune description disponible."));
        desc.getStyle().set("white-space", "pre-wrap");

        // Info list
        H3 infoTitle = new H3("📋 Informations");
        VerticalLayout info = new VerticalLayout();
        info.setPadding(false);
        info.setSpacing(false);
        info.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "12px")
                .set("padding", "12px");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        info.add(infoRow(VaadinIcon.MAP_MARKER, "Lieu",
                s(event.getLieu(), "—") + " — " + s(event.getVille(), "—")));

        info.add(infoRow(VaadinIcon.CALENDAR, "Début",
                event.getDateDebut() != null ? event.getDateDebut().format(fmt) : "—"));

        info.add(infoRow(VaadinIcon.CALENDAR_CLOCK, "Fin",
                event.getDateFin() != null ? event.getDateFin().format(fmt) : "—"));

        info.add(infoRow(VaadinIcon.USERS, "Capacité",
                event.getCapaciteMax() != null ? event.getCapaciteMax() + " places" : "—"));

        left.add(descTitle, desc, infoTitle, info);

        return left;
    }

    private VerticalLayout buildRightColumn() {
        VerticalLayout right = new VerticalLayout();
        right.setPadding(true);
        right.setSpacing(true);
        right.setMinWidth("280px");
        right.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "12px")
                .set("box-shadow", "var(--lumo-box-shadow-s)")
                .set("background", "var(--lumo-contrast-5pct)");

        H3 title = new H3("🎟 Réservation");
        title.getStyle().set("margin", "0");

        int available = 0;
        try {
            available = eventService.getAvailableSeats(event); // ✅ uses your method
        } catch (Exception ex) {
            available = 0;
        }

        Span price = new Span(event.getPrixUnitaire() != null
                ? String.format("%.2f DH", event.getPrixUnitaire())
                : "—");
        price.getStyle().set("font-size", "2.2em").set("font-weight", "700");

        Span per = new Span("par place");
        per.getStyle().set("color", "var(--lumo-secondary-text-color)");

        Span avail = badge("Places dispo : " + available,
                available > 0 ? "var(--lumo-success-color-10pct)" : "var(--lumo-error-color-10pct)",
                available > 0 ? "var(--lumo-success-text-color)" : "var(--lumo-error-text-color)");

        Button reserve = new Button("Réserver", VaadinIcon.CART.create());
        reserve.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        reserve.setWidthFull();

        // disable if full
        if (available <= 0) {
            reserve.setEnabled(false);
            reserve.setText("Complet");
        }

        reserve.addClickListener(ev -> {
            if (!authenticatedUser.isAuthenticated()) {
                Notification.show("Veuillez vous connecter pour réserver",
                                2500, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_WARNING);
                getUI().ifPresent(ui -> ui.navigate("login"));
                return;
            }
            getUI().ifPresent(ui -> ui.navigate("event/" + event.getId() + "/reserve"));
        });

        right.add(title, price, per, avail, reserve);
        return right;
    }

    private Component buildMapBlock() {
        // bonus map: use an iframe with query=lieu+ville (works without API key)
        String query = (s(event.getLieu(), "") + " " + s(event.getVille(), "")).trim();
        if (query.isBlank()) return new Div();

        String src = "https://www.google.com/maps?q=" + query.replace(" ", "+") + "&output=embed";

        IFrame map = new IFrame(src);
        map.setWidthFull();
        map.setHeight("320px");
        map.getStyle()
                .set("border", "0")
                .set("border-radius", "12px")
                .set("margin-top", "14px");

        VerticalLayout box = new VerticalLayout(new H3("🗺 Plan"), map);
        box.setPadding(false);
        box.setSpacing(true);
        return box;
    }

    private HorizontalLayout infoRow(VaadinIcon icon, String label, String value) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(Alignment.CENTER);
        row.getStyle().set("padding", "6px 0");

        Span ic = new Span(icon.create());
        ic.getStyle().set("color", "var(--lumo-primary-text-color)");

        Span l = new Span(label + " :");
        l.getStyle().set("font-weight", "600");
        l.getStyle().set("min-width", "110px");

        Span v = new Span(value);
        v.getStyle().set("color", "var(--lumo-secondary-text-color)");

        row.add(ic, l, v);
        return row;
    }

    private Span badge(String text, String bg, String fg) {
        Span b = new Span(text);
        b.getStyle()
                .set("background", bg)
                .set("color", fg)
                .set("padding", "6px 10px")
                .set("border-radius", "999px")
                .set("font-weight", "600")
                .set("font-size", "0.9em");
        return b;
    }

    private String s(String v, String fallback) {
        return (v == null || v.isBlank()) ? fallback : v;
    }

    private void notifyError(String msg) {
        Notification.show(msg, 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
