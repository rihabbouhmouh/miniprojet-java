package com.eventmanager.view.admin;

import com.eventmanager.entity.Reservation;
import com.eventmanager.enums.ReservationStatus;
import com.eventmanager.repository.ReservationRepository;
import com.eventmanager.view.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.DownloadEvent;
import com.vaadin.flow.server.streams.DownloadHandler;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Route(value = "admin/reservations", layout = MainLayout.class)
@PageTitle("Gestion des Réservations - Admin")
public class AllReservationsView extends VerticalLayout {

    private final ReservationRepository reservationRepository;

    private Grid<Reservation> grid;

    // Filters
    private TextField codeField;
    private TextField userField;
    private TextField eventField;
    private ComboBox<ReservationStatus> statusFilter;
    private DatePicker dateFrom;
    private DatePicker dateTo;

    private Span resultsInfo;

    // Stats components
    private Span statTotal;
    private Span statConfirmed;
    private Span statPending;
    private Span statCancelled;
    private Span statRevenue;

    private List<Reservation> allReservations;
    private List<Reservation> filteredReservations;

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public AllReservationsView(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setDefaultHorizontalComponentAlignment(Alignment.STRETCH);

        add(buildHeader());
        add(buildStatsRow());
        add(buildFiltersRow());
        add(buildGrid());

        loadReservations();
        applyFilters(); // initialize filtered list + stats
    }

    /* ===================== UI ===================== */

    private Component buildHeader() {
        VerticalLayout header = new VerticalLayout();
        header.setPadding(false);
        header.setSpacing(false);

        H2 title = new H2("🎫 Gestion des réservations");
        title.getStyle().set("margin", "0");

        Paragraph subtitle = new Paragraph("Recherchez, filtrez et gérez toutes les réservations du système.");
        subtitle.getStyle().set("margin", "4px 0 0 0").set("color", "var(--lumo-secondary-text-color)");

        header.add(title, subtitle);
        return header;
    }

    private Component buildStatsRow() {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setSpacing(true);
        row.getStyle().set("flex-wrap", "wrap");

        statTotal = new Span("—");
        statConfirmed = new Span("—");
        statPending = new Span("—");
        statCancelled = new Span("—");
        statRevenue = new Span("—");

        row.add(
                statCard("Total", statTotal, VaadinIcon.RECORDS),
                statCard("Confirmées", statConfirmed, VaadinIcon.CHECK_CIRCLE),
                statCard("En attente", statPending, VaadinIcon.CLOCK),
                statCard("Annulées", statCancelled, VaadinIcon.CLOSE_CIRCLE),
                statCard("Revenu (DH)", statRevenue, VaadinIcon.MONEY)
        );

        return row;
    }

    private Component statCard(String label, Span value, VaadinIcon icon) {
        HorizontalLayout card = new HorizontalLayout();
        card.setSpacing(true);
        card.setPadding(true);
        card.setAlignItems(FlexComponent.Alignment.CENTER);
        card.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "14px")
                .set("box-shadow", "var(--lumo-box-shadow-xs)")
                .set("min-width", "200px")
                .set("flex", "1");

        Icon ic = icon.create();
        ic.getStyle().set("color", "var(--lumo-primary-color)");

        VerticalLayout text = new VerticalLayout();
        text.setSpacing(false);
        text.setPadding(false);

        Span l = new Span(label);
        l.getStyle().set("font-size", "12px").set("color", "var(--lumo-secondary-text-color)");

        value.getStyle().set("font-size", "22px").set("font-weight", "700");

        text.add(l, value);
        card.add(ic, text);
        return card;
    }

    private Component buildFiltersRow() {
        VerticalLayout wrapper = new VerticalLayout();
        wrapper.setPadding(true);
        wrapper.setSpacing(true);
        wrapper.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "14px")
                .set("box-shadow", "var(--lumo-box-shadow-xs)");

        HorizontalLayout top = new HorizontalLayout();
        top.setWidthFull();
        top.setAlignItems(Alignment.CENTER);
        top.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        H4 filtersTitle = new H4("Filtres avancés");
        filtersTitle.getStyle().set("margin", "0");

        resultsInfo = new Span("— résultat(s)");
        resultsInfo.getStyle().set("color", "var(--lumo-secondary-text-color)");

        top.add(filtersTitle, resultsInfo);

        // Fields row 1
        HorizontalLayout row1 = new HorizontalLayout();
        row1.setWidthFull();
        row1.setSpacing(true);
        row1.getStyle().set("flex-wrap", "wrap");

        codeField = new TextField("Code");
        codeField.setPlaceholder("EVT-XXXXX");
        codeField.setPrefixComponent(VaadinIcon.SEARCH.create());
        codeField.setValueChangeMode(ValueChangeMode.LAZY);
        codeField.setValueChangeTimeout(250);
        codeField.addValueChangeListener(e -> applyFilters());

        userField = new TextField("Utilisateur");
        userField.setPlaceholder("Nom / prénom / email");
        userField.setPrefixComponent(VaadinIcon.USER.create());
        userField.setValueChangeMode(ValueChangeMode.LAZY);
        userField.setValueChangeTimeout(250);
        userField.addValueChangeListener(e -> applyFilters());

        eventField = new TextField("Événement");
        eventField.setPlaceholder("Titre de l’événement");
        eventField.setPrefixComponent(VaadinIcon.CALENDAR.create());
        eventField.setValueChangeMode(ValueChangeMode.LAZY);
        eventField.setValueChangeTimeout(250);
        eventField.addValueChangeListener(e -> applyFilters());

        statusFilter = new ComboBox<>("Statut");
        statusFilter.setItems(ReservationStatus.values());
        statusFilter.setClearButtonVisible(true);
        statusFilter.addValueChangeListener(e -> applyFilters());

        row1.add(codeField, userField, eventField, statusFilter);
        row1.setFlexGrow(2, codeField);
        row1.setFlexGrow(2, userField);
        row1.setFlexGrow(2, eventField);

        // Fields row 2
        HorizontalLayout row2 = new HorizontalLayout();
        row2.setWidthFull();
        row2.setSpacing(true);
        row2.setAlignItems(Alignment.END);
        row2.getStyle().set("flex-wrap", "wrap");

        dateFrom = new DatePicker("Du");
        dateFrom.addValueChangeListener(e -> applyFilters());

        dateTo = new DatePicker("Au");
        dateTo.addValueChangeListener(e -> applyFilters());

        Button clear = new Button("Réinitialiser", VaadinIcon.TRASH.create());
        clear.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        clear.addClickListener(e -> {
            codeField.clear();
            userField.clear();
            eventField.clear();
            statusFilter.clear();
            dateFrom.clear();
            dateTo.clear();
            applyFilters();
        });

        Button refresh = new Button("Actualiser", VaadinIcon.REFRESH.create());
        refresh.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        refresh.addClickListener(e -> {
            loadReservations();
            applyFilters();
        });

        Component export = buildExportButton(); // CSV bonus

        row2.add(dateFrom, dateTo, clear, export, refresh);

        wrapper.add(top, row1, row2);
        return wrapper;
    }

    private Component buildExportButton() {
        Button exportBtn = new Button("Exporter CSV", VaadinIcon.DOWNLOAD_ALT.create());
        exportBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

        Anchor exportAnchor = new Anchor();
        exportAnchor.getElement().setAttribute("download", true);
        exportAnchor.add(exportBtn);

        // New API (no StreamResource)
        DownloadHandler handler = (DownloadEvent event) -> {
            String fileName = "reservations_" + LocalDate.now() + ".csv";
            event.setFileName(fileName);
            event.getResponse().setHeader("Content-Type", "text/csv; charset=utf-8");

            String csv = buildCsv(filteredReservations == null ? List.of() : filteredReservations);

            try (OutputStream out = event.getOutputStream()) {
                out.write(csv.getBytes(StandardCharsets.UTF_8));
            }
        };

        exportAnchor.setHref(handler);
        return exportAnchor;
    }

    private Component buildGrid() {
        grid = new Grid<>(Reservation.class, false);
        grid.setSizeFull();
        grid.getStyle().set("min-height", "520px");
        grid.addThemeVariants(
                GridVariant.LUMO_ROW_STRIPES,
                GridVariant.LUMO_COLUMN_BORDERS
        );

        grid.addColumn(Reservation::getId).setHeader("ID").setWidth("80px").setFlexGrow(0);

        grid.addColumn(r -> safe(r.getCodeReservation())).setHeader("Code").setAutoWidth(true);

        grid.addColumn(r -> {
            if (r.getUtilisateur() == null) return "—";
            return safe(r.getUtilisateur().getPrenom()) + " " + safe(r.getUtilisateur().getNom());
        }).setHeader("Client").setAutoWidth(true);

        grid.addColumn(r -> r.getUtilisateur() != null ? safe(r.getUtilisateur().getEmail()) : "—")
                .setHeader("Email").setAutoWidth(true);

        grid.addColumn(r -> r.getEvenement() != null ? safe(r.getEvenement().getTitre()) : "—")
                .setHeader("Événement").setAutoWidth(true);

        grid.addColumn(r -> r.getNombrePlaces() != null ? r.getNombrePlaces() : 0)
                .setHeader("Places").setWidth("110px").setFlexGrow(0);

        grid.addColumn(r -> r.getMontantTotal() != null ? String.format("%.2f DH", r.getMontantTotal()) : "—")
                .setHeader("Montant").setWidth("140px").setFlexGrow(0);

        grid.addColumn(r -> r.getDateReservation() != null ? r.getDateReservation().format(DT) : "—")
                .setHeader("Date").setWidth("170px").setFlexGrow(0);

        grid.addComponentColumn(this::statusBadge).setHeader("Statut").setWidth("140px").setFlexGrow(0);

        grid.addComponentColumn(this::actions).setHeader("Actions").setWidth("220px").setFlexGrow(0);

        // let it scroll nicely
        setFlexGrow(1, grid);

        return grid;
    }

    /* ===================== DATA ===================== */

    private void loadReservations() {
        // should eager fetch user + event
        allReservations = reservationRepository.findAllWithDetails();
        // sort newest first
        allReservations.sort(Comparator.comparing(Reservation::getDateReservation,
                Comparator.nullsLast(Comparator.naturalOrder())).reversed());
    }

    private void applyFilters() {
        if (allReservations == null) {
            filteredReservations = List.of();
            grid.setItems(filteredReservations);
            updateStats(filteredReservations);
            resultsInfo.setText("0 résultat(s)");
            return;
        }

        final String code = trimLower(codeField.getValue());
        final String user = trimLower(userField.getValue());
        final String event = trimLower(eventField.getValue());
        final ReservationStatus status = statusFilter.getValue();
        final LocalDate from = dateFrom.getValue();
        final LocalDate to = dateTo.getValue();

        filteredReservations = allReservations.stream()
                .filter(r -> code == null || (r.getCodeReservation() != null &&
                        r.getCodeReservation().toLowerCase().contains(code)))
                .filter(r -> user == null || matchesUser(r, user))
                .filter(r -> event == null || (r.getEvenement() != null &&
                        r.getEvenement().getTitre() != null &&
                        r.getEvenement().getTitre().toLowerCase().contains(event)))
                .filter(r -> status == null || r.getStatut() == status)
                .filter(r -> from == null || (r.getDateReservation() != null &&
                        !r.getDateReservation().toLocalDate().isBefore(from)))
                .filter(r -> to == null || (r.getDateReservation() != null &&
                        !r.getDateReservation().toLocalDate().isAfter(to)))
                .toList();

        grid.setItems(filteredReservations);
        resultsInfo.setText(filteredReservations.size() + " résultat(s)");
        updateStats(filteredReservations);
    }

    private boolean matchesUser(Reservation r, String user) {
        if (r.getUtilisateur() == null) return false;
        String prenom = safe(r.getUtilisateur().getPrenom()).toLowerCase();
        String nom = safe(r.getUtilisateur().getNom()).toLowerCase();
        String email = safe(r.getUtilisateur().getEmail()).toLowerCase();
        return (prenom + " " + nom).contains(user) || email.contains(user) || nom.contains(user) || prenom.contains(user);
    }

    private void updateStats(List<Reservation> list) {
        long total = list.size();
        long confirmed = list.stream().filter(r -> r.getStatut() == ReservationStatus.CONFIRMEE).count();
        long pending = list.stream().filter(r -> r.getStatut() == ReservationStatus.EN_ATTENTE).count();
        long cancelled = list.stream().filter(r -> r.getStatut() == ReservationStatus.ANNULEE).count();

        double revenue = list.stream()
                .filter(r -> r.getStatut() == ReservationStatus.CONFIRMEE)
                .mapToDouble(r -> r.getMontantTotal() != null ? r.getMontantTotal() : 0.0)
                .sum();

        statTotal.setText(String.valueOf(total));
        statConfirmed.setText(String.valueOf(confirmed));
        statPending.setText(String.valueOf(pending));
        statCancelled.setText(String.valueOf(cancelled));
        statRevenue.setText(String.format("%.2f", revenue));
    }

    /* ===================== ACTIONS + BADGES ===================== */

    private Component statusBadge(Reservation r) {
        ReservationStatus s = r.getStatut();
        String bg =
                s == ReservationStatus.CONFIRMEE ? "#16a34a" :
                s == ReservationStatus.EN_ATTENTE ? "#f59e0b" :
                "#ef4444";

        Span badge = new Span(s != null ? s.name() : "—");
        badge.getStyle()
                .set("padding", "6px 10px")
                .set("border-radius", "999px")
                .set("font-size", "12px")
                .set("font-weight", "700")
                .set("color", "white")
                .set("background", bg);
        return badge;
    }

    private Component actions(Reservation r) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        layout.getStyle().set("flex-wrap", "wrap");

        // Confirm only if pending
        if (r.getStatut() == ReservationStatus.EN_ATTENTE) {
            Button confirm = new Button(VaadinIcon.CHECK.create());
            confirm.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
            confirm.getElement().setProperty("title", "Confirmer");
            confirm.addClickListener(e -> updateStatus(r, ReservationStatus.CONFIRMEE));
            layout.add(confirm);

            // Cancel only if pending (more logical)
            Button cancel = new Button(VaadinIcon.CLOSE.create());
            cancel.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            cancel.getElement().setProperty("title", "Annuler");
            cancel.addClickListener(e -> updateStatus(r, ReservationStatus.ANNULEE));
            layout.add(cancel);
        } else if (r.getStatut() == ReservationStatus.CONFIRMEE) {
            // Optional: show locked cancel (disabled) so user understands why there isn't a button
            Button locked = new Button(VaadinIcon.LOCK.create());
            locked.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            locked.setEnabled(false);
            locked.getElement().setProperty("title", "Réservation confirmée : l’admin ne l’annule pas ici.");
            layout.add(locked);
        }

        // View event
        if (r.getEvenement() != null) {
            Button view = new Button(VaadinIcon.EYE.create());
            view.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            view.getElement().setProperty("title", "Voir l’événement");
            view.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("event/" + r.getEvenement().getId())));
            layout.add(view);
        }

        return layout;
    }

    private void updateStatus(Reservation reservation, ReservationStatus status) {
        try {
            // safety: only allow changes from EN_ATTENTE
            if (reservation.getStatut() != ReservationStatus.EN_ATTENTE) {
                Notification.show("Action non autorisée pour ce statut.", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_CONTRAST);
                return;
            }

            reservation.setStatut(status);
            reservationRepository.save(reservation);

            Notification.show("Statut mis à jour", 2500, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            loadReservations();
            applyFilters();

        } catch (Exception e) {
            Notification.show(e.getMessage(), 5000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /* ===================== EXPORT ===================== */

    private String buildCsv(List<Reservation> rows) {
        // CSV simple (;) compatible Excel FR
        StringBuilder sb = new StringBuilder();
        sb.append("ID;Code;Client;Email;Événement;Places;Montant;Statut;Date\n");

        for (Reservation r : rows) {
            String id = r.getId() != null ? r.getId().toString() : "";
            String code = safe(r.getCodeReservation());
            String client = r.getUtilisateur() != null
                    ? (safe(r.getUtilisateur().getPrenom()) + " " + safe(r.getUtilisateur().getNom())).trim()
                    : "";
            String email = r.getUtilisateur() != null ? safe(r.getUtilisateur().getEmail()) : "";
            String event = r.getEvenement() != null ? safe(r.getEvenement().getTitre()) : "";
            String places = r.getNombrePlaces() != null ? r.getNombrePlaces().toString() : "0";
            String montant = r.getMontantTotal() != null ? String.format("%.2f", r.getMontantTotal()) : "0.00";
            String statut = r.getStatut() != null ? r.getStatut().name() : "";
            String date = r.getDateReservation() != null ? r.getDateReservation().format(DT) : "";

            sb.append(csvCell(id)).append(';')
                    .append(csvCell(code)).append(';')
                    .append(csvCell(client)).append(';')
                    .append(csvCell(email)).append(';')
                    .append(csvCell(event)).append(';')
                    .append(csvCell(places)).append(';')
                    .append(csvCell(montant)).append(';')
                    .append(csvCell(statut)).append(';')
                    .append(csvCell(date))
                    .append('\n');
        }
        return sb.toString();
    }

    private String csvCell(String s) {
        if (s == null) return "";
        // escape quotes, wrap in quotes if contains ; or " or newline
        String v = s.replace("\"", "\"\"");
        if (v.contains(";") || v.contains("\"") || v.contains("\n")) {
            return "\"" + v + "\"";
        }
        return v;
    }

    /* ===================== UTILS ===================== */

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String trimLower(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t.toLowerCase();
    }
}
