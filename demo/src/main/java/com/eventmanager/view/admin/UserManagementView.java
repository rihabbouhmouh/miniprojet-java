package com.eventmanager.view.admin;

import com.eventmanager.entity.User;
import com.eventmanager.enums.UserRole;
import com.eventmanager.service.IUserService;
import com.eventmanager.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "admin/users", layout = MainLayout.class)
@PageTitle("Gestion des Utilisateurs - Admin")
public class UserManagementView extends VerticalLayout {

    private final IUserService userService;
    private Grid<User> userGrid;
    private List<User> allUsers;
    private ListDataProvider<User> dataProvider;
    private TextField searchField;
    private ComboBox<UserRole> roleFilter;
    private ComboBox<Boolean> statusFilter;

    public UserManagementView(IUserService userService) {
        this.userService = userService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Header
        H2 title = new H2("👥 Gestion des Utilisateurs");
        title.addClassNames(LumoUtility.FontSize.XXXLARGE, LumoUtility.Margin.Bottom.LARGE);
        add(title);

        // Filters
        add(createFiltersSection());

        // Grid
        add(createUserGrid());

        // Load data
        loadUsers();
    }

    private HorizontalLayout createFiltersSection() {
        HorizontalLayout filters = new HorizontalLayout();
        filters.setWidthFull();
        filters.setSpacing(true);
        filters.setAlignItems(Alignment.END);

        // Search field
        searchField = new TextField();
        searchField.setPlaceholder("Rechercher par nom, prénom ou email...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setWidth("300px");
        searchField.addValueChangeListener(e -> filterUsers());

        // Role filter
        roleFilter = new ComboBox<>("Rôle");
        roleFilter.setItems(UserRole.values());
        roleFilter.setItemLabelGenerator(role -> role.getLabel());
        roleFilter.setClearButtonVisible(true);
        roleFilter.setWidth("200px");
        roleFilter.addValueChangeListener(e -> filterUsers());

        // Status filter
        statusFilter = new ComboBox<>("Statut");
        statusFilter.setItems(true, false);
        statusFilter.setItemLabelGenerator(status -> status ? "Actif" : "Inactif");
        statusFilter.setClearButtonVisible(true);
        statusFilter.setWidth("150px");
        statusFilter.addValueChangeListener(e -> filterUsers());

        // Refresh button
        Button refreshButton = new Button("Actualiser", VaadinIcon.REFRESH.create());
        refreshButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        refreshButton.addClickListener(e -> loadUsers());

        filters.add(searchField, roleFilter, statusFilter, refreshButton);
        filters.expand(searchField);

        return filters;
    }

    private Grid<User> createUserGrid() {
        userGrid = new Grid<>(User.class, false);
        userGrid.setSizeFull();

        userGrid.addColumn(User::getId).setHeader("ID").setWidth("80px").setFlexGrow(0);
        userGrid.addColumn(User::getNom).setHeader("Nom").setAutoWidth(true);
        userGrid.addColumn(User::getPrenom).setHeader("Prénom").setAutoWidth(true);
        userGrid.addColumn(User::getEmail).setHeader("Email").setAutoWidth(true);
        userGrid.addColumn(user -> user.getTelephone() != null ? user.getTelephone() : "-")
                .setHeader("Téléphone").setAutoWidth(true);
        userGrid.addColumn(user -> user.getRole().getLabel())
                .setHeader("Rôle").setAutoWidth(true);
        userGrid.addColumn(user -> user.getActif() ? "✅ Actif" : "❌ Inactif")
                .setHeader("Statut").setAutoWidth(true);
        userGrid.addColumn(user -> user.getDateInscription()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .setHeader("Date d'inscription").setAutoWidth(true);

        // Actions column
        userGrid.addComponentColumn(user -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);

            // ✅ Make actions look nicer: small buttons + wrap if needed
            actions.getStyle().set("flex-wrap", "wrap");
            actions.getStyle().set("gap", "0.25rem");

            // ✏️ EDIT BUTTON (NEW)
            Button editButton = new Button("Modifier", VaadinIcon.EDIT.create());
            editButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            editButton.addClickListener(e -> openEditUserDialog(user));
            actions.add(editButton);

            // Toggle status button
            Button toggleButton = new Button(
                    user.getActif() ? "Désactiver" : "Activer",
                    user.getActif() ? VaadinIcon.BAN.create() : VaadinIcon.CHECK.create()
            );
            toggleButton.addThemeVariants(
                    user.getActif() ? ButtonVariant.LUMO_ERROR : ButtonVariant.LUMO_SUCCESS
            );
            toggleButton.addClickListener(e -> toggleUserStatus(user));
            actions.add(toggleButton);

            // Change role button (only for non-admin users)
            if (user.getRole() != UserRole.ADMIN) {
                Button roleButton = new Button("Changer Rôle", VaadinIcon.USER.create());
                roleButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                roleButton.addClickListener(e -> changeUserRole(user));
                actions.add(roleButton);
            }

            return actions;
        }).setHeader("Actions").setAutoWidth(true);

        userGrid.setAllRowsVisible(true);
        return userGrid;
    }

    private void loadUsers() {
        allUsers = userService.getUsersWithFilters(null, null, null, null);
        dataProvider = new ListDataProvider<>(allUsers);
        userGrid.setDataProvider(dataProvider);
    }

    private void filterUsers() {
        String search = searchField.getValue();
        UserRole role = roleFilter.getValue();
        Boolean status = statusFilter.getValue();

        List<User> filtered = userService.getUsersWithFilters(
                search != null && !search.isEmpty() ? search : null,
                null,
                role,
                status
        );

        // Additional client-side filtering for search
        if (search != null && !search.isEmpty()) {
            String lowerSearch = search.toLowerCase();
            filtered = filtered.stream()
                    .filter(u -> u.getNom().toLowerCase().contains(lowerSearch) ||
                            u.getPrenom().toLowerCase().contains(lowerSearch) ||
                            u.getEmail().toLowerCase().contains(lowerSearch))
                    .toList();
        }

        dataProvider = new ListDataProvider<>(filtered);
        userGrid.setDataProvider(dataProvider);
    }

    private void toggleUserStatus(User user) {
        try {
            userService.toggleAccountStatus(user.getId(), !user.getActif());
            showSuccess("Statut de l'utilisateur mis à jour");
            loadUsers();
        } catch (Exception e) {
            showError("Erreur: " + e.getMessage());
        }
    }

    private void changeUserRole(User user) {
        // Simple role change dialog
        ComboBox<UserRole> roleCombo = new ComboBox<>("Nouveau rôle");
        roleCombo.setItems(UserRole.values());
        roleCombo.setItemLabelGenerator(UserRole::getLabel);
        roleCombo.setValue(user.getRole());

        VerticalLayout dialogContent = new VerticalLayout(roleCombo);
        dialogContent.setSpacing(true);

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Changer le rôle de " + user.getPrenom() + " " + user.getNom());

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

        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        dialogContent.add(buttons);
        dialog.add(dialogContent);
        dialog.open();
    }

    /* ===================== NEW: EDIT USER DIALOG ===================== */

    private void openEditUserDialog(User user) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Modifier l'utilisateur");

        // Fields
        TextField nomField = new TextField("Nom");
        TextField prenomField = new TextField("Prénom");
        EmailField emailField = new EmailField("Email");
        TextField telephoneField = new TextField("Téléphone");

        // Prefill
        nomField.setValue(user.getNom() != null ? user.getNom() : "");
        prenomField.setValue(user.getPrenom() != null ? user.getPrenom() : "");
        emailField.setValue(user.getEmail() != null ? user.getEmail() : "");
        telephoneField.setValue(user.getTelephone() != null ? user.getTelephone() : "");

        // Binder validation (NEW)
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

        // We bind to a copy in UI to avoid changing grid values before saving
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
                // updateUserBasicInfo(Long id, String nom, String prenom, String email , String telephone)
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

        VerticalLayout content = new VerticalLayout(form, buttons);
        content.setPadding(false);
        content.setSpacing(true);

        dialog.add(content);
        dialog.open();
    }

    private void showSuccess(String message) {
        Notification.show(message, 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String message) {
        Notification.show(message, 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
