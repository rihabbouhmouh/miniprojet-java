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
@PageTitle("Connexion")
@AnonymousAllowed
public class LoginView extends VerticalLayout {

    private final IUserService userService;  // CHANGEMENT ICI : UserService → IUserService
    private final NavigationManager navigationManager;
    private final AuthenticatedUser authenticatedUser;

    private EmailField emailField;
    private PasswordField passwordField;
    private Button loginButton;

    public LoginView(IUserService userService,  // CHANGEMENT ICI : UserService → IUserService
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
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        createLoginForm();
    }

    private void createLoginForm() {
        VerticalLayout formLayout = new VerticalLayout();
        formLayout.setWidth("400px");
        formLayout.setPadding(true);
        formLayout.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-s)");

        H1 title = new H1("🎭 Event Manager");
        title.getStyle().set("margin", "0").set("text-align", "center");

        Paragraph subtitle = new Paragraph("Connectez-vous pour gérer vos événements");
        subtitle.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("text-align", "center")
                .set("margin-top", "0");

        emailField = new EmailField("Email");
        emailField.setWidthFull();
        emailField.setPlaceholder("votre@email.com");
        emailField.setRequired(true);
        emailField.setErrorMessage("Email invalide");

        passwordField = new PasswordField("Mot de passe");
        passwordField.setWidthFull();
        passwordField.setPlaceholder("Votre mot de passe");
        passwordField.setRequired(true);

        loginButton = new Button("Se connecter");
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginButton.setWidthFull();
        loginButton.addClickListener(e -> handleLogin());

        passwordField.addKeyPressListener(event -> {
            if (event.getKey().getKeys().contains("Enter")) {
                handleLogin();
            }
        });

        RouterLink registerLink = new RouterLink("Créer un compte", RegisterView.class);
        Span registerText = new Span(new Span("Pas encore de compte ? "), registerLink);
        registerText.getStyle().set("text-align", "center");

        VerticalLayout testInfo = createTestInfo();

        formLayout.add(title, subtitle, emailField, passwordField, loginButton, registerText, testInfo);

        add(formLayout);
    }

    private VerticalLayout createTestInfo() {
        VerticalLayout testInfo = new VerticalLayout();
        testInfo.setPadding(true);
        testInfo.getStyle()
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("margin-top", "var(--lumo-space-m)");

        Paragraph title = new Paragraph("🔑 Comptes de test :");
        title.getStyle().set("font-weight", "bold").set("margin", "0");

        Paragraph admin = new Paragraph("Admin: admin@event.ma / admin123");
        admin.getStyle().set("margin", "5px 0").set("font-size", "var(--lumo-font-size-s)");

        Paragraph org = new Paragraph("Organisateur: organizer1@event.ma / org123");
        org.getStyle().set("margin", "5px 0").set("font-size", "var(--lumo-font-size-s)");

        Paragraph client = new Paragraph("Client: client1@event.ma / client123");
        client.getStyle().set("margin", "5px 0").set("font-size", "var(--lumo-font-size-s)");

        testInfo.add(title, admin, org, client);
        return testInfo;
    }

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
}