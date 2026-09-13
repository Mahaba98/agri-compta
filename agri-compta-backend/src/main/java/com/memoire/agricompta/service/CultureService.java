package com.memoire.agricompta.service;

import com.memoire.agricompta.domain.entity.Culture;
import com.memoire.agricompta.domain.enums.StatutCulture;
import com.memoire.agricompta.exception.BusinessException;
import com.memoire.agricompta.exception.ResourceNotFoundException;
import com.memoire.agricompta.repository.CultureRepository;
import com.memoire.agricompta.web.dto.CultureRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CultureService {
    private final CultureRepository repository;
    private final LookupService lookupService;
    private final AuthContext authContext;

    public List<Culture> findAll() {
        return repository.findByExploitationId(authContext.currentExploitationId());
    }

    public Culture findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Culture introuvable : " + id));
    }

    public Culture create(CultureRequest request) {
        Culture culture = new Culture();
        apply(culture, request);
        culture.setExploitation(lookupService.exploitation(authContext.currentExploitationId()));
        return repository.save(culture);
    }

    public Culture update(Long id, CultureRequest request) {
        Culture culture = findById(id);
        apply(culture, request);
        return repository.save(culture);
    }

    public void delete(Long id) {
        repository.delete(findById(id));
    }

    private void apply(Culture culture, CultureRequest request) {
        if (request.dateSemis() != null && request.dateRecoltePrevue() != null
                && request.dateRecoltePrevue().isBefore(request.dateSemis())) {
            throw new BusinessException("La date de recolte prevue doit etre apres la date de semis.");
        }
        culture.setNom(request.nom());
        culture.setVariete(request.variete());
        culture.setSurfaceHa(request.surfaceHa());
        culture.setDateSemis(request.dateSemis());
        culture.setDateRecoltePrevue(request.dateRecoltePrevue());
        culture.setDateRecolteReelle(request.dateRecolteReelle());
        culture.setStatut(request.statut() == null ? StatutCulture.PLANIFIEE : request.statut());
        culture.setCampagne(lookupService.campagne(request.campagneId()));
        culture.setParcelle(lookupService.parcelle(request.parcelleId()));
    }
}
