package com.memoire.agricompta.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "journal_financier")
public class JournalFinancier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String typePiece;

    @Column(nullable = false)
    private Long pieceId;

    @Column(nullable = false, length = 20)
    private String action;

    @Column(nullable = false)
    private Long exploitationId;

    @Column(nullable = false)
    private Long utilisateurId;

    @Column(nullable = false)
    private String auteur;

    @Column(nullable = false)
    private LocalDateTime dateAction;

    @Column(columnDefinition = "text")
    private String avant;

    @Column(columnDefinition = "text")
    private String apres;

    @Column(length = 500)
    private String motif;
}
