package com.eventmanager.view;

import com.eventmanager.entity.User;
import com.eventmanager.enums.UserRole;
import com.eventmanager.security.AuthenticatedUser;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.Optional;

public class MainLayout extends AppLayout {

    private final AuthenticatedUser authenticatedUser;
    private H1 viewTitle;

    public MainLayout(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;

        setPrimarySection(Section.DRAWER);
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        viewTitle = new H1();
        viewTitle.addClassNames(
                LumoUtility.FontSize.LARGE,
                LumoUtility.Margin.NONE
        );

        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu");

        HorizontalLayout header = new HorizontalLayout(toggle, viewTitle);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(viewTitle);
        header.setWidthFull();
        header.addClassNames(
                LumoUtility.Padding.Vertical.NONE,
                LumoUtility.Padding.Horizontal.MEDIUM
        );

        Optional<User> maybeUser = authenticatedUser.get();
        if (maybeUser.isPresent()) {
            User user = maybeUser.get();

            Avatar avatar = new Avatar(user.getPrenom() + " " + user.getNom());
            String initials = user.getPrenom().substring(0, 1) + user.getNom().substring(0, 1);
            avatar.setAbbreviation(initials.toUpperCase());

            Span userName = new Span(user.getPrenom() + " " + user.getNom());
            userName.addClassNames(LumoUtility.FontSize.SMALL);

            Button logoutButton = new Button("Déconnexion", VaadinIcon.SIGN_OUT.create());
            logoutButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            logoutButton.addClickListener(e -> authenticatedUser.logout());

            HorizontalLayout userLayout = new HorizontalLayout(avatar, userName, logoutButton);
            userLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
            userLayout.setSpacing(true);

            header.add(userLayout);
        }

        addToNavbar(header);
    }

    private void createDrawer() {
        H1 appName = new H1("🎭 Event Manager");
        appName.addClassNames(
                LumoUtility.FontSize.LARGE,
                LumoUtility.Margin.NONE
        );

        VerticalLayout drawerContent = new VerticalLayout();
        drawerContent.setSizeFull();
        drawerContent.setPadding(false);
        drawerContent.setSpacing(false);

        drawerContent.add(appName);
        drawerContent.add(createNavigation());

        addToDrawer(drawerContent);
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();

        Optional<User> maybeUser = authenticatedUser.get();

        if (maybeUser.isEmpty()) {
            // Utilisation de chemins de route comme chaînes de caractères
            nav.addItem(new SideNavItem("Accueil", "", VaadinIcon.HOME.create()));
            nav.addItem(new SideNavItem("Événements", "events", VaadinIcon.CALENDAR.create()));
            nav.addItem(new SideNavItem("Connexion", "login", VaadinIcon.SIGN_IN.create()));
            nav.addItem(new SideNavItem("Inscription", "register", VaadinIcon.USER_CHECK.create()));
        } else {
            User user = maybeUser.get();
            UserRole userRole = user.getRole();

            nav.addItem(new SideNavItem("Accueil", "", VaadinIcon.HOME.create()));
            nav.addItem(new SideNavItem("Événements", "events", VaadinIcon.CALENDAR.create()));

            if (userRole == UserRole.CLIENT || userRole == UserRole.ORGANIZER || userRole == UserRole.ADMIN) {
                nav.addItem(new SideNavItem("Mon Tableau de Bord", "dashboard", VaadinIcon.DASHBOARD.create()));
                nav.addItem(new SideNavItem("Mes Réservations", "my-reservations", VaadinIcon.TICKET.create()));
                nav.addItem(new SideNavItem("Mon Profil", "profile", VaadinIcon.USER.create()));
            }

            if (userRole == UserRole.ORGANIZER || userRole == UserRole.ADMIN) {
                // CORRIGÉ : Utilisation du constructeur à 3 paramètres pour les menus parents
                SideNavItem organizerMenu = new SideNavItem("Organisateur", "", VaadinIcon.BRIEFCASE.create());
                organizerMenu.addItem(new SideNavItem("Tableau de Bord", "organizer/dashboard", VaadinIcon.CHART.create()));
                organizerMenu.addItem(new SideNavItem("Mes Événements", "organizer/events", VaadinIcon.CALENDAR_USER.create()));
                organizerMenu.addItem(new SideNavItem("Créer un Événement", "organizer/event/new", VaadinIcon.PLUS_CIRCLE.create()));
                nav.addItem(organizerMenu);
            }

            if (userRole == UserRole.ADMIN) {
                // CORRIGÉ : Utilisation du constructeur à 3 paramètres pour les menus parents
                SideNavItem adminMenu = new SideNavItem("Administration", "", VaadinIcon.COG.create());
                adminMenu.addItem(new SideNavItem("Dashboard Admin", "admin/dashboard", VaadinIcon.CHART_GRID.create()));
                adminMenu.addItem(new SideNavItem("Utilisateurs", "admin/users", VaadinIcon.USERS.create()));
                adminMenu.addItem(new SideNavItem("Tous les Événements", "admin/events", VaadinIcon.CALENDAR_CLOCK.create()));
                adminMenu.addItem(new SideNavItem("Toutes les Réservations", "admin/reservations", VaadinIcon.RECORDS.create()));
                nav.addItem(adminMenu);
            }
        }

        return nav;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        updateViewTitle();
    }

    private void updateViewTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        if (title != null) {
            viewTitle.setText(title.value());
        }
    }
}