package com.memoire.agricompta.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "parcelles")
public class Parcelle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nom;

    @Column(name = "superficie_ha", nullable = false, precision = 12, scale = 2)
    private BigDecimal superficieHa;

    private String localisation;

    @Column(name = "type_sol", length = 120)
    private String typeSol;

    @Column(name = "type_irrigation", length = 120)
    private String typeIrrigation;

    @Column(columnDefinition = "text")
    private String description;
}
