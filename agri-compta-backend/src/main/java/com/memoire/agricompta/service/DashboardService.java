package com.memoire.agricompta.service;

import com.memoire.agricompta.domain.entity.Culture;
import com.memoire.agricompta.repository.CultureRepository;
import com.memoire.agricompta.repository.DepenseRepository;
import com.memoire.agricompta.repository.ProduitStockRepository;
import com.memoire.agricompta.repository.RecetteRepository;
import com.memoire.agricompta.web.dto.CultureRentabiliteResponse;
import com.memoire.agricompta.web.dto.DashboardResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final DepenseRepository depenseRepository;
    private final RecetteRepository recetteRepository;
    private final CultureRepository cultureRepository;
    private final ProduitStockRepository produitStockRepository;

    public DashboardResponse getDashboard() {
        BigDecimal totalDepenses = depenseRepository.totalDepenses();
        BigDecimal totalRecettes = recetteRepository.totalRecettes();
        BigDecimal beneficeGlobal = totalRecettes.subtract(totalDepenses);

        List<CultureRentabiliteResponse> rentabilites = cultureRepository.findAll().stream()
                .map(this::rentabiliteCulture)
                .toList();

        return new DashboardResponse(
                totalDepenses,
                totalRecettes,
                beneficeGlobal,
                rentabilites,
                produitStockRepository.findProduitsEnAlerte()
        );
    }

    private CultureRentabiliteResponse rentabiliteCulture(Culture culture) {
        BigDecimal coutTotal = depenseRepository.totalByCultureId(culture.getId());
        BigDecimal recetteTotale = recetteRepository.totalByCultureId(culture.getId());
        BigDecimal benefice = recetteTotale.subtract(coutTotal);
        BigDecimal surface = culture.getSurfaceHa();
        BigDecimal coutParHectare = divide(coutTotal, surface);
        BigDecimal margeParHectare = divide(benefice, surface);

        return new CultureRentabiliteResponse(
                culture.getId(),
                culture.getNom(),
                surface,
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
