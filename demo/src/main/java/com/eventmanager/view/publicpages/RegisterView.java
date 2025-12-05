package com.eventmanager.view.publicpages;

import com.eventmanager.enums.UserRole;
import com.eventmanager.security.NavigationManager;
import com.eventmanager.service.IUserService;  // CHANGEMENT ICI
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
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

@Route("register")
@PageTitle("Inscription")
@AnonymousAllowed
public class RegisterView extends VerticalLayout {

    private final IUserService userService;  // CHANGEMENT ICI
    private final NavigationManager navigationManager;

    private TextField nomField;
    private TextField prenomField;
    private EmailField emailField;
    private TextField telephoneField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private Button registerButton;

    private Binder<RegisterFormData> binder;

    public RegisterView(IUserService userService,  // CHANGEMENT ICI
                        NavigationManager navigationManager) {
        this.userService = userService;
        this.navigationManager = navigationManager;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        createRegistrationForm();
    }

    private void createRegistrationForm() {
        VerticalLayout formLayout = new VerticalLayout();
        formLayout.setWidth("450px");
        formLayout.setPadding(true);
        formLayout.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-s)");

        // Titre
        H1 title = new H1("Créer un compte");
        title.getStyle().set("margin", "0").set("text-align", "center");

        Paragraph subtitle = new Paragraph("Rejoignez Event Manager");
        subtitle.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("text-align", "center")
                .set("margin-top", "0");

        // Champs du formulaire
        nomField = new TextField("Nom");
        nomField.setWidthFull();
        nomField.setRequired(true);
        nomField.setPlaceholder("Votre nom");

        prenomField = new TextField("Prénom");
        prenomField.setWidthFull();
        prenomField.setRequired(true);
        prenomField.setPlaceholder("Votre prénom");

        emailField = new EmailField("Email");
        emailField.setWidthFull();
        emailField.setRequired(true);
        emailField.setPlaceholder("votre@email.com");
        emailField.setErrorMessage("Email invalide");

        telephoneField = new TextField("Téléphone");
        telephoneField.setWidthFull();
        telephoneField.setPlaceholder("06 12 34 56 78");

        passwordField = new PasswordField("Mot de passe");
        passwordField.setWidthFull();
        passwordField.setRequired(true);
        passwordField.setPlaceholder("Minimum 8 caractères");

        confirmPasswordField = new PasswordField("Confirmer le mot de passe");
        confirmPasswordField.setWidthFull();
        confirmPasswordField.setRequired(true);
        confirmPasswordField.setPlaceholder("Retapez votre mot de passe");

        // Binder pour la validation
        binder = new Binder<>(RegisterFormData.class);
        setupValidation();

        // Bouton d'inscription
        registerButton = new Button("S'inscrire");
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.setWidthFull();
        registerButton.addClickListener(e -> handleRegistration());

        // Lien vers connexion (CORRIGÉ ICI)
        RouterLink loginLink = new RouterLink("Se connecter", LoginView.class);
        Span loginText = new Span(
                new Text("Déjà un compte ? "),
                loginLink
        );
        loginText.getStyle().set("text-align", "center");

        formLayout.add(
                title,
                subtitle,
                nomField,
                prenomField,
                emailField,
                telephoneField,
                passwordField,
                confirmPasswordField,
                registerButton,
                loginText
        );

        add(formLayout);
    }

    private void setupValidation() {
        // Validation du nom
        binder.forField(nomField)
                .asRequired("Le nom est obligatoire")
                .withValidator(nom -> nom.length() >= 2, "Le nom doit contenir au moins 2 caractères")
                .bind(RegisterFormData::getNom, RegisterFormData::setNom);

        // Validation du prénom
        binder.forField(prenomField)
                .asRequired("Le prénom est obligatoire")
                .withValidator(prenom -> prenom.length() >= 2, "Le prénom doit contenir au moins 2 caractères")
                .bind(RegisterFormData::getPrenom, RegisterFormData::setPrenom);

        // Validation de l'email
        binder.forField(emailField)
                .asRequired("L'email est obligatoire")
                .withValidator(new EmailValidator("Email invalide"))
                .bind(RegisterFormData::getEmail, RegisterFormData::setEmail);

        // Validation du téléphone (optionnel)
        binder.forField(telephoneField)
                .bind(RegisterFormData::getTelephone, RegisterFormData::setTelephone);

        // Validation du mot de passe
        binder.forField(passwordField)
                .asRequired("Le mot de passe est obligatoire")
                .withValidator(password -> password.length() >= 8,
                        "Le mot de passe doit contenir au moins 8 caractères")
                .bind(RegisterFormData::getPassword, RegisterFormData::setPassword);

        // Validation de la confirmation
        binder.forField(confirmPasswordField)
                .asRequired("Veuillez confirmer le mot de passe")
                .withValidator(confirm -> confirm.equals(passwordField.getValue()),
                        "Les mots de passe ne correspondent pas")
                .bind(RegisterFormData::getConfirmPassword, RegisterFormData::setConfirmPassword);
    }

    private void handleRegistration() {
        RegisterFormData formData = new RegisterFormData();

        try {
            binder.writeBean(formData);

            // Désactiver le bouton pendant le traitement
            registerButton.setEnabled(false);
            registerButton.setText("Inscription en cours...");

            // Appel au service
            userService.registerUser(
                    formData.getNom(),
                    formData.getPrenom(),
                    formData.getEmail(),
                    formData.getPassword(),
                    formData.getTelephone(),
                    UserRole.CLIENT
            );

            showSuccess("Inscription réussie ! Vous pouvez maintenant vous connecter.");

            // Redirection vers login après 2 secondes
            getUI().ifPresent(ui -> ui.access(() -> {
                try {
                    Thread.sleep(2000);
                    navigationManager.navigateToLogin();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }));

        } catch (ValidationException e) {
            showError("Veuillez corriger les erreurs dans le formulaire");
            registerButton.setEnabled(true);
            registerButton.setText("S'inscrire");
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
            registerButton.setEnabled(true);
            registerButton.setText("S'inscrire");
        } catch (Exception e) {
            showError("Erreur lors de l'inscription : " + e.getMessage());
            registerButton.setEnabled(true);
            registerButton.setText("S'inscrire");
        }
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 4000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private void showSuccess(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    // Classe interne pour le formulaire
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