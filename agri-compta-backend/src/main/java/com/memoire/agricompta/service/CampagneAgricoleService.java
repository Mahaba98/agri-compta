package com.memoire.agricompta.service;

import com.memoire.agricompta.domain.entity.CampagneAgricole;
import com.memoire.agricompta.domain.enums.StatutCampagne;
import com.memoire.agricompta.exception.BusinessException;
import com.memoire.agricompta.exception.ResourceNotFoundException;
import com.memoire.agricompta.repository.CampagneAgricoleRepository;
import com.memoire.agricompta.web.dto.CampagneAgricoleRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CampagneAgricoleService {
    private final CampagneAgricoleRepository repository;
    private final AuthContext authContext;
    private final LookupService lookupService;

    public List<CampagneAgricole> findAll() {
        return repository.findByExploitationId(authContext.currentExploitationId());
    }

    public CampagneAgricole findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campagne introuvable : " + id));
    }

    public CampagneAgricole create(CampagneAgricoleRequest request) {
        CampagneAgricole campagne = new CampagneAgricole();
        apply(campagne, request);
        campagne.setExploitation(lookupService.exploitation(authContext.currentExploitationId()));
        return repository.save(campagne);
    }

    public CampagneAgricole update(Long id, CampagneAgricoleRequest request) {
        CampagneAgricole campagne = findById(id);
        apply(campagne, request);
        return repository.save(campagne);
    }

    public void delete(Long id) {
        repository.delete(findById(id));
    }

    private void apply(CampagneAgricole campagne, CampagneAgricoleRequest request) {
        if (request.dateFin() != null && request.dateFin().isBefore(request.dateDebut())) {
            throw new BusinessException("La date de fin doit etre superieure ou egale a la date de debut.");
        }
        campagne.setNom(request.nom());
        campagne.setDateDebut(request.dateDebut());
        campagne.setDateFin(request.dateFin());
        campagne.setStatut(request.statut() == null ? StatutCampagne.PLANIFIEE : request.statut());
    }
}
