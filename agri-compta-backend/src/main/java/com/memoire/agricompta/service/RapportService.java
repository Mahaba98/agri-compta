package com.memoire.agricompta.service;

import com.memoire.agricompta.domain.entity.Culture;
import com.memoire.agricompta.domain.entity.Depense;
import com.memoire.agricompta.domain.entity.OperationAgricole;
import com.memoire.agricompta.domain.entity.ProduitStock;
import com.memoire.agricompta.domain.entity.Recette;
import com.memoire.agricompta.domain.entity.Recolte;
import com.memoire.agricompta.exception.BusinessException;
import com.memoire.agricompta.repository.CultureRepository;
import com.memoire.agricompta.repository.DepenseRepository;
import com.memoire.agricompta.repository.OperationAgricoleRepository;
import com.memoire.agricompta.repository.ProduitStockRepository;
import com.memoire.agricompta.repository.RecetteRepository;
import com.memoire.agricompta.repository.RecolteRepository;
import com.memoire.agricompta.web.dto.RapportCultureResponse;
import com.memoire.agricompta.web.dto.RapportSyntheseResponse;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RapportService {
    private final CultureRepository cultureRepository;
    private final DepenseRepository depenseRepository;
    private final RecetteRepository recetteRepository;
    private final RecolteRepository recolteRepository;
    private final OperationAgricoleRepository operationRepository;
    private final ProduitStockRepository produitStockRepository;

    public RapportSyntheseResponse synthese(
            LocalDate dateDebut,
            LocalDate dateFin,
            Long campagneId,
            Long cultureId) {
        validateDates(dateDebut, dateFin);

        List<Culture> cultures = cultureRepository.findAll().stream()
                .filter(culture -> campagneId == null || culture.getCampagne().getId().equals(campagneId))
                .filter(culture -> cultureId == null || culture.getId().equals(cultureId))
                .toList();
        Set<Long> cultureIds = cultures.stream().map(Culture::getId).collect(Collectors.toSet());

        List<Depense> depenses = depenseRepository.findAll().stream()
                .filter(depense -> cultureIds.contains(depense.getCulture().getId()))
                .filter(depense -> inRange(depense.getDateDepense(), dateDebut, dateFin))
                .toList();
        List<Recette> recettes = recetteRepository.findAll().stream()
                .filter(recette -> cultureIds.contains(recette.getCulture().getId()))
                .filter(recette -> inRange(recette.getDateRecette(), dateDebut, dateFin))
                .toList();
        List<Recolte> recoltes = recolteRepository.findAll().stream()
                .filter(recolte -> cultureIds.contains(recolte.getCulture().getId()))
                .filter(recolte -> inRange(recolte.getDateRecolte(), dateDebut, dateFin))
                .toList();
        List<OperationAgricole> operations = operationRepository.findAll().stream()
                .filter(operation -> cultureIds.contains(operation.getCulture().getId()))
                .filter(operation -> inRange(operation.getDateOperation(), dateDebut, dateFin))
                .toList();

        BigDecimal totalDepenses = sumDepenses(depenses);
        BigDecimal totalOperations = sumOperations(operations);
        BigDecimal totalRecettes = sumRecettes(recettes);
        BigDecimal totalQuantite = recoltes.stream()
                .map(Recolte::getQuantite)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<RapportCultureResponse> lignes = cultures.stream()
                .map(culture -> ligneCulture(culture, depenses, recettes, recoltes, operations))
                .toList();

        return new RapportSyntheseResponse(
                dateDebut,
                dateFin,
                campagneId,
                cultureId,
                totalDepenses,
                totalOperations,
                totalRecettes,
                totalRecettes.subtract(totalDepenses).subtract(totalOperations),
                totalQuantite,
                operations.size(),
                recoltes.size(),
                valeurStock(),
                produitStockRepository.findProduitsEnAlerte().size(),
                lignes);
    }

    public byte[] exportCsv(
            LocalDate dateDebut,
            LocalDate dateFin,
            Long campagneId,
            Long cultureId) {
        RapportSyntheseResponse rapport = synthese(dateDebut, dateFin, campagneId, cultureId);
        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append("Culture;Campagne;Parcelle;Surface ha;Dépenses;Montant opérations;Recettes;Bénéfice;Quantité récoltée;Unité;Nombre opérations;Récoltes\r\n");
        for (RapportCultureResponse ligne : rapport.cultures()) {
            csv.append(escape(ligne.culture())).append(';')
                    .append(escape(ligne.campagne())).append(';')
                    .append(escape(ligne.parcelle())).append(';')
                    .append(ligne.surfaceHa()).append(';')
                    .append(ligne.totalDepenses()).append(';')
                    .append(ligne.totalOperations()).append(';')
                    .append(ligne.totalRecettes()).append(';')
                    .append(ligne.benefice()).append(';')
                    .append(ligne.quantiteRecoltee()).append(';')
                    .append(escape(ligne.unite())).append(';')
                    .append(ligne.nombreOperations()).append(';')
                    .append(ligne.nombreRecoltes()).append("\r\n");
        }
        csv.append("\r\nTOTAL;;;;")
                .append(rapport.totalDepenses()).append(';')
                .append(rapport.totalOperations()).append(';')
                .append(rapport.totalRecettes()).append(';')
                .append(rapport.benefice()).append(';')
                .append(rapport.totalQuantiteRecoltee()).append(";;")
                .append(rapport.nombreOperations()).append(';')
                .append(rapport.nombreRecoltes()).append("\r\n");
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private RapportCultureResponse ligneCulture(
            Culture culture,
            List<Depense> depenses,
            List<Recette> recettes,
            List<Recolte> recoltes,
            List<OperationAgricole> operations) {
        List<Depense> depensesCulture = depenses.stream()
                .filter(item -> item.getCulture().getId().equals(culture.getId()))
                .toList();
        List<Recette> recettesCulture = recettes.stream()
                .filter(item -> item.getCulture().getId().equals(culture.getId()))
                .toList();
        List<Recolte> recoltesCulture = recoltes.stream()
                .filter(item -> item.getCulture().getId().equals(culture.getId()))
                .toList();
        List<OperationAgricole> operationsCulture = operations.stream()
                .filter(item -> item.getCulture().getId().equals(culture.getId()))
                .toList();

        BigDecimal totalDepenses = sumDepenses(depensesCulture);
        BigDecimal totalOperations = sumOperations(operationsCulture);
        BigDecimal totalRecettes = sumRecettes(recettesCulture);
        BigDecimal quantite = recoltesCulture.stream()
                .map(Recolte::getQuantite)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        String unite = recoltesCulture.stream()
                .map(Recolte::getUnite)
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .reduce((first, second) -> first.equals(second) ? first : "Unités multiples")
                .orElse("-");

        return new RapportCultureResponse(
                culture.getId(),
                culture.getNom(),
                culture.getCampagne().getNom(),
                culture.getParcelle().getNom(),
                culture.getSurfaceHa(),
                totalDepenses,
                totalOperations,
                totalRecettes,
                totalRecettes.subtract(totalDepenses).subtract(totalOperations),
                quantite,
                unite,
                operationsCulture.size(),
                recoltesCulture.size());
    }

    private BigDecimal sumDepenses(List<Depense> depenses) {
        return depenses.stream().map(Depense::getMontant).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumRecettes(List<Recette> recettes) {
        return recettes.stream().map(Recette::getMontant).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumOperations(List<OperationAgricole> operations) {
        return operations.stream()
                .map(OperationAgricole::getCoutMainOeuvre)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal valeurStock() {
        return produitStockRepository.findAll().stream()
                .map(this::valeurProduit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal valeurProduit(ProduitStock produit) {
        if (produit.getPrixUnitaireMoyen() == null) {
            return BigDecimal.ZERO;
        }
        return produit.getQuantiteDisponible().multiply(produit.getPrixUnitaireMoyen());
    }

    private boolean inRange(LocalDate date, LocalDate dateDebut, LocalDate dateFin) {
        return (dateDebut == null || !date.isBefore(dateDebut))
                && (dateFin == null || !date.isAfter(dateFin));
    }

    private void validateDates(LocalDate dateDebut, LocalDate dateFin) {
        if (dateDebut != null && dateFin != null && dateDebut.isAfter(dateFin)) {
            throw new BusinessException("La date de début doit précéder la date de fin.");
        }
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return '"' + value.replace("\"", "\"\"") + '"';
    }
}
