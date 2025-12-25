package com.eventmanager.view.organizer;

import com.eventmanager.entity.Event;
import com.eventmanager.entity.User;
import com.eventmanager.enums.EventCategory;
import com.eventmanager.enums.EventStatus;
import com.eventmanager.security.AuthenticatedUser;
import com.eventmanager.service.IEventService;
import com.eventmanager.view.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;

@Route(value = "organizer/event/new", layout = MainLayout.class)
@PageTitle("Créer un Événement - Organisateur")
public class EventFormView extends VerticalLayout {

    private final IEventService eventService;
    private final User organizer;

    // Fields
    private TextField titre;
    private TextArea description;
    private ComboBox<EventCategory> categorie;
    private DateTimePicker dateDebut;
    private DateTimePicker dateFin;
    private TextField lieu;
    private TextField ville;
    private IntegerField capaciteMax;
    private NumberField prixUnitaire;

    // Image
    private TextField imageUrl;
    private Upload imageUpload;
    private Image previewImage;

    private final BeanValidationBinder<Event> binder = new BeanValidationBinder<>(Event.class);

    public EventFormView(AuthenticatedUser authenticatedUser, IEventService eventService) {
        this.eventService = eventService;

        this.organizer = authenticatedUser.get()
                .orElseThrow(() -> new IllegalStateException("Utilisateur non authentifié"));

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("➕ Créer un nouvel événement");
        title.addClassNames(LumoUtility.FontSize.XXXLARGE, LumoUtility.Margin.Bottom.LARGE);

        add(title);
        add(buildForm());
        add(buildButtons());
    }

    private VerticalLayout buildForm() {
        titre = new TextField("Titre");
        titre.setWidthFull();

        description = new TextArea("Description");
        description.setWidthFull();
        description.setMinHeight("140px");

        categorie = new ComboBox<>("Catégorie");
        categorie.setItems(EventCategory.values());
        categorie.setWidthFull();

        dateDebut = new DateTimePicker("Date début");
        dateFin = new DateTimePicker("Date fin");

        // Default values
        dateDebut.setValue(LocalDateTime.now().plusDays(7));
        dateFin.setValue(LocalDateTime.now().plusDays(7).plusHours(2));

        lieu = new TextField("Lieu");
        ville = new TextField("Ville");

        capaciteMax = new IntegerField("Capacité max");
        capaciteMax.setMin(1);
        capaciteMax.setStepButtonsVisible(true);

        prixUnitaire = new NumberField("Prix unitaire (DH)");
        prixUnitaire.setMin(0);
        prixUnitaire.setStep(0.01);      // allow decimals
        prixUnitaire.setWidthFull();



        // Image URL + upload
        imageUrl = new TextField("Image URL (optionnel)");
        imageUrl.setWidthFull();

        previewImage = new Image();
        previewImage.setWidth("260px");
        previewImage.setHeight("160px");
        previewImage.getStyle().set("object-fit", "cover");
        previewImage.getStyle().set("border-radius", "10px");
        previewImage.getStyle().set("border", "1px solid #ddd");
        previewImage.setVisible(false);

        MemoryBuffer buffer = new MemoryBuffer();
        imageUpload = new Upload(buffer);
        imageUpload.setAcceptedFileTypes("image/png", "image/jpeg", "image/jpg", "image/webp");
        imageUpload.setMaxFileSize(2 * 1024 * 1024); // 2MB
        imageUpload.setDropAllowed(true);
        imageUpload.setUploadButton(new Button("Uploader une image", VaadinIcon.UPLOAD.create()));
        imageUpload.setWidthFull();

        // ✅ IMPORTANT: create imageBox BEFORE using it in the form
        VerticalLayout imageBox = new VerticalLayout(imageUpload, previewImage);
        imageBox.setPadding(false);
        imageBox.setSpacing(true);
        imageBox.setWidthFull();

        imageUpload.addSucceededListener(e -> {
            try {
                byte[] bytes = buffer.getInputStream().readAllBytes();
                String mime = e.getMIMEType();

                String base64 = Base64.getEncoder().encodeToString(bytes);
                String dataUrl = "data:" + mime + ";base64," + base64;

                imageUrl.setValue(dataUrl);
                previewImage.setSrc(dataUrl);
                previewImage.setVisible(true);

                showSuccess("Image chargée avec succès");
            } catch (IOException ex) {
                showError("Erreur upload: " + ex.getMessage());
            }
        });

        imageUpload.addFileRejectedListener(e ->
                showError("Fichier refusé: " + e.getErrorMessage())
        );

        imageUrl.addValueChangeListener(e -> {
            String val = e.getValue();
            if (val != null && !val.isBlank()) {
                previewImage.setSrc(val);
                previewImage.setVisible(true);
            } else {
                previewImage.setVisible(false);
            }
        });

        FormLayout form = new FormLayout();
        form.setWidthFull();

        form.add(
                titre, categorie,
                ville, lieu,
                dateDebut, dateFin,
                capaciteMax, prixUnitaire,
                imageUrl, imageBox,
                description
        );

        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("900px", 2)
        );

        // Colspans
        form.setColspan(description, 2);
        form.setColspan(imageUrl, 2);
        form.setColspan(imageBox, 2);

        // ✅ Binder bindings
        binder.bind(titre, "titre");
        binder.bind(description, "description");
        binder.bind(categorie, "categorie");
        binder.bind(dateDebut, "dateDebut");
        binder.bind(dateFin, "dateFin");
        binder.bind(lieu, "lieu");
        binder.bind(ville, "ville");
        binder.bind(capaciteMax, "capaciteMax");
        binder.bind(prixUnitaire, "prixUnitaire");
        binder.bind(imageUrl, "imageUrl");

        binder.forField(prixUnitaire)
                .asRequired("Prix obligatoire")
                .withValidator(v -> v != null && !v.isNaN() && v >= 0, "Prix invalide")
                .bind(Event::getPrixUnitaire, Event::setPrixUnitaire);

        // Custom validation: dateFin > dateDebut
        binder.withValidator(ev -> {
            if (ev.getDateDebut() == null || ev.getDateFin() == null) return true;
            return ev.getDateFin().isAfter(ev.getDateDebut());
        }, "La date de fin doit être après la date de début");

        VerticalLayout wrapper = new VerticalLayout(form);
        wrapper.setPadding(false);
        wrapper.setSpacing(true);
        wrapper.setWidthFull();
        return wrapper;
    }

    private HorizontalLayout buildButtons() {
        Button saveDraft = new Button("Enregistrer (Brouillon)", VaadinIcon.CHECK.create());
        saveDraft.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button saveAndPublish = new Button("Enregistrer & Publier", VaadinIcon.ROCKET.create());
        saveAndPublish.addThemeVariants(ButtonVariant.LUMO_SUCCESS);

        Button cancel = new Button("Annuler", VaadinIcon.ARROW_LEFT.create());
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        saveDraft.addClickListener(e -> saveEvent(false));
        saveAndPublish.addClickListener(e -> saveEvent(true));
        cancel.addClickListener(e -> UI.getCurrent().navigate("organizer/events"));

        HorizontalLayout actions = new HorizontalLayout(saveDraft, saveAndPublish, cancel);
        actions.setWidthFull();
        actions.setJustifyContentMode(JustifyContentMode.END);
        actions.setAlignItems(Alignment.CENTER);
        actions.getStyle().set("flex-wrap", "wrap");
        return actions;
    }

    private void saveEvent(boolean publish) {
        Event event = new Event();
        try {
            binder.writeBean(event);

            event.setOrganisateur(organizer);
            event.setStatut(EventStatus.BROUILLON);

            Event created = eventService.createEvent(event, organizer.getId());

            if (publish) {
                eventService.changeEventStatus(created.getId(), EventStatus.PUBLIE);
            }

            showSuccess(publish ? "Événement créé et publié" : "Événement créé (brouillon)");
            UI.getCurrent().navigate("organizer/events");

        } catch (ValidationException ve) {
            showError("Veuillez corriger les erreurs du formulaire");
        } catch (Exception ex) {
            showError("Erreur: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void showSuccess(String msg) {
        Notification.show(msg, 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String msg) {
        Notification.show(msg, 5000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
