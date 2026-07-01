package com.memoire.agricompta.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "recoltes")
public class Recolte {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_recolte", nullable = false)
    private LocalDate dateRecolte;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal quantite;

    @Column(nullable = false, length = 30)
    private String unite;

    @Column(name = "prix_unitaire", precision = 14, scale = 2)
    private BigDecimal prixUnitaire;

    @Column(name = "montant_total", precision = 14, scale = 2)
    private BigDecimal montantTotal;

    @Column(columnDefinition = "text")
    private String observation;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "culture_id", nullable = false)
    private Culture culture;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @PrePersist
    void prePersist() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
    }
}
