package com.eventmanager.view.admin;

import com.eventmanager.entity.User;
import com.eventmanager.enums.UserRole;
import com.eventmanager.security.AuthenticatedUser;
import com.eventmanager.service.IUserService;
import com.eventmanager.view.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Route(value = "admin/users", layout = MainLayout.class)
@PageTitle("Gestion des Utilisateurs - Admin")
@CssImport("./styles/admin-users.css")
public class UserManagementView extends VerticalLayout {

    private final IUserService userService;
    private final AuthenticatedUser authenticatedUser;

    private Long currentAdminId;

    private Grid<User> userGrid;
    private List<User> allUsers;
    private ListDataProvider<User> dataProvider;

    private TextField searchField;
    private ComboBox<UserRole> roleFilter;
    private ComboBox<Boolean> statusFilter;

    public UserManagementView(IUserService userService, AuthenticatedUser authenticatedUser) {
        this.userService = userService;
        this.authenticatedUser = authenticatedUser;

        this.currentAdminId = authenticatedUser.getUserId().orElse(null);

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        addClassName("admin-users-view");

        add(
                buildHeader(),
                buildFiltersCard(),
                buildGridCard()
        );

        loadUsers();
    }

    /* ======================= UI BUILDERS ======================= */

    private Component buildHeader() {
        Div header = new Div();
        header.addClassName("admin-users-header");

        H2 title = new H2("Gestion des utilisateurs");
        title.addClassName("admin-users-title");

        Paragraph sub = new Paragraph("Recherchez, filtrez, modifiez et gérez les comptes utilisateurs.");
        sub.addClassName("admin-users-subtitle");

        header.add(title, sub);
        return header;
    }

    private Component buildFiltersCard() {
        Div card = new Div();
        card.addClassNames("card", "filters-card");

        Div top = new Div();
        top.addClassName("card-head");

        H3 t = new H3("Filtres");
        t.addClassName("card-title");

        Button refreshButton = new Button("Actualiser", VaadinIcon.REFRESH.create());
        refreshButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        refreshButton.addClassName("btn");
        refreshButton.addClickListener(e -> loadUsers());

        top.add(t, refreshButton);

        // Fields
        searchField = new TextField("Recherche");
        searchField.setPlaceholder("Nom, prénom ou email...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setWidthFull();
        searchField.addValueChangeListener(e -> filterUsers());

        roleFilter = new ComboBox<>("Rôle");
        roleFilter.setItems(UserRole.values());
        roleFilter.setItemLabelGenerator(UserRole::getLabel);
        roleFilter.setClearButtonVisible(true);
        roleFilter.setWidthFull();
        roleFilter.addValueChangeListener(e -> filterUsers());

        statusFilter = new ComboBox<>("Statut");
        statusFilter.setItems(true, false);
        statusFilter.setItemLabelGenerator(s -> s ? "Actif" : "Inactif");
        statusFilter.setClearButtonVisible(true);
        statusFilter.setWidthFull();
        statusFilter.addValueChangeListener(e -> filterUsers());

        FormLayout form = new FormLayout();
        form.addClassName("filters-form");
        form.setWidthFull();
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("900px", 3)
        );

        form.add(searchField, roleFilter, statusFilter);

        Div body = new Div(form);
        body.addClassName("filters-body");

        card.add(top, body);
        return wrapContainer(card);
    }


    private Component buildGridCard() {
        Div card = new Div();
        card.addClassName("card");

        Div head = new Div();
        head.addClassName("card-head");

        H3 t = new H3("Liste des utilisateurs");
        t.addClassName("card-title");

        Span hint = new Span("Votre compte est épinglé en haut.");
        hint.addClassName("card-hint");

        head.add(t, hint);

        userGrid = new Grid<>(User.class, false);
        userGrid.addClassName("user-grid");
        userGrid.setWidthFull();
        userGrid.setHeight("520px"); // ✅ important: prevents bad layout / scrollbars
        userGrid.setSelectionMode(Grid.SelectionMode.NONE);


        userGrid.addColumn(new ComponentRenderer<>(this::nameCell))
                .setHeader("Utilisateur")
                .setWidth("260px")
                .setFlexGrow(0);

        userGrid.addColumn(User::getEmail)
        .setHeader("Email")
        .setWidth("280px")
        .setFlexGrow(0);

        userGrid.addColumn(user -> user.getTelephone() != null ? user.getTelephone() : "—")
                .setHeader("Téléphone")
                .setWidth("150px")
                .setFlexGrow(0);

        userGrid.addColumn(new ComponentRenderer<>(this::rolePill))
                .setHeader("Rôle")
                .setWidth("140px")
                .setFlexGrow(0);

        userGrid.addColumn(new ComponentRenderer<>(this::statusPill))
                .setHeader("Statut")
                .setWidth("130px")
                .setFlexGrow(0);

        userGrid.addColumn(user -> user.getDateInscription() != null
                        ? user.getDateInscription().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                        : "—")
                .setHeader("Inscription")
                .setWidth("180px")
                .setFlexGrow(0);

        userGrid.addComponentColumn(this::actionsCell)
                .setHeader("Actions")
                .setWidth("320px")
                .setFlexGrow(0);

        Div body = new Div(userGrid);
        body.addClassName("card-body");
        body.addClassName("grid-body"); // add this class

        card.add(head, body);
        return wrapContainer(card);
    }

    private Component wrapContainer(Component content) {
        Div container = new Div(content);
        container.addClassName("page-container");
        return container;
    }

    /* ======================= CELLS ======================= */

    private Component nameCell(User user) {
        boolean isMe = isCurrentAdmin(user);

        Div cell = new Div();
        cell.addClassName("user-cell");

        Div left = new Div();
        left.addClassName("user-main");

        Span fullName = new Span((safe(user.getPrenom()) + " " + safe(user.getNom())).trim());
        fullName.addClassName("user-name");

        Span idLine = new Span("ID: " + user.getId());
        idLine.addClassName("user-meta");

        left.add(fullName, idLine);

        Div right = new Div();
        right.addClassName("user-badges");

        if (isMe) {
            Span me = new Span("Vous");
            me.addClassName("badge");
            me.addClassName("badge-me");
            right.add(me);
        }

        cell.add(left, right);
        return cell;
    }

    private Component statusPill(User user) {
        Span pill = new Span(user.getActif() != null && user.getActif() ? "Actif" : "Inactif");
        pill.addClassName("pill");
        pill.addClassName(user.getActif() != null && user.getActif() ? "pill-active" : "pill-inactive");
        return pill;
    }

    private Component rolePill(User user) {
        String label = user.getRole() != null ? user.getRole().getLabel() : "—";
        Span pill = new Span(label);
        pill.addClassName("pill");
        pill.addClassName("pill-role");

        if (user.getRole() == UserRole.ADMIN) pill.addClassName("role-admin");
        else if (user.getRole() == UserRole.ORGANIZER) pill.addClassName("role-organizer");
        else pill.addClassName("role-client");

        return pill;
    }

    private Component actionsCell(User user) {
        boolean isMe = isCurrentAdmin(user);

        HorizontalLayout actions = new HorizontalLayout();
        actions.addClassName("actions-wrap");
        actions.setSpacing(true);

        // Edit
        Button editButton = new Button("Modifier", VaadinIcon.EDIT.create());
        editButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        editButton.addClassName("btn");
        editButton.addClickListener(e -> openEditUserDialog(user));

        // Toggle
        Button toggleButton = new Button(
                user.getActif() != null && user.getActif() ? "Désactiver" : "Activer",
                user.getActif() != null && user.getActif() ? VaadinIcon.BAN.create() : VaadinIcon.CHECK.create()
        );
        toggleButton.addThemeVariants(user.getActif() != null && user.getActif()
                ? ButtonVariant.LUMO_ERROR
                : ButtonVariant.LUMO_SUCCESS
        );
        toggleButton.addClassName("btn");

        if (isMe) {
            toggleButton.setEnabled(false);
            toggleButton.getElement().setProperty("title", "C’est votre compte — vous ne pouvez pas le désactiver.");
            toggleButton.addClickListener(e -> showError("C’est votre compte — vous ne pouvez pas le désactiver."));
        } else {
            toggleButton.addClickListener(e -> toggleUserStatus(user));
        }

        // Change role (only for non-admin users + (recommended) not yourself)
        if (user.getRole() != UserRole.ADMIN) {
            Button roleButton = new Button("Rôle", VaadinIcon.USER.create());
            roleButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            roleButton.addClassName("btn");

            // Optional safety: do not change your own role
            if (isMe) {
                roleButton.setEnabled(false);
                roleButton.getElement().setProperty("title", "C’est votre compte — rôle non modifiable ici.");
            } else {
                roleButton.addClickListener(e -> changeUserRole(user));
            }

            actions.add(editButton, toggleButton, roleButton);
        } else {
            actions.add(editButton, toggleButton);
        }

        return actions;
    }

    /* ======================= DATA / LOGIC ======================= */

    private void loadUsers() {
        allUsers = userService.getUsersWithFilters(null, null, null, null);
        allUsers = pinCurrentAdminFirst(allUsers);

        dataProvider = new ListDataProvider<>(allUsers);
        userGrid.setDataProvider(dataProvider);
    }

    private void filterUsers() {
        String search = safe(searchField.getValue());
        UserRole role = roleFilter.getValue();
        Boolean status = statusFilter.getValue();

        List<User> filtered = userService.getUsersWithFilters(
                !search.isBlank() ? search : null,
                null,
                role,
                status
        );

        if (!search.isBlank()) {
            String lower = search.toLowerCase();
            filtered = filtered.stream()
                    .filter(u -> safe(u.getNom()).toLowerCase().contains(lower)
                            || safe(u.getPrenom()).toLowerCase().contains(lower)
                            || safe(u.getEmail()).toLowerCase().contains(lower))
                    .toList();
        }

        filtered = pinCurrentAdminFirst(filtered);
        dataProvider = new ListDataProvider<>(filtered);
        userGrid.setDataProvider(dataProvider);
    }

    private List<User> pinCurrentAdminFirst(List<User> users) {
        if (currentAdminId == null) return users;

        return users.stream()
                .sorted(Comparator
                        .comparing((User u) -> !currentAdminId.equals(u.getId())) // false first => me on top
                        .thenComparing(User::getId))
                .toList();
    }

    private void toggleUserStatus(User user) {
        if (isCurrentAdmin(user)) {
            showError("C’est votre compte — vous ne pouvez pas le désactiver.");
            return;
        }

        try {
            boolean next = !(user.getActif() != null && user.getActif());
            userService.toggleAccountStatus(user.getId(), next);
            showSuccess("Statut de l'utilisateur mis à jour");
            loadUsers();
        } catch (Exception e) {
            showError("Erreur: " + e.getMessage());
        }
    }

    private void changeUserRole(User user) {
        if (isCurrentAdmin(user)) {
            showError("C’est votre compte — vous ne pouvez pas changer votre rôle ici.");
            return;
        }

        ComboBox<UserRole> roleCombo = new ComboBox<>("Nouveau rôle");
        roleCombo.setItems(UserRole.values());
        roleCombo.setItemLabelGenerator(UserRole::getLabel);
        roleCombo.setValue(user.getRole());

        VerticalLayout dialogContent = new VerticalLayout(roleCombo);
        dialogContent.setSpacing(true);

        Dialog dialog = new Dialog();
        dialog.addClassName("admin-dialog");
        dialog.setHeaderTitle("Changer le rôle de " + safe(user.getPrenom()) + " " + safe(user.getNom()));

        Button saveButton = new Button("Enregistrer", VaadinIcon.CHECK.create());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> {
            try {
                userService.updateUserRole(user.getId(), roleCombo.getValue());
                showSuccess("Rôle mis à jour avec succès");
                dialog.close();
                loadUsers();
            } catch (Exception ex) {
                showError("Erreur: " + ex.getMessage());
            }
        });

        Button cancelButton = new Button("Annuler", VaadinIcon.CLOSE.create());
        cancelButton.addClickListener(e -> dialog.close());

        dialogContent.add(new HorizontalLayout(saveButton, cancelButton));
        dialog.add(dialogContent);
        dialog.open();
    }

    /* ======================= EDIT DIALOG ======================= */

    private void openEditUserDialog(User user) {
        Dialog dialog = new Dialog();
        dialog.addClassName("admin-dialog");
        dialog.setHeaderTitle("Modifier l'utilisateur");

        TextField nomField = new TextField("Nom");
        TextField prenomField = new TextField("Prénom");
        EmailField emailField = new EmailField("Email");
        TextField telephoneField = new TextField("Téléphone");

        nomField.setValue(safe(user.getNom()));
        prenomField.setValue(safe(user.getPrenom()));
        emailField.setValue(safe(user.getEmail()));
        telephoneField.setValue(safe(user.getTelephone()));

        Binder<User> binder = new Binder<>(User.class);

        binder.forField(nomField)
                .asRequired("Nom obligatoire")
                .bind(User::getNom, User::setNom);

        binder.forField(prenomField)
                .asRequired("Prénom obligatoire")
                .bind(User::getPrenom, User::setPrenom);

        binder.forField(emailField)
                .asRequired("Email obligatoire")
                .withValidator(email -> email.contains("@"), "Email invalide")
                .bind(User::getEmail, User::setEmail);

        binder.forField(telephoneField)
                .bind(User::getTelephone, User::setTelephone);

        User temp = new User();
        temp.setNom(user.getNom());
        temp.setPrenom(user.getPrenom());
        temp.setEmail(user.getEmail());
        temp.setTelephone(user.getTelephone());
        binder.setBean(temp);

        FormLayout form = new FormLayout(nomField, prenomField, emailField, telephoneField);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        Button save = new Button("Enregistrer", VaadinIcon.CHECK.create());
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(e -> {
            if (!binder.validate().isOk()) return;

            try {
                userService.updateProfile(
                        user.getId(),
                        temp.getNom(),
                        temp.getPrenom(),
                        temp.getEmail(),
                        temp.getTelephone()
                );
                showSuccess("Utilisateur mis à jour");
                dialog.close();
                loadUsers();
            } catch (Exception ex) {
                showError("Erreur: " + ex.getMessage());
            }
        });

        Button cancel = new Button("Annuler", VaadinIcon.CLOSE.create(), e -> dialog.close());
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout buttons = new HorizontalLayout(save, cancel);
        buttons.addClassName("dialog-actions");

        VerticalLayout content = new VerticalLayout(form, buttons);
        content.setPadding(false);
        content.setSpacing(true);

        dialog.add(content);
        dialog.open();
    }

    /* ======================= HELPERS ======================= */

    private boolean isCurrentAdmin(User user) {
        return currentAdminId != null && user != null && currentAdminId.equals(user.getId());
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private void showSuccess(String message) {
        Notification.show(message, 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String message) {
        Notification.show(message, 3500, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
