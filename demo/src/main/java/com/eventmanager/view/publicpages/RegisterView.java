package com.eventmanager.view.publicpages;

import com.eventmanager.enums.UserRole;
import com.eventmanager.security.AuthenticatedUser;
import com.eventmanager.security.NavigationManager;
import com.eventmanager.service.IUserService;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import io.netty.handler.codec.mqtt.MqttReasonCodes.Auth;

/**
 * Register View - Fixed version with proper spacing and label colors
 */
@Route("register")
@PageTitle("Inscription - Event Manager")
@AnonymousAllowed
public class RegisterView extends VerticalLayout {

    private final IUserService userService;
    private final NavigationManager navigationManager;
    private final AuthenticatedUser authenticatedUser;

    private TextField nomField;
    private TextField prenomField;
    private EmailField emailField;
    private TextField telephoneField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private Button registerButton;

    private Binder<RegisterFormData> binder;

    // Password strength indicator
    private Div passwordStrengthBar;
    private Span passwordStrengthText;

    public RegisterView(IUserService userService,
                        AuthenticatedUser authenticatedUser,
                        NavigationManager navigationManager) {
        this.userService = userService;
        this.authenticatedUser = authenticatedUser;
        this.navigationManager = navigationManager;

        // Redirect if already authenticated
        if (authenticatedUser.isAuthenticated()) {
            navigationManager.navigateToUserHome();
            return;
        }

        // Configure main layout styles
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        setMargin(false);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // Background gradient
        getStyle()
                .set("background",
                        "linear-gradient(135deg, #764ba2 0%, #667eea 40%, #1b1b2f 100%)")
                .set("min-height", "100vh")
                .set("overflow-y", "auto");

        createRegistrationLayout();
    }

    private void createRegistrationLayout() {
        // Main card container
        VerticalLayout container = new VerticalLayout();
        container.setWidthFull();
        container.setMaxWidth("700px");
        // testing the marging
        container.getStyle().set("margin-top", "70px");
        container.getStyle().set("padding-top", "70px");
        container.getStyle().set("margin-bottom", "50px");
        container.setPadding(false);
        container.setSpacing(false);
        container.setAlignItems(Alignment.STRETCH);
        container.getStyle()
                .set("background", "rgba(255, 255, 255, 0.05)")
                .set("border-radius", "24px")
                .set("backdrop-filter", "blur(16px)")
                .set("box-shadow", "0 20px 60px rgba(0, 0, 0, 0.5)")
                .set("padding", "40px 36px")
                .set("border", "1px solid rgba(255, 255, 255, 0.1)");
        // Header section
        VerticalLayout header = createHeader();

        // Form section
        VerticalLayout form = createForm();

        container.add(header, form);
        add(container);
    }

    /**
     * Create header
     */
    private VerticalLayout createHeader() {
        VerticalLayout header = new VerticalLayout();
        header.setPadding(false);
        header.setSpacing(false);
        header.setMargin(false);
        header.setAlignItems(Alignment.CENTER);
        header.getStyle().set("margin-bottom", "30px");

        // Emoji icon
        H1 title2 = new H1("🎭 Event Manager");
        title2.getStyle()
                .set("margin", "0")
                .set("font-size", "3rem")
                .set("color", "white");
        Span emoji = new Span("✨");
        emoji.getStyle()
                .set("font-size", "3.2rem")
                .set("display", "block")
                .set("font-weight", "700")
                .set("margin-bottom", "22px");

        // Title
        H2 title = new H2("Créer un compte");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "2rem")
                .set("font-weight", "500")
                .set("color", "white")
                .set("text-align", "center");

        // Subtitle
        Paragraph subtitle = new Paragraph("Rejoignez Event Manager pour gérer vos événements, réservations et statistiques.");
        subtitle.getStyle()
                .set("color", "rgba(255, 255, 255, 0.75)")
                .set("margin", "12px 0 0 0")
                .set("font-size", "0.95rem")
                .set("text-align", "center");

        header.add(emoji ,title2, title, subtitle);
        return header;
    }

    /**
     * Create form
     */
    private VerticalLayout createForm() {
        VerticalLayout form = new VerticalLayout();
        form.setPadding(false);
        form.setSpacing(false);
        form.setMargin(false);
        form.setWidthFull();

        // Name fields row
        HorizontalLayout nameRow = new HorizontalLayout();
        nameRow.setWidthFull();
        nameRow.setSpacing(true);
        nameRow.getStyle().set("margin-bottom", "16px");

        nomField = createTextField("Nom", "Votre nom");
        prenomField = createTextField("Prénom", "Votre prénom");

        nameRow.add(nomField, prenomField);

        // Email field
        emailField = new EmailField("Adresse email");
        emailField.setWidthFull();
        emailField.setRequired(true);
        emailField.setPlaceholder("vous@example.com");
        emailField.setErrorMessage("Email invalide");
        emailField.setClearButtonVisible(true);
        emailField.setPrefixComponent(VaadinIcon.ENVELOPE.create());
        emailField.getStyle().set("margin-bottom", "16px");
        styleField(emailField);

        // Phone field
        telephoneField = new TextField("Téléphone (optionnel)");
        telephoneField.setWidthFull();
        telephoneField.setPlaceholder("06 12 34 56 78");
        telephoneField.setPrefixComponent(VaadinIcon.PHONE.create());
        telephoneField.getStyle().set("margin-bottom", "16px");
        styleField(telephoneField);

        // Password container
        VerticalLayout passwordContainer = new VerticalLayout();
        passwordContainer.setPadding(false);
        passwordContainer.setSpacing(false);
        passwordContainer.setMargin(false);
        passwordContainer.setWidthFull();
        passwordContainer.getStyle().set("margin-bottom", "16px");

        passwordField = new PasswordField("Mot de passe");
        passwordField.setWidthFull();
        passwordField.setRequired(true);
        passwordField.setPlaceholder("Minimum 8 caractères");
        passwordField.setPrefixComponent(VaadinIcon.LOCK.create());
        styleField(passwordField);

        // Password strength bar
        passwordStrengthBar = new Div();
        passwordStrengthBar.getStyle()
                .set("width", "0%")
                .set("height", "4px")
                .set("border-radius", "2px")
                .set("background", "#4caf50")
                .set("margin-top", "8px")
                .set("transition", "all 0.3s ease");

        passwordStrengthText = new Span();
        passwordStrengthText.getStyle()
                .set("font-size", "0.85rem")
                .set("color", "rgba(255, 255, 255, 0.7)")
                .set("margin-top", "4px")
                .set("display", "block");

        passwordField.addValueChangeListener(e -> updatePasswordStrength(e.getValue()));

        passwordContainer.add(passwordField, passwordStrengthBar, passwordStrengthText);

        // Confirm password
        confirmPasswordField = new PasswordField("Confirmer le mot de passe");
        confirmPasswordField.setWidthFull();
        confirmPasswordField.setRequired(true);
        confirmPasswordField.setPlaceholder("Retapez votre mot de passe");
        confirmPasswordField.setPrefixComponent(VaadinIcon.LOCK.create());
        confirmPasswordField.getStyle().set("margin-bottom", "20px");
        styleField(confirmPasswordField);

        // Setup validation
        binder = new Binder<>(RegisterFormData.class);
        setupValidation();

        // Register button
        registerButton = new Button("S'inscrire");
        registerButton.setWidthFull();
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.setIcon(VaadinIcon.USER_CHECK.create());
        registerButton.setIconAfterText(true);
        registerButton.getStyle()
                .set("margin-bottom", "20px")
                .set("font-weight", "600")
                .set("font-size", "1rem")
                .set("padding", "14px")
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("border", "none")
                .set("border-radius", "12px")
                .set("cursor", "pointer");

        registerButton.addClickListener(e -> handleRegistration());

        // Login link
        RouterLink loginLink = new RouterLink("Se connecter", LoginView.class);
        loginLink.getStyle()
                .set("color", "#ffd700")
                .set("font-weight", "600")
                .set("text-decoration", "none");

        Span loginText = new Span(
                new Text("Déjà un compte ? "),
                loginLink
        );
        loginText.getStyle()
                .set("display", "block")
                .set("text-align", "center")
                .set("margin-bottom", "16px")
                .set("color", "rgba(255, 255, 255, 0.75)")
                .set("font-size", "0.95rem");

        // Security badge
        Div securityBadge = new Div();
        securityBadge.getStyle()
                .set("background", "rgba(255, 255, 255, 0.05)")
                .set("border-radius", "12px")
                .set("padding", "12px 16px")
                .set("border", "1px solid rgba(255, 255, 255, 0.08)")
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "8px");

        Span lockIcon = new Span("🔒");
        Span securityText = new Span("Données sécurisées et chiffrées");
        securityText.getStyle()
                .set("font-size", "0.85rem")
                .set("color", "rgba(255, 255, 255, 0.7)");

        securityBadge.add(lockIcon, securityText);

        // Add all to form
        form.add(
                nameRow,
                emailField,
                telephoneField,
                passwordContainer,
                confirmPasswordField,
                registerButton,
                loginText,
                securityBadge
        );

        return form;
    }

    /**
     * Create styled text field
     */
    private TextField createTextField(String label, String placeholder) {
        TextField field = new TextField(label);
        field.setWidthFull();
        field.setRequired(true);
        field.setPlaceholder(placeholder);
        field.setPrefixComponent(VaadinIcon.USER.create());
        styleField(field);
        return field;
    }

    /**
     * Style fields - FIXED LABELS
     */
    private void styleField(com.vaadin.flow.component.HasElement field) {
        field.getElement().getStyle()
                .set("--lumo-contrast-10pct", "rgba(255, 255, 255, 0.1)")
                .set("--lumo-contrast-20pct", "rgba(255, 255, 255, 0.15)")
                .set("--lumo-primary-text-color", "rgba(255, 255, 255, 0.9)")
                .set("--lumo-secondary-text-color", "rgba(255, 255, 255, 0.65)")
                .set("--lumo-body-text-color", "white")
                .set("--lumo-disabled-text-color", "rgba(255, 255, 255, 0.5)")
                .set("color", "white");
    }

    /**
     * Update password strength
     */
    private void updatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            passwordStrengthBar.getStyle().set("width", "0%");
            passwordStrengthText.setText("");
            return;
        }

        int strength = calculatePasswordStrength(password);
        String color, text, width;

        if (strength < 3) {
            color = "#f44336";
            text = "❌ Faible";
            width = "33%";
        } else if (strength < 5) {
            color = "#ff9800";
            text = "⚠️ Moyen";
            width = "66%";
        } else {
            color = "#4caf50";
            text = "✅ Fort";
            width = "100%";
        }

        passwordStrengthBar.getStyle()
                .set("background", color)
                .set("width", width);

        passwordStrengthText.setText(text);
        passwordStrengthText.getStyle().set("color", color);
    }

    /**
     * Calculate password strength
     */
    private int calculatePasswordStrength(String password) {
        int strength = 0;
        if (password.length() >= 8) strength++;
        if (password.length() >= 12) strength++;
        if (password.matches(".*[a-z].*")) strength++;
        if (password.matches(".*[A-Z].*")) strength++;
        if (password.matches(".*\\d.*")) strength++;
        if (password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) strength++;
        return strength;
    }

    /**
     * Setup validation
     */
    private void setupValidation() {
        binder.forField(nomField)
                .asRequired("Le nom est obligatoire")
                .withValidator(nom -> nom.length() >= 2, "Minimum 2 caractères")
                .bind(RegisterFormData::getNom, RegisterFormData::setNom);

        binder.forField(prenomField)
                .asRequired("Le prénom est obligatoire")
                .withValidator(prenom -> prenom.length() >= 2, "Minimum 2 caractères")
                .bind(RegisterFormData::getPrenom, RegisterFormData::setPrenom);

        binder.forField(emailField)
                .asRequired("L'email est obligatoire")
                .withValidator(new EmailValidator("Email invalide"))
                .bind(RegisterFormData::getEmail, RegisterFormData::setEmail);

        binder.forField(telephoneField)
                .bind(RegisterFormData::getTelephone, RegisterFormData::setTelephone);

        binder.forField(passwordField)
                .asRequired("Le mot de passe est obligatoire")
                .withValidator(password -> password.length() >= 8, "Minimum 8 caractères")
                .bind(RegisterFormData::getPassword, RegisterFormData::setPassword);

        binder.forField(confirmPasswordField)
                .asRequired("Confirmez le mot de passe")
                .withValidator(confirm -> confirm.equals(passwordField.getValue()),
                        "Les mots de passe ne correspondent pas")
                .bind(RegisterFormData::getConfirmPassword, RegisterFormData::setConfirmPassword);
    }

    /**
     * Handle registration
     */
    private void handleRegistration() {
        RegisterFormData formData = new RegisterFormData();

        try {
            binder.writeBean(formData);

            registerButton.setEnabled(false);
            registerButton.setText("Inscription...");

            userService.registerUser(
                    formData.getNom(),
                    formData.getPrenom(),
                    formData.getEmail(),
                    formData.getPassword(),
                    formData.getTelephone(),
                    UserRole.CLIENT
            );

            showSuccess("✅ Inscription réussie !");

            getUI().ifPresent(ui -> ui.access(() -> {
                try {
                    Thread.sleep(2000);
                    navigationManager.navigateToLogin();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }));

        } catch (ValidationException e) {
            showError("❌ Corrigez les erreurs");
            registerButton.setEnabled(true);
            registerButton.setText("S'inscrire");
        } catch (IllegalArgumentException e) {
            showError("❌ " + e.getMessage());
            registerButton.setEnabled(true);
            registerButton.setText("S'inscrire");
        } catch (Exception e) {
            showError("❌ Erreur : " + e.getMessage());
            registerButton.setEnabled(true);
            registerButton.setText("S'inscrire");
        }
    }

    private void showError(String message) {
        Notification.show(message, 4000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private void showSuccess(String message) {
        Notification.show(message, 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    /**
     * Form data
     */
    public static class RegisterFormData {
        private String nom;
        private String prenom;
        private String email;
        private String telephone;
        private String password;
        private String confirmPassword;

        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }

        public String getPrenom() { return prenom; }
        public void setPrenom(String prenom) { this.prenom = prenom; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getTelephone() { return telephone; }
        public void setTelephone(String telephone) { this.telephone = telephone; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getConfirmPassword() { return confirmPassword; }
        public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    }
}