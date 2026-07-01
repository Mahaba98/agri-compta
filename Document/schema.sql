CREATE TABLE utilisateurs (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    mot_de_passe VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL CHECK (role IN ('ADMIN', 'AGRICULTEUR', 'COMPTABLE')),
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE campagnes_agricoles (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(120) NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE,
    statut VARCHAR(30) NOT NULL DEFAULT 'PLANIFIEE'
        CHECK (statut IN ('PLANIFIEE', 'EN_COURS', 'TERMINEE')),
    CONSTRAINT chk_campagne_dates CHECK (date_fin IS NULL OR date_fin >= date_debut)
);

CREATE TABLE parcelles (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(120) NOT NULL,
    superficie_ha NUMERIC(12, 2) NOT NULL CHECK (superficie_ha > 0),
    localisation VARCHAR(255),
    type_sol VARCHAR(120),
    type_irrigation VARCHAR(120),
    description TEXT
);

CREATE TABLE cultures (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(120) NOT NULL,
    variete VARCHAR(120),
    surface_ha NUMERIC(12, 2) NOT NULL CHECK (surface_ha > 0),
    date_semis DATE,
    date_recolte_prevue DATE,
    date_recolte_reelle DATE,
    statut VARCHAR(30) NOT NULL DEFAULT 'PLANIFIEE'
        CHECK (statut IN ('PLANIFIEE', 'EN_COURS', 'RECOLTEE', 'ANNULEE')),
    campagne_id BIGINT NOT NULL REFERENCES campagnes_agricoles(id),
    parcelle_id BIGINT NOT NULL REFERENCES parcelles(id),
    CONSTRAINT chk_culture_dates CHECK (
        date_recolte_prevue IS NULL
        OR date_semis IS NULL
        OR date_recolte_prevue >= date_semis
    )
);

CREATE TABLE categories_depenses (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(120) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE operations_agricoles (
    id BIGSERIAL PRIMARY KEY,
    type_operation VARCHAR(60) NOT NULL CHECK (
        type_operation IN (
            'SEMIS',
            'FERTILISATION',
            'IRRIGATION',
            'TRAITEMENT',
            'RECOLTE',
            'LABOUR',
            'DESHERBAGE',
            'AUTRE'
        )
    ),
    date_operation DATE NOT NULL,
    description TEXT,
    cout_main_oeuvre NUMERIC(14, 2) NOT NULL DEFAULT 0 CHECK (cout_main_oeuvre >= 0),
    culture_id BIGINT NOT NULL REFERENCES cultures(id),
    utilisateur_id BIGINT REFERENCES utilisateurs(id),
    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE depenses (
    id BIGSERIAL PRIMARY KEY,
    libelle VARCHAR(180) NOT NULL,
    montant NUMERIC(14, 2) NOT NULL CHECK (montant > 0),
    date_depense DATE NOT NULL,
    mode_paiement VARCHAR(50),
    reference_piece VARCHAR(120),
    culture_id BIGINT NOT NULL REFERENCES cultures(id),
    categorie_id BIGINT NOT NULL REFERENCES categories_depenses(id),
    operation_agricole_id BIGINT REFERENCES operations_agricoles(id),
    utilisateur_id BIGINT REFERENCES utilisateurs(id),
    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE recoltes (
    id BIGSERIAL PRIMARY KEY,
    date_recolte DATE NOT NULL,
    quantite NUMERIC(14, 2) NOT NULL CHECK (quantite > 0),
    unite VARCHAR(30) NOT NULL,
    prix_unitaire NUMERIC(14, 2) CHECK (prix_unitaire IS NULL OR prix_unitaire >= 0),
    montant_total NUMERIC(14, 2) CHECK (montant_total IS NULL OR montant_total >= 0),
    observation TEXT,
    culture_id BIGINT NOT NULL REFERENCES cultures(id),
    utilisateur_id BIGINT REFERENCES utilisateurs(id),
    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE recettes (
    id BIGSERIAL PRIMARY KEY,
    libelle VARCHAR(180) NOT NULL,
    montant NUMERIC(14, 2) NOT NULL CHECK (montant > 0),
    date_recette DATE NOT NULL,
    quantite NUMERIC(14, 2) CHECK (quantite IS NULL OR quantite > 0),
    unite VARCHAR(30),
    prix_unitaire NUMERIC(14, 2) CHECK (prix_unitaire IS NULL OR prix_unitaire > 0),
    culture_id BIGINT NOT NULL REFERENCES cultures(id),
    utilisateur_id BIGINT REFERENCES utilisateurs(id),
    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE produits_stock (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(150) NOT NULL,
    type_produit VARCHAR(60) NOT NULL CHECK (
        type_produit IN (
            'SEMENCE',
            'ENGRAIS',
            'PRODUIT_PHYTOSANITAIRE',
            'CARBURANT',
            'ALIMENT',
            'MATERIEL_CONSOMMABLE',
            'AUTRE'
        )
    ),
    unite VARCHAR(30) NOT NULL,
    quantite_disponible NUMERIC(14, 2) NOT NULL DEFAULT 0 CHECK (quantite_disponible >= 0),
    seuil_alerte NUMERIC(14, 2) NOT NULL DEFAULT 0 CHECK (seuil_alerte >= 0),
    prix_unitaire_moyen NUMERIC(14, 2) CHECK (prix_unitaire_moyen IS NULL OR prix_unitaire_moyen >= 0),
    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE mouvements_stock (
    id BIGSERIAL PRIMARY KEY,
    type_mouvement VARCHAR(30) NOT NULL CHECK (type_mouvement IN ('ENTREE', 'SORTIE', 'AJUSTEMENT')),
    quantite NUMERIC(14, 2) NOT NULL CHECK (quantite > 0),
    prix_unitaire NUMERIC(14, 2) CHECK (prix_unitaire IS NULL OR prix_unitaire >= 0),
    date_mouvement DATE NOT NULL,
    motif VARCHAR(255),
    produit_stock_id BIGINT NOT NULL REFERENCES produits_stock(id),
    culture_id BIGINT REFERENCES cultures(id),
    operation_agricole_id BIGINT REFERENCES operations_agricoles(id),
    utilisateur_id BIGINT REFERENCES utilisateurs(id),
    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_cultures_campagne ON cultures(campagne_id);
CREATE INDEX idx_cultures_parcelle ON cultures(parcelle_id);
CREATE INDEX idx_depenses_culture ON depenses(culture_id);
CREATE INDEX idx_depenses_categorie ON depenses(categorie_id);
CREATE INDEX idx_recettes_culture ON recettes(culture_id);
CREATE INDEX idx_operations_culture ON operations_agricoles(culture_id);
CREATE INDEX idx_recoltes_culture ON recoltes(culture_id);
CREATE INDEX idx_recoltes_date ON recoltes(date_recolte);
CREATE INDEX idx_mouvements_stock_produit ON mouvements_stock(produit_stock_id);
CREATE INDEX idx_mouvements_stock_culture ON mouvements_stock(culture_id);

INSERT INTO categories_depenses (nom, description) VALUES
('Semences', 'Achat de semences et plants'),
('Engrais', 'Achat d''engrais organiques ou chimiques'),
('Produits phytosanitaires', 'Produits de traitement des cultures'),
('Main-d''oeuvre', 'Paiement des travailleurs agricoles'),
('Carburant', 'Carburant utilise pour les machines agricoles'),
('Irrigation', 'Charges liees a l''eau et a l''irrigation'),
('Location materiel', 'Location de tracteur, outils ou machines'),
('Autres charges', 'Autres depenses agricoles');
