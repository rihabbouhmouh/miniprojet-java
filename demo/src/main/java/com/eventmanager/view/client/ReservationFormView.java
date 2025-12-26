package com.eventmanager.view.client;

import com.eventmanager.entity.Event;
import com.eventmanager.entity.Reservation;
import com.eventmanager.entity.User;
import com.eventmanager.repository.ReservationRepository;
import com.eventmanager.service.IReservationService;
import com.eventmanager.security.AuthenticatedUser;
import com.eventmanager.service.IEventService;
import com.eventmanager.view.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.time.format.DateTimeFormatter;

@Route(value = "event/:id/reserve", layout = MainLayout.class)
@PageTitle("Réserver")
public class ReservationFormView extends VerticalLayout implements BeforeEnterObserver {

    private final IEventService eventService;
    private final ReservationRepository reservationRepository;
    private final AuthenticatedUser authenticatedUser;
    private final IReservationService reservationService;

    private Event event;
    private User currentUser;

    // UI
    private IntegerField seatsField;
    private Span unitPriceValue;
    private Span totalValue;
    private Span availabilityValue;
    private TextArea commentField;

    private Button submitBtn;

    public ReservationFormView(IEventService eventService,
                                 IReservationService reservationService,
                               ReservationRepository reservationRepository,
                               AuthenticatedUser authenticatedUser) {
        this.eventService = eventService;
        this.reservationRepository = reservationRepository;
        this.authenticatedUser = authenticatedUser;
        this.reservationService = reservationService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        addClassNames(LumoUtility.Padding.LARGE, LumoUtility.Gap.MEDIUM);
        getStyle().set("background", "var(--lumo-base-color)");
        }

    @Override
    public void beforeEnter(BeforeEnterEvent e) {
        // Must be logged in
        if (!authenticatedUser.isAuthenticated()) {
            Notification.show("Veuillez vous connecter pour réserver.",
                            2500, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
            e.forwardTo("login");
            return;
        }

        this.currentUser = authenticatedUser.get().orElse(null);
        if (currentUser == null) {
            e.forwardTo("login");
            return;
        }

        Long eventId = e.getRouteParameters().get("id").map(Long::valueOf).orElse(null);
        if (eventId == null) {
            notifyError("ID événement manquant");
            e.forwardTo("events"); // adjust if needed
            return;
        }

        try {
            this.event = eventService.getEventById(eventId);
        } catch (Exception ex) {
            notifyError("Événement introuvable");
            e.forwardTo("events"); // adjust if needed
            return;
        }

        buildUI();
        refreshComputed();
    }

    private void buildUI() {
        removeAll();

        VerticalLayout wrapper = new VerticalLayout();
        wrapper.setWidthFull();
        wrapper.setMaxWidth("900px");
        wrapper.getStyle().set("margin", "0 auto");
        wrapper.setPadding(false);
        wrapper.setSpacing(true);

        // Back
        Button back = new Button("Retour", VaadinIcon.ARROW_LEFT.create());
        back.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        back.getStyle()
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "12px")
                .set("background", "var(--lumo-base-color)");
        back.addClickListener(ev -> getUI().ifPresent(ui -> ui.navigate("event/" + event.getId())));

        // Card
        VerticalLayout card = new VerticalLayout();
        card.setWidthFull();
        card.getStyle()
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "14px")
                .set("box-shadow", "var(--lumo-box-shadow-m)")
                .set("padding", "18px")
                .set("background", "var(--lumo-base-color)");

        H2 title = new H2("Réservation");
        title.getStyle().set("margin", "0");

        Span subtitle = new Span(s(event.getTitre(), "Événement"));
        subtitle.addClassNames(LumoUtility.TextColor.SECONDARY);

        // Optional: small event info row
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Div meta = new Div();
        meta.getStyle()
                .set("margin-top", "6px")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "0.95em");
        String when = (event.getDateDebut() != null ? event.getDateDebut().format(fmt) : "—");
        String where = (s(event.getLieu(), "—") + " - " + s(event.getVille(), "—"));
        meta.setText("Date: " + when + "   |   Lieu: " + where);

        // Form area (2 columns wrap)
        HorizontalLayout formRow = new HorizontalLayout();
        formRow.setWidthFull();
        formRow.setSpacing(true);
        formRow.getStyle().set("flex-wrap", "wrap");

        VerticalLayout left = new VerticalLayout();
        left.setPadding(false);
        left.setSpacing(true);
        left.setMinWidth("320px");

        VerticalLayout right = new VerticalLayout();
        right.setPadding(false);
        right.setSpacing(true);
        right.setMinWidth("280px");

        // Seats spinner (1-10)
        seatsField = new IntegerField("Nombre de places");
        seatsField.setMin(1);
        seatsField.setMax(10);
        seatsField.setStepButtonsVisible(true);
        seatsField.setValue(1);
        seatsField.setWidthFull();
        seatsField.addValueChangeListener(ev -> refreshComputed());

        // Comment
        commentField = new TextArea("Commentaire (optionnel)");
        commentField.setWidthFull();
        commentField.setMaxHeight("160px");
        commentField.getStyle().set("border-radius", "12px");

        left.add(seatsField, commentField);

        // Summary box
        VerticalLayout summary = new VerticalLayout();
        summary.setPadding(true);
        summary.setSpacing(false);
        summary.setWidthFull();
        summary.getStyle()
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "12px")
                .set("background", "var(--lumo-contrast-5pct)");

        unitPriceValue = new Span("—");
        totalValue = new Span("—");
        availabilityValue = new Span("—");

        summary.add(
                summaryRow("Prix unitaire", unitPriceValue),
                summaryRow("Montant total", totalValue),
                summaryRow("Disponibilité", availabilityValue)
        );

        submitBtn = new Button("Confirmer la réservation", VaadinIcon.CHECK.create());
        submitBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submitBtn.setWidthFull();
        submitBtn.getStyle().set("border-radius", "12px");
        submitBtn.addClickListener(ev -> openRecapDialog());

        right.add(summary, submitBtn);

        formRow.add(left, right);
        formRow.setFlexGrow(2, left);
        formRow.setFlexGrow(1, right);

        card.add(title, subtitle, meta, new Hr(), formRow);

        wrapper.add(back, card);
        add(wrapper);
    }

    private Component summaryRow(String label, Span value) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(Alignment.BASELINE);
        row.getStyle().set("padding", "6px 0");

        Span l = new Span(label);
        l.getStyle().set("font-weight", "700");

        value.getStyle().set("margin-left", "auto").set("font-weight", "700");

        row.add(l, value);
        return row;
    }

    private void refreshComputed() {
        int seats = safeSeatsValue();

        double unit = (event.getPrixUnitaire() != null) ? event.getPrixUnitaire() : 0.0;
        double total = unit * seats;

        int available;
        try {
            available = eventService.getAvailableSeats(event); // uses your existing method
        } catch (Exception ex) {
            available = 0;
        }

        unitPriceValue.setText(String.format("%.2f DH", unit));
        totalValue.setText(String.format("%.2f DH", total));
        availabilityValue.setText(available + " place(s) disponible(s)");

        boolean ok = seats >= 1 && seats <= 10 && seats <= available;
        submitBtn.setEnabled(ok);

        if (!ok) {
            if (seats > 10) {
                notifyWarn("Maximum 10 places par réservation.");
            } else if (seats > available) {
                notifyWarn("Places insuffisantes. Disponible: " + available);
            }
        }
    }

    private void openRecapDialog() {
        int seats = safeSeatsValue();

        int available;
        try {
            available = eventService.getAvailableSeats(event);
        } catch (Exception ex) {
            available = 0;
        }

        if (seats < 1 || seats > 10) {
            notifyError("Nombre de places invalide (1 à 10).");
            return;
        }
        if (seats > available) {
            notifyError("Places insuffisantes. Disponible: " + available + ", demandé: " + seats);
            return;
        }

        double unit = (event.getPrixUnitaire() != null) ? event.getPrixUnitaire() : 0.0;
        double total = unit * seats;

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Récapitulatif");

        VerticalLayout recap = new VerticalLayout();
        recap.setPadding(false);
        recap.setSpacing(true);

        recap.add(line("Événement", s(event.getTitre(), "—")));
        recap.add(line("Places", String.valueOf(seats)));
        recap.add(line("Prix unitaire", String.format("%.2f DH", unit)));
        recap.add(line("Total", String.format("%.2f DH", total)));

        String comment = commentField.getValue();
        if (comment != null && !comment.isBlank()) {
            recap.add(line("Commentaire", comment));
        }

        Button confirm = new Button("Valider", VaadinIcon.CHECK.create());
        confirm.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        confirm.getStyle().set("border-radius", "12px");
        confirm.addClickListener(e -> {
            try {
                Reservation saved = createReservation(seats, comment);
                dialog.close();
                showSuccess(saved);
            } catch (Exception ex) {
                dialog.close();
                notifyError(ex.getMessage() != null ? ex.getMessage() : "Erreur lors de la réservation");
            }
        });

        Button cancel = new Button("Annuler");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancel.getStyle()
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "12px")
                .set("background", "var(--lumo-base-color)");
        cancel.addClickListener(e -> dialog.close());

        HorizontalLayout actions = new HorizontalLayout(confirm, cancel);
        actions.setWidthFull();
        actions.setJustifyContentMode(JustifyContentMode.END);

        dialog.add(recap, actions);
        dialog.open();
    }

    private Component line(String label, String value) {
        VerticalLayout box = new VerticalLayout();
        box.setPadding(false);
        box.setSpacing(false);

        Span l = new Span(label);
        l.getStyle().set("font-weight", "700");

        Span v = new Span(value);
        v.getStyle().set("color", "var(--lumo-secondary-text-color)");

        box.add(l, v);
        return box;
    }

    private Reservation createReservation(int seats, String comment) {
        // Re-check auth
        if (!authenticatedUser.isAuthenticated() || currentUser == null) {
            throw new IllegalStateException("Vous devez être connecté.");
        }

        // Re-check availability right before save
        int availableNow = eventService.getAvailableSeats(event);
        if (seats > availableNow) {
            throw new IllegalArgumentException("Places insuffisantes. Disponible: " + availableNow);
        }

        Reservation r = new Reservation();
        r.setUtilisateur(currentUser);
        r.setEvenement(event);
        r.setNombrePlaces(seats);

        if (comment != null && !comment.isBlank()) {
            r.setCommentaire(comment);
        }

        // Reservation entity will generate code + compute amount in @PrePersist
        return reservationService.createReservation(currentUser.getId(), event.getId(), seats , comment);
    }

    private void showSuccess(Reservation saved) {
        Dialog success = new Dialog();
        success.setHeaderTitle("Réservation confirmée");

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);

        Span msg = new Span("Votre réservation a été enregistrée.");
        msg.getStyle().set("font-weight", "700");

        Div codeBox = new Div();
        codeBox.setText("Code de réservation: " + s(saved.getCodeReservation(), "—"));
        codeBox.getStyle()
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "12px")
                .set("padding", "12px 14px")
                .set("background", "var(--lumo-contrast-5pct)")
                .set("font-weight", "800");

        Button toDashboard = new Button("Aller au dashboard", VaadinIcon.ARROW_RIGHT.create());
        toDashboard.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        toDashboard.getStyle()
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "12px")
                .set("background", "var(--lumo-base-color)");
        toDashboard.addClickListener(e -> {
            success.close();
            getUI().ifPresent(ui -> ui.navigate("dashboard"));
        });

        Button close = new Button("Fermer");
        close.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        close.getStyle().set("border-radius", "12px");
        close.addClickListener(e -> success.close());

        HorizontalLayout actions = new HorizontalLayout(close, toDashboard);
        actions.setJustifyContentMode(JustifyContentMode.END);
        actions.setWidthFull();

        content.add(msg, codeBox, actions);
        success.add(content);
        success.open();

        Notification.show("Réservation enregistrée", 2000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private int safeSeatsValue() {
        Integer v = seatsField != null ? seatsField.getValue() : 1;
        if (v == null) return 1;
        return v;
    }

    private String s(String v, String fallback) {
        return (v == null || v.isBlank()) ? fallback : v;
    }

    private void notifyError(String msg) {
        Notification.show(msg, 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private void notifyWarn(String msg) {
        Notification.show(msg, 2500, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_WARNING);
    }
}
