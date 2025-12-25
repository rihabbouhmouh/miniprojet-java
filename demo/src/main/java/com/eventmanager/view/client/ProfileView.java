package com.eventmanager.view.client;

import com.eventmanager.entity.User;
import com.eventmanager.enums.UserRole;
import com.eventmanager.security.AuthenticatedUser;
import com.eventmanager.security.NavigationManager;
import com.eventmanager.service.IUserService;
import com.eventmanager.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.time.format.DateTimeFormatter;

@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Profil Utilisateur - Event Manager")
@AnonymousAllowed
public class ProfileView extends VerticalLayout {

    private final IUserService userService;
    private final AuthenticatedUser authenticatedUser;
    private final NavigationManager navigationManager;

    private User user;
    private Long userId;
    private boolean isAdmin;
    private boolean isEditing = false;

    private TextField nomField;
    private TextField prenomField;
    private EmailField emailField;
    private TextField telephoneField;
    private ComboBox<UserRole> roleComboBox;
    private ComboBox<Boolean> actifComboBox;
    private Paragraph dateInscriptionField;
    private Paragraph emailDisplayField;

    private Button editButton;
    private Button saveButton;
    private Button cancelButton;
    private Button backButton;

    private Binder<User> binder;

    public ProfileView(IUserService userService,
                       AuthenticatedUser authenticatedUser,
                       NavigationManager navigationManager) {
        this.userService = userService;
        this.authenticatedUser = authenticatedUser;
        this.navigationManager = navigationManager;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        
        // Initialize on construction
        this.isAdmin = authenticatedUser.get()
                .map(u -> u.getRole() == UserRole.ADMIN)
                .orElse(false);

        authenticatedUser.get().ifPresentOrElse(
                currentUser -> {
                    this.userId = currentUser.getId();
                    loadUser(currentUser.getId());
                },
                () -> {
                    showError("Vous devez être connecté");
                    navigationManager.navigateToLogin();
                }
        );
    }

    private void loadUser(Long userId) {
        try {
            this.user = userService.getUserById(userId);
            
            // Check permissions: user can only edit their own profile unless they're admin
            if (!isAdmin) {
                authenticatedUser.getUserId().ifPresent(currentUserId -> {
                    if (!currentUserId.equals(userId)) {
                        showError("Vous n'avez pas la permission de modifier ce profil");
                        navigationManager.navigateToProfile();
                        return;
                    }
                });
            }

            createProfileView();
        } catch (Exception e) {
            showError("Utilisateur non trouvé");
            navigationManager.navigateToHome();
        }
    }

    private void createProfileView() {
        removeAll();

        // Back button
        backButton = new Button("Retour", VaadinIcon.ARROW_LEFT.create());
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.addClickListener(e -> {
            if (isAdmin && userId != null) {
                navigationManager.navigateTo("admin/users");
            } else {
                navigationManager.navigateToHome();
            }
        });

        // Title
        H2 title = new H2(isAdmin && userId != null ? 
                "👤 Profil Utilisateur (Admin)" : "👤 Mon Profil");
        title.getStyle().set("margin", "0 0 var(--lumo-space-l) 0");

        // Edit button (only for own profile or admin)
        editButton = new Button("Modifier", VaadinIcon.EDIT.create());
        editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editButton.addClickListener(e -> enableEditing());

        HorizontalLayout header = new HorizontalLayout(title, editButton);
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);

        // Form
        VerticalLayout formContainer = new VerticalLayout();
        formContainer.setPadding(true);
        formContainer.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-s)");

        createFormFields();
        createFormLayout();

        formContainer.add(createFormLayout());

        add(backButton, header, formContainer);
    }

    private void createFormFields() {
        nomField = new TextField("Nom");
        nomField.setWidthFull();
        nomField.setReadOnly(!isEditing);

        prenomField = new TextField("Prénom");
        prenomField.setWidthFull();
        prenomField.setReadOnly(!isEditing);

        emailField = new EmailField("Email");
        emailField.setWidthFull();
        emailField.setReadOnly(true); // Email should not be editable

        emailDisplayField = new Paragraph();
        emailDisplayField.getStyle()
                .set("margin", "0")
                .set("padding", "var(--lumo-space-s)");

        telephoneField = new TextField("Téléphone");
        telephoneField.setWidthFull();
        telephoneField.setReadOnly(!isEditing);



        roleComboBox = new ComboBox<>("Rôle");
        roleComboBox.setItems(UserRole.values());
        roleComboBox.setWidthFull();
        roleComboBox.setReadOnly(!isEditing || !isAdmin); // Only admin can change role

        actifComboBox = new ComboBox<>("Statut");
        actifComboBox.setItems(true, false);
        actifComboBox.setItemLabelGenerator(actif -> actif ? "Actif" : "Inactif");
        actifComboBox.setWidthFull();
        actifComboBox.setReadOnly(!isEditing || !isAdmin); // Only admin can change status

        dateInscriptionField = new Paragraph();
        dateInscriptionField.getStyle()
                .set("margin", "0")
                .set("padding", "var(--lumo-space-s)")
                .set("color", "var(--lumo-secondary-text-color)");
    }

    private VerticalLayout createFormLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(true);

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        formLayout.add(nomField, 2);
        formLayout.add(prenomField, 2);
        
        if (isEditing) {
            formLayout.add(emailField, 2);
        } else {
            formLayout.add(emailDisplayField, 2);
        }
        
        formLayout.add(telephoneField, 2);
        
        if (isAdmin) {
            formLayout.add(roleComboBox, 1);
            formLayout.add(actifComboBox, 1);
        }

        // Read-only fields
        H3 infoTitle = new H3("📅 Informations");
        infoTitle.getStyle().set("margin", "var(--lumo-space-m) 0 var(--lumo-space-s) 0");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");
        dateInscriptionField.setText("Date d'inscription: " + 
                (user.getDateInscription() != null ? 
                        user.getDateInscription().format(formatter) : "N/A"));

        // Action buttons
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);
        buttonLayout.setVisible(isEditing);

        saveButton = new Button("Enregistrer", VaadinIcon.CHECK.create());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> saveProfile());

        cancelButton = new Button("Annuler", VaadinIcon.CLOSE.create());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.addClickListener(e -> cancelEditing());

        buttonLayout.add(saveButton, cancelButton);

        layout.add(formLayout, infoTitle, dateInscriptionField, buttonLayout);

        // Bind data
        binder = new Binder<>(User.class);
        binder.bind(nomField, User::getNom, User::setNom);
        binder.bind(prenomField, User::getPrenom, User::setPrenom);
        binder.bind(emailField, User::getEmail, User::setEmail);
        binder.bind(telephoneField, User::getTelephone, User::setTelephone);
        if (isAdmin) {
            binder.bind(roleComboBox, User::getRole, User::setRole);
            binder.bind(actifComboBox, User::getActif, User::setActif);
        }
        binder.readBean(user);

        if (!isEditing) {
            emailDisplayField.setText("Email: " + user.getEmail());
        } else {
            emailField.setValue(user.getEmail());
        }

        return layout;
    }

    private void enableEditing() {
        isEditing = true;
        editButton.setVisible(false);
        nomField.setReadOnly(false);
        prenomField.setReadOnly(false);
        telephoneField.setReadOnly(false);

        if (isAdmin) {
            roleComboBox.setReadOnly(false);
            actifComboBox.setReadOnly(false);
        }
        createProfileView(); // Recreate to show save/cancel buttons
    }

    private void saveProfile() {
        try {
            if (binder.writeBeanIfValid(user)) {
                // Update profile
                userService.updateProfile(user.getId(), 
                        user.getNom(), 
                        user.getPrenom(),
                        user.getEmail(), 
                        user.getTelephone());

                // If admin, update role and status
                if (isAdmin) {
                    if (user.getRole() != null) {
                        userService.updateUserRole(user.getId(), user.getRole());
                    }
                    userService.toggleAccountStatus(user.getId(), user.getActif());
                }

                showSuccess("Profil mis à jour avec succès");
                isEditing = false;
                loadUser(user.getId()); // Reload to refresh view
            } else {
                showError("Veuillez corriger les erreurs dans le formulaire");
            }
        } catch (Exception e) {
            showError("Erreur lors de la mise à jour: " + e.getMessage());
        }
    }

    private void cancelEditing() {
        isEditing = false;
        loadUser(user.getId()); // Reload to reset view
    }

    private void showSuccess(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}