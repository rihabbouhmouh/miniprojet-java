-- ============================================
-- Données initiales (H2) - EventHub
-- Utilisateurs (5) + Événements (15) + Réservations (20)
-- ============================================

-- ------------------------------------------------
-- Nettoyage (utile si tu relances sans drop schema)
-- ------------------------------------------------
DELETE FROM reservations;
DELETE FROM events;
DELETE FROM users;

-- ------------------------------------------------
-- UTILISATEURS (5 minimum)
-- 1 ADMIN     : admin@event.ma / admin123
-- 2 ORGANIZER : organizer1@event.ma, organizer2@event.ma / org123
-- 2 CLIENT    : client1@event.ma, client2@event.ma / client123
--
-- BCrypt hashes (stables):
-- admin123  -> $2a$10$vVQ4oC2wxivVKWyX/UB88uPk4ITI8QPj9ZsTx6empqwhELyavJCfq
-- org123    -> $2a$10$oDdpSZsTQqmclHy3oo8KlesYY7jFDXw5YTjVTPFSiZFAwgAJjWQAO
-- client123 -> $2a$10$hB4vu3mt6BhWkxaQSExNTeXurgptx4QR.5HHaA90pE0kLfJaL6/zS
-- ------------------------------------------------
INSERT INTO users (id, nom, prenom, email, password, user_role, date_inscription, actif, telephone) VALUES
(1, 'Admin', 'Systeme', 'admin@event.ma',
 '$2a$10$vVQ4oC2wxivVKWyX/UB88uPk4ITI8QPj9ZsTx6empqwhELyavJCfq',
 'ADMIN', CURRENT_TIMESTAMP, TRUE, '0611223344'),

(2, 'El Amrani', 'Karim', 'organizer1@event.ma',
 '$2a$10$oDdpSZsTQqmclHy3oo8KlesYY7jFDXw5YTjVTPFSiZFAwgAJjWQAO',
 'ORGANIZER', CURRENT_TIMESTAMP, TRUE, '0622334455'),

(3, 'Bennani', 'Leila', 'organizer2@event.ma',
 '$2a$10$oDdpSZsTQqmclHy3oo8KlesYY7jFDXw5YTjVTPFSiZFAwgAJjWQAO',
 'ORGANIZER', CURRENT_TIMESTAMP, TRUE, '0633445566'),

(4, 'Nassiri', 'Omar', 'client1@event.ma',
 '$2a$10$hB4vu3mt6BhWkxaQSExNTeXurgptx4QR.5HHaA90pE0kLfJaL6/zS',
 'CLIENT', CURRENT_TIMESTAMP, TRUE, '0644556677'),

(5, 'Haddad', 'Sara', 'client2@event.ma',
 '$2a$10$hB4vu3mt6BhWkxaQSExNTeXurgptx4QR.5HHaA90pE0kLfJaL6/zS',
 'CLIENT', CURRENT_TIMESTAMP, TRUE, '0655667788');

-- ------------------------------------------------
-- ÉVÉNEMENTS (15 minimum)
-- 3 CONCERT, 3 THEATRE, 3 CONFERENCE, 3 SPORT, 3 AUTRE
-- Mix statuts: BROUILLON, PUBLIE, ANNULE, TERMINE
-- Villes: Casablanca, Rabat, Marrakech, Tanger, Fès
-- Prix: 50 à 500 DH
-- Organisateurs: 2 et 3
-- ------------------------------------------------
INSERT INTO events (id, titre, description, categorie, date_debut, date_fin, lieu, ville,
                    capacite_max, prix_unitaire, image_url, organisateur_id, statut,
                    date_creation, date_modification)
VALUES
-- ===== CONCERT (1..3)
(1, 'Jazz Night Casablanca',
 'Une soirée jazz avec artistes locaux et internationaux.',
 'CONCERT',
 DATEADD('DAY', 7, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, DATEADD('DAY', 7, CURRENT_TIMESTAMP)),
 'Studio des Arts', 'Casablanca',
 200, 250.0, 'https://images.unsplash.com/photo-1514320291840-2e0a9bf2a9ae', 2, 'PUBLIE',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(2, 'Rock Festival Tanger',
 'Festival rock sur 2 jours avec groupes marocains & invités.',
 'CONCERT',
 DATEADD('DAY', 20, CURRENT_TIMESTAMP), DATEADD('DAY', 21, CURRENT_TIMESTAMP),
 'Stade Ibn Battouta', 'Tanger',
 800, 180.0, 'https://images.unsplash.com/photo-1470229722913-7c0e2dbbafd3', 3, 'BROUILLON',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(3, 'Soirée Andalouse Fès',
 'Musique andalouse traditionnelle.',
 'CONCERT',
 DATEADD('DAY', 12, CURRENT_TIMESTAMP), DATEADD('HOUR', 2, DATEADD('DAY', 12, CURRENT_TIMESTAMP)),
 'Salle des Fêtes', 'Fès',
 250, 220.0, 'https://images.unsplash.com/photo-1506157786151-b8491531f063', 2, 'ANNULE',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- ===== THEATRE (4..6)
(4, 'Le Malade Imaginaire',
 'Molière revisité avec mise en scène moderne.',
 'THEATRE',
 DATEADD('DAY', 10, CURRENT_TIMESTAMP), DATEADD('HOUR', 2, DATEADD('DAY', 10, CURRENT_TIMESTAMP)),
 'Théâtre National Mohamed V', 'Rabat',
 140, 150.0, 'https://images.unsplash.com/photo-1503095396549-807759245b35', 3, 'PUBLIE',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(5, 'Comédie Marocaine (édition passée)',
 'Spectacle humoristique - édition précédente.',
 'THEATRE',
 DATEADD('DAY', -20, CURRENT_TIMESTAMP), DATEADD('HOUR', 2, DATEADD('DAY', -20, CURRENT_TIMESTAMP)),
 'Complexe Culturel', 'Casablanca',
 180, 120.0, 'https://images.unsplash.com/photo-1526698905402-e13b880ad864', 2, 'TERMINE',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(6, 'La Famille Sous Pression',
 'Pièce moderne sur les défis contemporains.',
 'THEATRE',
 DATEADD('DAY', 30, CURRENT_TIMESTAMP), DATEADD('HOUR', 2, DATEADD('DAY', 30, CURRENT_TIMESTAMP)),
 'Centre Culturel', 'Marrakech',
 220, 130.0, 'https://images.unsplash.com/photo-1521334726092-b509a19597c1', 3, 'BROUILLON',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- ===== CONFERENCE (7..9)
(7, 'Tech Summit Rabat 2025',
 'IA, Cloud, Cybersecurity : experts & networking.',
 'CONFERENCE',
 DATEADD('DAY', 40, CURRENT_TIMESTAMP), DATEADD('DAY', 41, CURRENT_TIMESTAMP),
 'Palais des Congrès', 'Rabat',
 600, 500.0, 'https://images.unsplash.com/photo-1540575467063-178a50c2df87', 2, 'PUBLIE',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(8, 'Santé Digitale au Maroc',
 'Transformation digitale du secteur santé.',
 'CONFERENCE',
 DATEADD('DAY', 25, CURRENT_TIMESTAMP), DATEADD('DAY', 25, CURRENT_TIMESTAMP),
 'Hôpital Universitaire', 'Casablanca',
 300, 350.0, 'https://images.unsplash.com/photo-1521737604893-d14cc237f11d', 3, 'ANNULE',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(9, 'Énergies Renouvelables Tanger',
 'Transition énergétique et innovations vertes.',
 'CONFERENCE',
 DATEADD('DAY', 55, CURRENT_TIMESTAMP), DATEADD('HOUR', 4, DATEADD('DAY', 55, CURRENT_TIMESTAMP)),
 'Centre Écologique', 'Tanger',
 450, 300.0, 'https://images.unsplash.com/photo-1509395176047-4a66953fd231', 2, 'PUBLIE',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- ===== SPORT (10..12)
(10, 'Marathon de Rabat',
 '42 km + catégories amateurs.',
 'SPORT',
 DATEADD('DAY', 15, CURRENT_TIMESTAMP), DATEADD('HOUR', 6, DATEADD('DAY', 15, CURRENT_TIMESTAMP)),
 'Boulevard Hassan II', 'Rabat',
 1200, 50.0, 'https://images.unsplash.com/photo-1452626038306-9aae5e071dd3', 2, 'PUBLIE',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(11, 'Tournoi de Tennis Amateur',
 'Compétition ouverte à tous les niveaux.',
 'SPORT',
 DATEADD('DAY', 18, CURRENT_TIMESTAMP), DATEADD('DAY', 19, CURRENT_TIMESTAMP),
 'Club Olympique', 'Marrakech',
 120, 100.0, 'https://images.unsplash.com/photo-1554068865-24cecd4e34b8', 3, 'PUBLIE',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(12, 'Championnat d''Échecs (édition passée)',
 'Tournoi national - édition passée.',
 'SPORT',
 DATEADD('DAY', -12, CURRENT_TIMESTAMP), DATEADD('DAY', -12, CURRENT_TIMESTAMP),
 'Complexe Sportif', 'Fès',
 80, 75.0, 'https://images.unsplash.com/photo-1523875194681-bedd468c58bf', 3, 'TERMINE',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- ===== AUTRE (13..15)
(13, 'Salon du Livre Casablanca',
 'Rencontres auteurs, dédicaces, +100 exposants.',
 'AUTRE',
 DATEADD('DAY', 9, CURRENT_TIMESTAMP), DATEADD('DAY', 11, CURRENT_TIMESTAMP),
 'Bibliothèque Nationale', 'Casablanca',
 500, 60.0, 'https://images.unsplash.com/photo-1524995997946-a1c2e315a42f', 2, 'PUBLIE',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(14, 'Exposition d''Art Contemporain',
 'Peinture, sculpture, photographie.',
 'AUTRE',
 DATEADD('DAY', 5, CURRENT_TIMESTAMP), DATEADD('DAY', 6, CURRENT_TIMESTAMP),
 'Galerie des Arts', 'Tanger',
 200, 80.0, 'https://images.unsplash.com/photo-1460661419201-fd4cecdf8a8b', 3, 'BROUILLON',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(15, 'Atelier Calligraphie Arabe',
 'Initiation avec un maître calligraphe.',
 'AUTRE',
 DATEADD('DAY', 22, CURRENT_TIMESTAMP), DATEADD('DAY', 22, CURRENT_TIMESTAMP),
 'Maison de la Culture', 'Fès',
 60, 100.0, 'https://images.unsplash.com/photo-1520975916090-3105956dac38', 2, 'PUBLIE',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ------------------------------------------------
-- RÉSERVATIONS (20 minimum)
-- Recherche/filtre utiles: code, utilisateur, événement
-- Places: 1 à 10
-- Statuts: CONFIRMEE, EN_ATTENTE, ANNULEE
-- ------------------------------------------------
INSERT INTO reservations (utilisateur_id, evenement_id, nombre_places, montant_total,
                          date_reservation, statut, code_reservation, commentaire)
VALUES
-- Client1 (id=4)
(4, 1, 2, 500.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'EVT-00001', 'Deux places côte à côte'),
(4, 4, 1, 150.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'EVT-00002', NULL),
(4, 7, 1, 500.0, CURRENT_TIMESTAMP, 'EN_ATTENTE', 'EVT-00003', 'En attente de validation'),
(4, 10, 5, 250.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'EVT-00004', 'Groupe 5 personnes'),
(4, 11, 3, 300.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'EVT-00005', NULL),
(4, 13, 4, 240.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'EVT-00006', NULL),
(4, 9, 2, 600.0, CURRENT_TIMESTAMP, 'EN_ATTENTE', 'EVT-00007', NULL),
(4, 15, 1, 100.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'EVT-00008', NULL),
(4, 5, 2, 240.0, CURRENT_TIMESTAMP, 'ANNULEE', 'EVT-00009', 'Changement de programme'),
(4, 3, 1, 220.0, CURRENT_TIMESTAMP, 'ANNULEE', 'EVT-00010', 'Événement annulé'),

-- Client2 (id=5)
(5, 1, 1, 250.0, CURRENT_TIMESTAMP, 'EN_ATTENTE', 'EVT-00011', NULL),
(5, 4, 2, 300.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'EVT-00012', NULL),
(5, 7, 2, 1000.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'EVT-00013', 'Deux tickets VIP'),
(5, 10, 10, 500.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'EVT-00014', 'Groupe de 10 coureurs'),
(5, 11, 1, 100.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'EVT-00015', NULL),
(5, 13, 2, 120.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'EVT-00016', NULL),
(5, 9, 3, 900.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'EVT-00017', NULL),
(5, 15, 2, 200.0, CURRENT_TIMESTAMP, 'EN_ATTENTE', 'EVT-00018', NULL),
(5, 12, 4, 300.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'EVT-00019', 'Réservation archive'),
(5, 14, 2, 160.0, CURRENT_TIMESTAMP, 'ANNULEE', 'EVT-00020', 'Annulé (brouillon)');


-- ------------------------------------------------
-- ✅ FIX: Resynchroniser les IDENTITY (auto-increment)
-- (Obligatoire car on insère des IDs manuellement)
-- ------------------------------------------------
ALTER TABLE users ALTER COLUMN id RESTART WITH (SELECT COALESCE(MAX(id), 0) + 1 FROM users);
ALTER TABLE events ALTER COLUMN id RESTART WITH (SELECT COALESCE(MAX(id), 0) + 1 FROM events);
ALTER TABLE reservations ALTER COLUMN id RESTART WITH (SELECT COALESCE(MAX(id), 0) + 1 FROM reservations);
