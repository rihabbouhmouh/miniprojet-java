package com.eventmanager.view;

import com.eventmanager.entity.User;
import com.eventmanager.enums.UserRole;
import com.eventmanager.security.AuthenticatedUser;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.menubar.MenuBar;
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
    private Span viewTitle;

    public MainLayout(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;

        addClassName("main-layout");
        setPrimarySection(Section.DRAWER);

        // Global app background feel
        getStyle().set("background", "#f5f7fa");

        createHeader();
        createDrawer();
    }

    private void createHeader() {
        // Left: toggle + brand + page title
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu");

        Div brand = new Div();
        brand.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "10px")
                .set("cursor", "pointer");

        Div logo = new Div();
        logo.setText("EH");
        logo.getStyle()
                .set("width", "34px")
                .set("height", "34px")
                .set("border-radius", "10px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("font-weight", "800")
                .set("background", "linear-gradient(135deg, #2563eb 0%, #7c3aed 100%)")
                .set("color", "white")
                .set("box-shadow", "0 6px 18px rgba(37, 99, 235, 0.18)");

        Span brandName = new Span("EventHub");
        brandName.getStyle()
                .set("font-weight", "800")
                .set("font-size", "1.05rem")
                .set("color", "#0f172a");

        brand.add(logo, brandName);
        brand.addClickListener(e -> UI.getCurrent().navigate("home"));

        viewTitle = new Span();
        viewTitle.getStyle()
                .set("font-weight", "700")
                .set("color", "#0f172a")
                .set("white-space", "nowrap")
                .set("overflow", "hidden")
                .set("text-overflow", "ellipsis")
                .set("max-width", "680px");

        // Right: user menu (or login/register)
        HorizontalLayout rightSide = buildRightSide();

        HorizontalLayout header = new HorizontalLayout(toggle, brand, viewTitle);
        header.setWidthFull();
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setSpacing(true);

        // Let the title take space, keep right side aligned
        header.expand(viewTitle);

        // Navbar styling (light, modern)
        header.getStyle()
                .set("background", "white")
                .set("border-bottom", "1px solid rgba(15, 23, 42, 0.08)")
                .set("box-shadow", "0 6px 18px rgba(15, 23, 42, 0.04)")
                .set("padding", "10px 14px");

        // Wrap to prevent overflow on small screens
        rightSide.getStyle().set("margin-left", "auto");
        header.add(rightSide);

        addToNavbar(header);
    }

    private HorizontalLayout buildRightSide() {
        HorizontalLayout right = new HorizontalLayout();
        right.setAlignItems(FlexComponent.Alignment.CENTER);
        right.setSpacing(true);

        Optional<User> maybeUser = authenticatedUser.get();

        if (maybeUser.isEmpty()) {
            Button login = new Button("Connexion");
            login.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            login.getStyle().set("font-weight", "600");
            login.addClickListener(e -> UI.getCurrent().navigate("login"));

            Button register = new Button("Inscription");
            register.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            register.getStyle()
                    .set("font-weight", "700")
                    .set("border-radius", "10px");
            register.addClickListener(e -> UI.getCurrent().navigate("register"));

            right.add(login, register);
            return right;
        }

        User user = maybeUser.get();
        String fullName = safeFullName(user);
        String initials = safeInitials(user);

        Avatar avatar = new Avatar(fullName);
        avatar.setAbbreviation(initials);
        avatar.getStyle()
                .set("box-shadow", "0 4px 10px rgba(15, 23, 42, 0.10)")
                .set("border", "2px solid rgba(37, 99, 235, 0.15)");

        Span name = new Span(fullName);
        name.getStyle()
                .set("font-weight", "700")
                .set("color", "#0f172a");

        Span roleBadge = new Span(user.getRole() != null ? user.getRole().name() : "USER");
        roleBadge.getStyle()
                .set("font-size", "0.72rem")
                .set("font-weight", "800")
                .set("padding", "4px 10px")
                .set("border-radius", "999px")
                .set("background", "rgba(37, 99, 235, 0.10)")
                .set("color", "#2563eb");

        // Dropdown menu (cleaner than having a logout button always visible)
        MenuBar menuBar = new MenuBar();
        menuBar.setOpenOnHover(false);
        menuBar.getStyle().set("border-radius", "12px");
        menuBar.getElement().getStyle().set("background", "transparent");

        MenuItem profileMenu = menuBar.addItem(avatar);
        profileMenu.getSubMenu().addItem("Mon profil", e -> UI.getCurrent().navigate("profile"));
        profileMenu.getSubMenu().addItem("Mes réservations", e -> UI.getCurrent().navigate("my-reservations"));

        if (user.getRole() == UserRole.ADMIN) {
            profileMenu.getSubMenu().addItem("Admin Dashboard", e -> UI.getCurrent().navigate("admin/dashboard"));
        } else if (user.getRole() == UserRole.ORGANIZER) {
            profileMenu.getSubMenu().addItem("Organisateur", e -> UI.getCurrent().navigate("organizer/dashboard"));
        }

        profileMenu.getSubMenu().addItem("Déconnexion", e -> UI.getCurrent().getPage().setLocation("/logout"));

        // Desktop display: name + role badge + avatar menu
        right.add(name, roleBadge, menuBar);
        right.getStyle().set("gap", "10px");

        return right;
    }

    private void createDrawer() {
        VerticalLayout drawerContent = new VerticalLayout();
        drawerContent.setSizeFull();
        drawerContent.setPadding(false);
        drawerContent.setSpacing(false);

        // Drawer Header (brand + small user card)
        Div drawerHeader = new Div();
        drawerHeader.getStyle()
                .set("padding", "16px 16px 10px 16px")
                .set("border-bottom", "1px solid rgba(15, 23, 42, 0.08)")
                .set("background", "white");

        H1 appName = new H1("EventHub");
        appName.getStyle()
                .set("margin", "0")
                .set("font-size", "1.25rem")
                .set("font-weight", "900")
                .set("color", "#0f172a");

        Span tagline = new Span("Découvrez • Réservez • Gérez");
        tagline.getStyle()
                .set("display", "block")
                .set("margin-top", "4px")
                .set("color", "#64748b")
                .set("font-size", "0.85rem")
                .set("font-weight", "600");

        drawerHeader.add(appName, tagline);

        Div userCard = buildDrawerUserCard();
        SideNav nav = createNavigation();

        Div navWrap = new Div(nav);
        navWrap.getStyle()
                .set("padding", "10px 10px 14px 10px")
                .set("background", "white");

        drawerContent.add(drawerHeader);
        if (userCard != null) drawerContent.add(userCard);
        drawerContent.add(navWrap);

        drawerContent.getStyle().set("background", "white");

        addToDrawer(drawerContent);
    }

    private Div buildDrawerUserCard() {
        Optional<User> maybeUser = authenticatedUser.get();
        if (maybeUser.isEmpty()) return null;

        User user = maybeUser.get();
        String fullName = safeFullName(user);
        String initials = safeInitials(user);

        Div card = new Div();
        card.getStyle()
                .set("padding", "12px 16px")
                .set("background", "white");

        Div box = new Div();
        box.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "12px")
                .set("padding", "12px")
                .set("border-radius", "14px")
                .set("border", "1px solid rgba(15, 23, 42, 0.08)")
                .set("box-shadow", "0 8px 18px rgba(15, 23, 42, 0.04)");

        Avatar avatar = new Avatar(fullName);
        avatar.setAbbreviation(initials);

        Div info = new Div();
        Span name = new Span(fullName);
        name.getStyle().set("font-weight", "800").set("color", "#0f172a");

        Span role = new Span(user.getRole() != null ? user.getRole().name() : "USER");
        role.getStyle().set("display", "block").set("color", "#64748b").set("font-size", "0.82rem");

        info.add(name, role);

        box.add(avatar, info);
        card.add(box);

        return card;
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();
        Optional<User> maybeUser = authenticatedUser.get();

        // Public
        nav.addItem(new SideNavItem("Accueil", "home", com.vaadin.flow.component.icon.VaadinIcon.HOME.create()));
        nav.addItem(new SideNavItem("Événements", "events", com.vaadin.flow.component.icon.VaadinIcon.CALENDAR.create()));

        if (maybeUser.isEmpty()) {
            nav.addItem(new SideNavItem("Connexion", "login", com.vaadin.flow.component.icon.VaadinIcon.SIGN_IN.create()));
            nav.addItem(new SideNavItem("Inscription", "register", com.vaadin.flow.component.icon.VaadinIcon.USER_CHECK.create()));
            return nav;
        }

        User user = maybeUser.get();
        UserRole role = user.getRole();

        // Common logged-in items
        nav.addItem(new SideNavItem("Mon tableau de bord", "dashboard", com.vaadin.flow.component.icon.VaadinIcon.DASHBOARD.create()));
        nav.addItem(new SideNavItem("Mes réservations", "my-reservations", com.vaadin.flow.component.icon.VaadinIcon.TICKET.create()));
        nav.addItem(new SideNavItem("Mon profil", "profile", com.vaadin.flow.component.icon.VaadinIcon.USER.create()));

        // Organizer group 
        if (role == UserRole.ORGANIZER) {
            SideNavItem organizerGroup = new SideNavItem("Organisateur", "", com.vaadin.flow.component.icon.VaadinIcon.BRIEFCASE.create());
            organizerGroup.addItem(new SideNavItem("Dashboard", "organizer/dashboard", com.vaadin.flow.component.icon.VaadinIcon.CHART.create()));
            organizerGroup.addItem(new SideNavItem("Mes événements", "organizer/events", com.vaadin.flow.component.icon.VaadinIcon.CALENDAR_USER.create()));
            organizerGroup.addItem(new SideNavItem("Créer un événement", "organizer/event/new", com.vaadin.flow.component.icon.VaadinIcon.PLUS_CIRCLE.create()));
            nav.addItem(organizerGroup);
        }

        // Admin group
        if (role == UserRole.ADMIN) {
            SideNavItem adminGroup = new SideNavItem("Administration","", com.vaadin.flow.component.icon.VaadinIcon.COG.create());
            adminGroup.addItem(new SideNavItem("Dashboard", "admin/dashboard", com.vaadin.flow.component.icon.VaadinIcon.CHART_GRID.create()));
            adminGroup.addItem(new SideNavItem("Utilisateurs", "admin/users", com.vaadin.flow.component.icon.VaadinIcon.USERS.create()));
            adminGroup.addItem(new SideNavItem("Événements", "admin/events", com.vaadin.flow.component.icon.VaadinIcon.CALENDAR_CLOCK.create()));
            adminGroup.addItem(new SideNavItem("Réservations", "admin/reservations", com.vaadin.flow.component.icon.VaadinIcon.RECORDS.create()));
            nav.addItem(adminGroup);
        }

        return nav;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        updateViewTitle();
    }

    private void updateViewTitle() {
        if (getContent() == null) return;

        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        if (title != null && title.value() != null) {
            viewTitle.setText(title.value());
        } else {
            viewTitle.setText("");
        }
    }

    private String safeFullName(User user) {
        String p = user.getPrenom() != null ? user.getPrenom().trim() : "";
        String n = user.getNom() != null ? user.getNom().trim() : "";
        String full = (p + " " + n).trim();
        return full.isEmpty() ? "Utilisateur" : full;
    }

    private String safeInitials(User user) {
        String p = user.getPrenom() != null ? user.getPrenom().trim() : "";
        String n = user.getNom() != null ? user.getNom().trim() : "";

        String a = !p.isEmpty() ? p.substring(0, 1) : "U";
        String b = !n.isEmpty() ? n.substring(0, 1) : "X";
        return (a + b).toUpperCase();
    }
}
