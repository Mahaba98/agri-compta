package com.memoire.agricompta.domain.entity;

import com.memoire.agricompta.domain.enums.StatutCulture;
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
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cultures")
public class Culture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nom;

    @Column(length = 120)
    private String variete;

    @Column(name = "surface_ha", nullable = false, precision = 12, scale = 2)
    private BigDecimal surfaceHa;

    @Column(name = "date_semis")
    private LocalDate dateSemis;

    @Column(name = "date_recolte_prevue")
    private LocalDate dateRecoltePrevue;

    @Column(name = "date_recolte_reelle")
    private LocalDate dateRecolteReelle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatutCulture statut = StatutCulture.PLANIFIEE;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "campagne_id", nullable = false)
    private CampagneAgricole campagne;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parcelle_id", nullable = false)
    private Parcelle parcelle;
}
