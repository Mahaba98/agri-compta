package com.memoire.agricompta.service;

import com.memoire.agricompta.domain.entity.Culture;
import com.memoire.agricompta.repository.CultureRepository;
import com.memoire.agricompta.repository.DepenseRepository;
import com.memoire.agricompta.repository.OperationAgricoleRepository;
import com.memoire.agricompta.repository.ProduitStockRepository;
import com.memoire.agricompta.repository.RecetteRepository;
import com.memoire.agricompta.web.dto.CultureRentabiliteResponse;
import com.memoire.agricompta.web.dto.DashboardResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final DepenseRepository depenseRepository;
    private final RecetteRepository recetteRepository;
    private final CultureRepository cultureRepository;
    private final OperationAgricoleRepository operationRepository;
    private final ProduitStockRepository produitStockRepository;
    private final AuthContext authContext;

    public DashboardResponse getDashboard() {
        Long exploitationId = authContext.currentExploitationId();
        BigDecimal totalDepenses = depenseRepository.totalDepensesByExploitationId(exploitationId);
        BigDecimal totalOperations = operationRepository.totalOperationsByExploitationId(exploitationId);
        BigDecimal totalRecettes = recetteRepository.totalRecettesByExploitationId(exploitationId);
        BigDecimal beneficeGlobal = totalRecettes.subtract(totalDepenses).subtract(totalOperations);

        var rentabilites = cultureRepository.findByExploitationId(exploitationId).stream()
                .map(this::rentabiliteCulture)
                .toList();

        return new DashboardResponse(
                totalDepenses,
                totalOperations,
                totalRecettes,
                beneficeGlobal,
                rentabilites,
                produitStockRepository.findProduitsEnAlerteByExploitationId(exploitationId)
        );
    }

    private CultureRentabiliteResponse rentabiliteCulture(Culture culture) {
        Long exploitationId = authContext.currentExploitationId();
        BigDecimal totalDepenses = depenseRepository.totalByExploitationIdAndCultureId(exploitationId, culture.getId());
        BigDecimal totalOperations = operationRepository.totalByExploitationIdAndCultureId(exploitationId, culture.getId());
        BigDecimal coutTotal = totalDepenses.add(totalOperations);
        BigDecimal recetteTotale = recetteRepository.totalByExploitationIdAndCultureId(exploitationId, culture.getId());
        BigDecimal benefice = recetteTotale.subtract(coutTotal);
        BigDecimal surface = culture.getSurfaceHa();
        BigDecimal coutParHectare = divide(coutTotal, surface);
        BigDecimal margeParHectare = divide(benefice, surface);

        return new CultureRentabiliteResponse(
                culture.getId(),
                culture.getNom(),
                surface,
                totalDepenses,
                totalOperations,
                coutTotal,
                recetteTotale,
                benefice,
                coutParHectare,
                margeParHectare
        );
    }

    private BigDecimal divide(BigDecimal amount, BigDecimal divisor) {
        if (divisor == null || divisor.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return amount.divide(divisor, 2, RoundingMode.HALF_UP);
    }
}
