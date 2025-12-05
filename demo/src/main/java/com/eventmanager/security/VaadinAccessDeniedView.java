package com.eventmanager.security;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("access-denied")
@PageTitle("Accès Refusé")
@AnonymousAllowed
public class VaadinAccessDeniedView extends VerticalLayout {

    private final NavigationManager navigationManager;

    public VaadinAccessDeniedView(NavigationManager navigationManager) {
        this.navigationManager = navigationManager;

        setSpacing(true);
        setPadding(true);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setSizeFull();

        var icon = VaadinIcon.BAN.create();
        icon.setSize("100px");
        icon.setColor("var(--lumo-error-color)");

        H1 title = new H1("403 - Accès Refusé");
        title.getStyle()
                .set("color", "var(--lumo-error-color)")
                .set("margin", "0");

        Paragraph message = new Paragraph(
                "Vous n'avez pas les permissions nécessaires pour accéder à cette page."
        );
        message.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("text-align", "center");

        Button backButton = new Button("Retour à l'accueil", VaadinIcon.HOME.create());
        backButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        backButton.addClickListener(e -> navigationManager.navigateToHome());

        add(icon, title, message, backButton);
    }
}