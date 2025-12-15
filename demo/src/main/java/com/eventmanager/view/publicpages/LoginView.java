package com.eventmanager.view.publicpages;

import com.eventmanager.security.AuthenticatedUser;
import com.eventmanager.security.NavigationManager;
import com.eventmanager.service.IUserService;  // CHANGEMENT ICI
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login")
@PageTitle("Connexion - Event Manager")
@AnonymousAllowed
public class LoginView extends VerticalLayout {

    private final IUserService userService;
    private final NavigationManager navigationManager;
    private final AuthenticatedUser authenticatedUser;

    private EmailField emailField;
    private PasswordField passwordField;
    private Button loginButton;

    public LoginView(IUserService userService,
                     NavigationManager navigationManager,
                     AuthenticatedUser authenticatedUser) {
        this.userService = userService;
        this.navigationManager = navigationManager;
        this.authenticatedUser = authenticatedUser;

        if (authenticatedUser.isAuthenticated()) {
            navigationManager.navigateToUserHome();
            return;
        }

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // Arrière-plan global
        getStyle()
                .set("background",
                        "linear-gradient(135deg, #667eea 0%, #764ba2 40%, #1b1b2f 100%)");

        createLoginLayout();
    }

    private void createLoginLayout() {
        // Conteneur principal centré
        VerticalLayout container = new VerticalLayout();
        container.setWidthFull();
        container.setMaxWidth("420px");
        container.setPadding(false);
        container.setSpacing(false);
        container.setAlignItems(Alignment.STRETCH);
        container.getStyle()
                .set("background", "rgba(255,255,255,0.04)")
                .set("border-radius", "20px")
                .set("backdrop-filter", "blur(14px)")
                .set("box-shadow", "0 18px 45px rgba(0,0,0,0.4)")
                .set("padding", "32px 28px")
                .set("border", "1px solid rgba(255,255,255,0.08)");

        // Header
        VerticalLayout header = new VerticalLayout();
        header.setPadding(false);
        header.setSpacing(false);
        header.setAlignItems(Alignment.CENTER);

        H1 title = new H1("🎭 Event Manager");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "2.2rem")
                .set("color", "white");

        Paragraph subtitle = new Paragraph("Connectez-vous");
        subtitle.getStyle()
                .set("color", "rgba(255,255,255,0.75)")
                .set("margin-top", "8px")
                .set("margin-bottom", "24px")
                .set("font-size", "0.95rem")
                .set("text-align", "center");

        header.add(title, subtitle);

        // Formulaire
        VerticalLayout form = new VerticalLayout();
        form.setPadding(false);
        form.setSpacing(true);

        emailField = new EmailField("Adresse email");
        emailField.setWidthFull();
        emailField.setPlaceholder("vous@example.com");
        emailField.setClearButtonVisible(true);
        emailField.setRequired(true);
        emailField.setErrorMessage("Email invalide");
        emailField.getStyle()
                .set("color", "white");
        emailField.getElement().getThemeList().add("small");
        styleField(emailField);

        passwordField = new PasswordField("Mot de passe");
        passwordField.setWidthFull();
        passwordField.setPlaceholder("Votre mot de passe");
        passwordField.setRequired(true);
        passwordField.getElement().getThemeList().add("small");
        styleField(passwordField);

        // Soumission avec Enter
        passwordField.addKeyDownListener(e -> {
            if ("Enter".equalsIgnoreCase(e.getKey().getKeys().toString())) {
                handleLogin();
            }
        });

        loginButton = new Button("Se connecter", VaadinIcon.SIGN_IN.create());
        loginButton.setWidthFull();
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginButton.getStyle()
                .set("margin-bottom", "20px")
                .set("font-weight", "600")
                .set("font-size", "1rem")
                .set("padding", "14px")
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("border", "none")
                .set("border-radius", "12px")
                .set("cursor", "pointer");
        loginButton.addClickListener(e -> handleLogin());

        // Lien vers inscription
        RouterLink registerLink = new RouterLink("Créer un compte", RegisterView.class);
        registerLink.getStyle()
                .set("color", "#ffd700")
                .set("font-weight", "500")
                .set("text-decoration", "none")
                .set("cursor", "pointer");

        Span registerText = new Span("Pas encore de compte ? ");
        Span registerWrapper = new Span(registerText, registerLink);
        registerWrapper.getStyle()
                .set("display", "block")
                .set("text-align", "center")
                .set("margin-top", "12px")
                .set("color", "rgba(255,255,255,0.75)");

        // Bloc comptes de test
        VerticalLayout testInfo = createTestInfo();

        form.add(emailField, passwordField, loginButton, registerWrapper, testInfo);

        container.add(header, form);
        add(container);
    }

    private VerticalLayout createTestInfo() {
        VerticalLayout testInfo = new VerticalLayout();
        testInfo.setPadding(true);
        testInfo.setSpacing(false);
        testInfo.getStyle()
                .set("background", "rgba(15,15,35,0.9)")
                .set("border-radius", "12px")
                .set("margin-top", "18px")
                .set("border", "1px solid rgba(255,255,255,0.08)");

        Paragraph title = new Paragraph("🔑 Comptes de test");
        title.getStyle()
                .set("font-weight", "600")
                .set("margin", "0 0 6px 0")
                .set("color", "white");

        Paragraph admin = new Paragraph("Admin : admin@event.ma / admin123");
        Paragraph org = new Paragraph("Organisateur : organizer1@event.ma / org123");
        Paragraph client = new Paragraph("Client : client1@event.ma / client123");

        for (Paragraph p : new Paragraph[]{admin, org, client}) {
            p.getStyle()
                    .set("margin", "2px 0")
                    .set("font-size", "0.8rem")
                    .set("color", "rgba(255,255,255,0.75)");
        }

        testInfo.add(title, admin, org, client);
        return testInfo;
    }

    // handleLogin(), showError(), showSuccess() restent identiques
    private void handleLogin() {
        String email = emailField.getValue();
        String password = passwordField.getValue();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs");
            return;
        }

        if (!emailField.isInvalid()) {
            loginButton.setEnabled(false);
            loginButton.setText("Connexion en cours...");

            try {
                var userOptional = userService.authenticate(email, password);

                if (userOptional.isPresent()) {
                    showSuccess("Connexion réussie !");
                    UI.getCurrent().getPage().executeJs(
                            "setTimeout(function(){ window.location.reload(); }, 500);"
                    );
                } else {
                    showError("Email ou mot de passe incorrect");
                    loginButton.setEnabled(true);
                    loginButton.setText("Se connecter");
                }
            } catch (Exception e) {
                showError("Erreur lors de la connexion: " + e.getMessage());
                loginButton.setEnabled(true);
                loginButton.setText("Se connecter");
            }
        }
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private void showSuccess(String message) {
        Notification notification = Notification.show(message, 2000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
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
}
