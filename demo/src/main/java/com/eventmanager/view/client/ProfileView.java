package com.eventmanager.view.client;

import com.eventmanager.entity.Reservation;
import com.eventmanager.entity.User;
import com.eventmanager.enums.ReservationStatus;
import com.eventmanager.security.AuthenticatedUser;
import com.eventmanager.service.IReservationService;
import com.eventmanager.service.IUserService;
import com.eventmanager.view.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Mon Profil")
public class ProfileView extends VerticalLayout implements BeforeEnterObserver {

    private final IUserService userService;
    private final IReservationService reservationService;
    private final AuthenticatedUser authenticatedUser;

    private User user;

    // Profile form
    private final Binder<User> profileBinder = new Binder<>(User.class);
    private TextField nomField;
    private TextField prenomField;
    private EmailField emailField;
    private TextField telephoneField;

    // Password form
    private PasswordField currentPasswordField;
    private PasswordField newPasswordField;
    private PasswordField confirmPasswordField;

    public ProfileView(IUserService userService,
                       IReservationService reservationService,
                       AuthenticatedUser authenticatedUser) {
        this.userService = userService;
        this.reservationService = reservationService;
        this.authenticatedUser = authenticatedUser;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("background", "var(--lumo-base-color)");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!authenticatedUser.isAuthenticated()) {
            Notification.show("Veuillez vous connecter.", 2500, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
            event.forwardTo("login");
            return;
        }

        Long userId = authenticatedUser.get()
                .map(User::getId)
                .orElse(null);

        if (userId == null) {
            event.forwardTo("login");
            return;
        }

        try {
            this.user = userService.getUserById(userId);
        } catch (Exception ex) {
            notifyError("Impossible de charger votre profil.");
            event.forwardTo("");
            return;
        }

        buildUI();
    }

    private void buildUI() {
        removeAll();

        // Header
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        H2 title = new H2("Profil");
        title.getStyle().set("margin", "0");

        Button logoutLike = new Button("Retour", VaadinIcon.ARROW_LEFT.create());
        logoutLike.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        logoutLike.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("dashboard")));

        header.add(title, logoutLike);

        // Sections
        Component profileCard = buildProfileCard();
        Component passwordCard = buildPasswordCard();
        Component statsCard = buildStatsCard();
        Component dangerCard = buildDangerCard();

        add(header, profileCard, passwordCard, statsCard, dangerCard);
    }

    // -------------------- PROFILE --------------------

    private Component buildProfileCard() {
        VerticalLayout card = card("Informations personnelles", "Modifiez vos informations. L’email n’est pas modifiable.");
        card.setWidthFull();

        nomField = new TextField("Nom");
        prenomField = new TextField("Prénom");
        emailField = new EmailField("Email");
        telephoneField = new TextField("Téléphone");

        nomField.setWidthFull();
        prenomField.setWidthFull();
        emailField.setWidthFull();
        telephoneField.setWidthFull();

        emailField.setReadOnly(true);

        FormLayout form = new FormLayout();
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("700px", 2)
        );
        form.add(nomField, prenomField, emailField, telephoneField);

        // Binder
        profileBinder.bind(nomField, User::getNom, User::setNom);
        profileBinder.bind(prenomField, User::getPrenom, User::setPrenom);
        profileBinder.bind(emailField, User::getEmail, User::setEmail);
        profileBinder.bind(telephoneField, User::getTelephone, User::setTelephone);
        profileBinder.readBean(user);

        Button save = new Button("Enregistrer", VaadinIcon.CHECK.create());
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.getStyle().set("border-radius", "12px");
        save.addClickListener(e -> saveProfile());

        HorizontalLayout actions = new HorizontalLayout(save);
        actions.setWidthFull();
        actions.setJustifyContentMode(JustifyContentMode.END);

        // 가입 날짜
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Span created = new Span("Inscrit le: " +
                (user.getDateInscription() != null ? user.getDateInscription().format(fmt) : "—"));
        created.getStyle().set("color", "var(--lumo-secondary-text-color)");

        card.add(form, created, actions);
        return card;
    }

    private void saveProfile() {
        try {
            if (!profileBinder.writeBeanIfValid(user)) {
                notifyWarn("Veuillez corriger les erreurs du formulaire.");
                return;
            }

            // Uses your existing method
            userService.updateProfile(
                    user.getId(),
                    user.getNom(),
                    user.getPrenom(),
                    user.getEmail(),
                    user.getTelephone()
            );

            notifySuccess("Profil mis à jour.");
        } catch (Exception ex) {
            notifyError(ex.getMessage() != null ? ex.getMessage() : "Erreur lors de la mise à jour.");
        }
    }

    // -------------------- PASSWORD --------------------

    private Component buildPasswordCard() {
        VerticalLayout card = card("Changer le mot de passe", "Utilisez un mot de passe fort (min 8 caractères).");
        card.setWidthFull();

        currentPasswordField = new PasswordField("Mot de passe actuel");
        newPasswordField = new PasswordField("Nouveau mot de passe");
        confirmPasswordField = new PasswordField("Confirmer le nouveau mot de passe");

        currentPasswordField.setWidthFull();
        newPasswordField.setWidthFull();
        confirmPasswordField.setWidthFull();

        FormLayout form = new FormLayout();
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("700px", 2)
        );

        form.add(currentPasswordField, newPasswordField);
        form.add(confirmPasswordField, 2);

        Button change = new Button("Mettre à jour le mot de passe", VaadinIcon.LOCK.create());
        change.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        change.getStyle().set("border-radius", "12px");
        change.addClickListener(e -> changePassword());

        HorizontalLayout actions = new HorizontalLayout(change);
        actions.setWidthFull();
        actions.setJustifyContentMode(JustifyContentMode.END);

        card.add(form, actions);
        return card;
    }

    private void changePassword() {
        String oldPwd = currentPasswordField.getValue();
        String newPwd = newPasswordField.getValue();
        String confirm = confirmPasswordField.getValue();

        if (oldPwd == null || oldPwd.isBlank()) {
            notifyWarn("Veuillez saisir votre mot de passe actuel.");
            return;
        }
        if (newPwd == null || newPwd.length() < 8) {
            notifyWarn("Le nouveau mot de passe doit contenir au moins 8 caractères.");
            return;
        }
        if (!newPwd.equals(confirm)) {
            notifyWarn("La confirmation ne correspond pas.");
            return;
        }

        try {
            userService.changePassword(user.getId(), oldPwd, newPwd);

            currentPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();

            notifySuccess("Mot de passe mis à jour.");
        } catch (Exception ex) {
            notifyError(ex.getMessage() != null ? ex.getMessage() : "Erreur lors du changement de mot de passe.");
        }
    }

    // -------------------- STATS --------------------

    private Component buildStatsCard() {
        VerticalLayout card = card("Statistiques personnelles", "Un aperçu rapide de votre activité.");
        card.setWidthFull();

        List<Reservation> list;
        try {
            list = reservationService.getReservationsByUser(user.getId());
        } catch (Exception ex) {
            list = List.of();
        }

        long total = list.size();
        long pending = list.stream().filter(r -> r.getStatut() == ReservationStatus.EN_ATTENTE).count();
        long confirmed = list.stream().filter(r -> r.getStatut() == ReservationStatus.CONFIRMEE).count();
        long cancelled = list.stream().filter(r -> r.getStatut() == ReservationStatus.ANNULEE).count();

        long upcoming = list.stream().filter(r ->
                r.getStatut() != ReservationStatus.ANNULEE &&
                        r.getEvenement() != null &&
                        r.getEvenement().getDateDebut() != null &&
                        r.getEvenement().getDateDebut().isAfter(LocalDateTime.now())
        ).count();

        // Simple “metric chips” (black/white, modern)
        HorizontalLayout row = new HorizontalLayout(
                metric("Total", String.valueOf(total), VaadinIcon.CLIPBOARD_TEXT),
                metric("À venir", String.valueOf(upcoming), VaadinIcon.CALENDAR_CLOCK),
                metric("Confirmées", String.valueOf(confirmed), VaadinIcon.CHECK),
                metric("En attente", String.valueOf(pending), VaadinIcon.CLOCK),
                metric("Annulées", String.valueOf(cancelled), VaadinIcon.CLOSE_CIRCLE)
        );
        row.setWidthFull();
        row.getStyle().set("flex-wrap", "wrap");
        row.setSpacing(true);

        card.add(row);
        return card;
    }

    private Component metric(String label, String value, VaadinIcon icon) {
        VerticalLayout box = new VerticalLayout();
        box.setPadding(true);
        box.setSpacing(false);
        box.setWidth("220px");
        box.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "16px")
                .set("background", "var(--lumo-base-color)")
                .set("box-shadow", "var(--lumo-box-shadow-xs)");

        HorizontalLayout top = new HorizontalLayout(icon.create(), new Span(label));
        top.setAlignItems(Alignment.CENTER);
        top.setSpacing(true);
        top.getStyle().set("color", "var(--lumo-secondary-text-color)");

        H3 num = new H3(value);
        num.getStyle().set("margin", "8px 0 0 0");

        box.add(top, num);
        return box;
    }

    // -------------------- DEACTIVATE --------------------

    private Component buildDangerCard() {
        VerticalLayout card = card("Désactiver le compte", "Vous pourrez le réactiver plus tard via un administrateur ou un support (selon votre logique).");
        card.setWidthFull();
        card.getStyle().set("border", "1px solid var(--lumo-error-color-50pct)");

        Button deactivate = new Button("Désactiver mon compte", VaadinIcon.USER_CLOCK.create());
        deactivate.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
        deactivate.getStyle().set("border-radius", "12px");

        deactivate.addClickListener(e -> {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader("Confirmation");
            dialog.setText("Voulez-vous vraiment désactiver votre compte ?");

            dialog.setCancelable(true);
            dialog.setConfirmText("Oui, désactiver");
            dialog.setConfirmButtonTheme("error primary");

            dialog.addConfirmListener(ev -> {
                try {
                    // Deactivate account
                    userService.toggleAccountStatus(user.getId(), false);
                    notifySuccess("Compte désactivé.");

                    // Logout user
                    UI.getCurrent().getPage().setLocation("/logout");
                } catch (Exception ex) {
                    notifyError(ex.getMessage() != null ? ex.getMessage() : "Erreur lors de la désactivation.");
                }
            });

            dialog.open();
        });

        card.add(deactivate);
        return card;
    }

    // -------------------- UI helpers --------------------

    private VerticalLayout card(String title, String subtitle) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setSpacing(true);
        card.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "18px")
                .set("background", "var(--lumo-base-color)")
                .set("box-shadow", "var(--lumo-box-shadow-s)");

        H3 h = new H3(title);
        h.getStyle().set("margin", "0");

        Span sub = new Span(subtitle);
        sub.getStyle().set("color", "var(--lumo-secondary-text-color)");

        card.add(h, sub, new Hr());
        return card;
    }

    private void notifySuccess(String msg) {
        Notification.show(msg, 2500, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void notifyWarn(String msg) {
        Notification.show(msg, 2500, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_WARNING);
    }

    private void notifyError(String msg) {
        Notification.show(msg, 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
