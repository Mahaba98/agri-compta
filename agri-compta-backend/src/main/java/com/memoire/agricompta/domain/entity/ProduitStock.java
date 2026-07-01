package com.memoire.agricompta.domain.entity;

import com.memoire.agricompta.domain.enums.TypeProduit;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "produits_stock")
public class ProduitStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_produit", nullable = false, length = 60)
    private TypeProduit typeProduit;

    @Column(nullable = false, length = 30)
    private String unite;

    @Column(name = "quantite_disponible", nullable = false, precision = 14, scale = 2)
    private BigDecimal quantiteDisponible = BigDecimal.ZERO;

    @Column(name = "seuil_alerte", nullable = false, precision = 14, scale = 2)
    private BigDecimal seuilAlerte = BigDecimal.ZERO;

    @Column(name = "prix_unitaire_moyen", precision = 14, scale = 2)
    private BigDecimal prixUnitaireMoyen;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @PrePersist
    void prePersist() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
    }
}
