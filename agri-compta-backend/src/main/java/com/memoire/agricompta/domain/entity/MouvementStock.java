package com.memoire.agricompta.domain.entity;

import com.memoire.agricompta.domain.enums.TypeMouvement;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "mouvements_stock")
public class MouvementStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_mouvement", nullable = false, length = 30)
    private TypeMouvement typeMouvement;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal quantite;

    @Column(name = "prix_unitaire", precision = 14, scale = 2)
    private BigDecimal prixUnitaire;

    @Column(name = "date_mouvement", nullable = false)
    private LocalDate dateMouvement;

    private String motif;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "produit_stock_id", nullable = false)
    private ProduitStock produitStock;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "culture_id")
    private Culture culture;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "operation_agricole_id")
    private OperationAgricole operationAgricole;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "exploitation_id")
    private Exploitation exploitation;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @PrePersist
    void prePersist() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
    }
}
