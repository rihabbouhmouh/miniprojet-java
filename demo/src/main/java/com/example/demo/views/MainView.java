package com.example.demo.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("") // Route principale : http://localhost:8080
public class MainView extends VerticalLayout {

    public MainView() {
        add(new H1("Bienvenue dans mon application Vaadin !"));
        // Ajoute d'autres composants ici
    }
}