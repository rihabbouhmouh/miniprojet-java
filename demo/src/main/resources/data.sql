-- ============================================
-- Données initiales : Utilisateurs, Événements, Réservations
-- Projet : Plateforme de gestion d'événements culturels
-- Base de données : H2
-- ============================================

-- ============================
-- UTILISATEURS
-- ============================
-- Mots de passe hashés avec BCrypt :
-- admin123 -> $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
-- org123   -> $2a$10$VEjxo0jq2YT5J1WbEIKbke5NkX7PHe7rljKzWLgX5LNjVTjhWtUgi
-- client123-> $2a$10$2aMCqHWZhEh1H6J6e9WEduYXxaLFJg3JMQjXm/GFLHjBvEDHZXHNy

-- CORRECTION : Remplacez 'role' par 'user_role'
INSERT INTO users (nom, prenom, email, password, user_role, date_inscription, actif, telephone) VALUES
('Admin', 'Systeme', 'admin@event.ma', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', NOW(), TRUE, '0611223344'),
('El Amrani', 'Karim', 'organizer1@event.ma', '$2a$10$VEjxo0jq2YT5J1WbEIKbke5NkX7PHe7rljKzWLgX5LNjVTjhWtUgi', 'ORGANIZER', NOW(), TRUE, '0622334455'),
('Bennani', 'Leila', 'organizer2@event.ma', '$2a$10$VEjxo0jq2YT5J1WbEIKbke5NkX7PHe7rljKzWLgX5LNjVTjhWtUgi', 'ORGANIZER', NOW(), TRUE, '0633445566'),
('Nassiri', 'Omar', 'client1@event.ma', '$2a$10$2aMCqHWZhEh1H6J6e9WEduYXxaLFJg3JMQjXm/GFLHjBvEDHZXHNy', 'CLIENT', NOW(), TRUE, '0644556677'),
('Haddad', 'Sara', 'client2@event.ma', '$2a$10$2aMCqHWZhEh1H6J6e9WEduYXxaLFJg3JMQjXm/GFLHjBvEDHZXHNy', 'CLIENT', NOW(), TRUE, '0655667788');

-- ============================
-- ÉVÉNEMENTS - CONCERTS (3)
-- ============================
INSERT INTO events (titre, description, categorie, date_debut, date_fin, lieu, ville, capacite_max, prix_unitaire, image_url, organisateur_id, statut, date_creation, date_modification) VALUES
('Jazz à Casablanca', 'Concert de jazz international avec des artistes locaux et internationaux. Une soirée exceptionnelle dans un cadre prestigieux.', 'CONCERT',
 DATEADD('DAY', 10, NOW()), DATEADD('HOUR', 3, DATEADD('DAY', 10, NOW())),
 'Théâtre Mohamed V', 'Casablanca', 120, 250.0, 'https://images.unsplash.com/photo-1514320291840-2e0a9bf2a9ae', 2, 'PUBLIE', NOW(), NOW()),

('Rock Festival Maroc', 'Festival de rock avec les meilleurs groupes marocains et invités internationaux. Deux jours de musique non-stop !', 'CONCERT',
 DATEADD('DAY', 20, NOW()), DATEADD('DAY', 21, NOW()),
 'Stade Ibn Battouta', 'Tanger', 300, 180.0, 'https://images.unsplash.com/photo-1470229722913-7c0e2dbbafd3', 3, 'PUBLIE', NOW(), NOW()),

('Soirée Andalouse', 'Musique traditionnelle andalouse avec l''orchestre philharmonique de Fès.', 'CONCERT',
 DATEADD('DAY', 25, NOW()), DATEADD('HOUR', 2, DATEADD('DAY', 25, NOW())),
 'Salle des Fêtes', 'Fès', 200, 220.0, NULL, 2, 'BROUILLON', NOW(), NOW());

-- ============================
-- ÉVÉNEMENTS - THÉÂTRE (3)
-- ============================
INSERT INTO events (titre, description, categorie, date_debut, date_fin, lieu, ville, capacite_max, prix_unitaire, image_url, organisateur_id, statut, date_creation, date_modification) VALUES
('Le Malade Imaginaire', 'Pièce classique de Molière revisitée par la troupe nationale. Mise en scène moderne et innovante.', 'THEATRE',
 DATEADD('DAY', 15, NOW()), DATEADD('HOUR', 2, DATEADD('DAY', 15, NOW())),
 'Théâtre National Mohamed V', 'Rabat', 100, 150.0, NULL, 3, 'PUBLIE', NOW(), NOW()),

('Comédie Marocaine', 'Spectacle humoristique contemporain avec les meilleurs comédiens du royaume.', 'THEATRE',
 DATEADD('DAY', 30, NOW()), DATEADD('MINUTE', 90, DATEADD('DAY', 30, NOW())),
 'Complexe Culturel', 'Casablanca', 180, 120.0, NULL, 2, 'PUBLIE', NOW(), NOW()),

('La Famille Sous Pression', 'Pièce de théâtre moderne sur les défis de la société contemporaine.', 'THEATRE',
 DATEADD('DAY', 35, NOW()), DATEADD('HOUR', 2, DATEADD('DAY', 35, NOW())),
 'Centre Culturel', 'Marrakech', 150, 130.0, NULL, 3, 'BROUILLON', NOW(), NOW());

-- ============================
-- ÉVÉNEMENTS - CONFÉRENCES (3)
-- ============================
INSERT INTO events (titre, description, categorie, date_debut, date_fin, lieu, ville, capacite_max, prix_unitaire, image_url, organisateur_id, statut, date_creation, date_modification) VALUES
('Tech Summit 2025', 'Conférence internationale sur l''intelligence artificielle et les nouvelles technologies. Experts mondiaux et networking.', 'CONFERENCE',
 DATEADD('DAY', 45, NOW()), DATEADD('DAY', 46, NOW()),
 'Palais des Congrès', 'Rabat', 400, 500.0, 'https://images.unsplash.com/photo-1540575467063-178a50c2df87', 2, 'PUBLIE', NOW(), NOW()),

('Santé Digitale au Maroc', 'Conférence médicale sur la transformation numérique du secteur de la santé.', 'CONFERENCE',
 DATEADD('DAY', 50, NOW()), DATEADD('DAY', 51, NOW()),
 'Hôpital Universitaire', 'Casablanca', 250, 350.0, NULL, 3, 'ANNULE', NOW(), NOW()),

('Énergies Renouvelables', 'Débat national sur la transition énergétique et les énergies vertes au Maroc.', 'CONFERENCE',
 DATEADD('DAY', 60, NOW()), DATEADD('HOUR', 4, DATEADD('DAY', 60, NOW())),
 'Centre Écologique', 'Tanger', 300, 300.0, NULL, 2, 'PUBLIE', NOW(), NOW());

-- ============================
-- ÉVÉNEMENTS - SPORT (3)
-- ============================
INSERT INTO events (titre, description, categorie, date_debut, date_fin, lieu, ville, capacite_max, prix_unitaire, image_url, organisateur_id, statut, date_creation, date_modification) VALUES
('Tournoi de Tennis Amateur', 'Compétition amicale ouverte à tous les niveaux. Inscriptions sur place.', 'SPORT',
 DATEADD('DAY', 18, NOW()), DATEADD('DAY', 19, NOW()),
 'Club Olympique', 'Marrakech', 80, 100.0, NULL, 3, 'PUBLIE', NOW(), NOW()),

('Marathon de Rabat', 'Course internationale de 42 km dans les rues historiques de Rabat. Catégories : Elite, Amateurs, Fun Run.', 'SPORT',
 DATEADD('DAY', 28, NOW()), DATEADD('HOUR', 6, DATEADD('DAY', 28, NOW())),
 'Boulevard Hassan II', 'Rabat', 500, 50.0, 'https://images.unsplash.com/photo-1452626038306-9aae5e071dd3', 2, 'PUBLIE', NOW(), NOW()),

('Championnat d''Échecs', 'Tournoi national d''échecs pour amateurs et professionnels. Prix attractifs.', 'SPORT',
 DATEADD('DAY', 70, NOW()), DATEADD('DAY', 71, NOW()),
 'Complexe Sportif', 'Fès', 60, 75.0, NULL, 3, 'BROUILLON', NOW(), NOW());

-- ============================
-- ÉVÉNEMENTS - AUTRE (3)
-- ============================
INSERT INTO events (titre, description, categorie, date_debut, date_fin, lieu, ville, capacite_max, prix_unitaire, image_url, organisateur_id, statut, date_creation, date_modification) VALUES
('Salon du Livre de Casablanca', 'Rencontres et dédicaces avec des auteurs marocains et internationaux. Plus de 100 exposants.', 'AUTRE',
 DATEADD('DAY', 12, NOW()), DATEADD('DAY', 14, NOW()),
 'Bibliothèque Nationale', 'Casablanca', 250, 60.0, NULL, 2, 'PUBLIE', NOW(), NOW()),

('Exposition d''Art Contemporain', 'Œuvres d''artistes marocains émergents. Peinture, sculpture, photographie.', 'AUTRE',
 DATEADD('DAY', 8, NOW()), DATEADD('DAY', 10, NOW()),
 'Galerie des Arts', 'Tanger', 120, 80.0, 'https://images.unsplash.com/photo-1460661419201-fd4cecdf8a8b', 3, 'PUBLIE', NOW(), NOW()),

('Atelier de Calligraphie Arabe', 'Initiation à l''art de la calligraphie arabe traditionnelle avec un maître calligraphe.', 'AUTRE',
 DATEADD('DAY', -5, NOW()), DATEADD('DAY', -4, NOW()),
 'Maison de la Culture', 'Fès', 50, 100.0, NULL, 2, 'TERMINE', NOW(), NOW());

-- ============================
-- RÉSERVATIONS (20)
-- ============================
INSERT INTO reservations (utilisateur_id, evenement_id, nombre_places, montant_total, date_reservation, statut, code_reservation, commentaire) VALUES
-- Réservations Client 1 (Omar - id 4)
(4, 1, 2, 500.0, NOW(), 'CONFIRMEE', 'EVT-A1B2C3', 'Deux places côte à côte si possible'),
(4, 4, 1, 150.0, NOW(), 'CONFIRMEE', 'EVT-D4E5F6', 'Place au premier rang préférée'),
(4, 7, 1, 500.0, NOW(), 'EN_ATTENTE', 'EVT-G7H8I9', 'En attente de confirmation'),
(4, 10, 2, 200.0, NOW(), 'CONFIRMEE', 'EVT-J1K2L3', NULL),
(4, 13, 1, 60.0, NOW(), 'CONFIRMEE', 'EVT-M4N5O6', NULL),
(4, 2, 2, 360.0, NOW(), 'CONFIRMEE', 'EVT-P7Q8R9', NULL),
(4, 5, 2, 240.0, NOW(), 'ANNULEE', 'EVT-S1T2U3', 'Changement de programme'),
(4, 14, 2, 160.0, NOW(), 'CONFIRMEE', 'EVT-V4W5X6', NULL),
(4, 9, 1, 300.0, NOW(), 'EN_ATTENTE', 'EVT-Y7Z8A9', NULL),
(4, 11, 1, 50.0, NOW(), 'CONFIRMEE', 'EVT-B1C2D3', NULL),

-- Réservations Client 2 (Sara - id 5)
(5, 2, 3, 540.0, NOW(), 'CONFIRMEE', 'EVT-E4F5G6', 'Pour mes amis et moi'),
(5, 5, 2, 240.0, NOW(), 'CONFIRMEE', 'EVT-H7I8J9', NULL),
(5, 8, 1, 350.0, NOW(), 'ANNULEE', 'EVT-K1L2M3', 'Événement annulé'),
(5, 9, 2, 600.0, NOW(), 'CONFIRMEE', 'EVT-N4O5P6', NULL),
(5, 11, 4, 200.0, NOW(), 'CONFIRMEE', 'EVT-Q7R8S9', 'Groupe de coureurs'),
(5, 14, 1, 80.0, NOW(), 'CONFIRMEE', 'EVT-T1U2V3', NULL),
(5, 1, 1, 250.0, NOW(), 'EN_ATTENTE', 'EVT-W4X5Y6', NULL),
(5, 10, 1, 100.0, NOW(), 'CONFIRMEE', 'EVT-Z7A8B9', NULL),
(5, 13, 3, 180.0, NOW(), 'CONFIRMEE', 'EVT-C1D2E3', NULL),
(5, 4, 2, 300.0, NOW(), 'CONFIRMEE', 'EVT-F4G5H6', NULL);

-- ============================
-- FIN DU SCRIPT
-- ============================