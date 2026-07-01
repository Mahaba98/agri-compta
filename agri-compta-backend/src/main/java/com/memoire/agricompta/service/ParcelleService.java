package com.memoire.agricompta.service;

import com.memoire.agricompta.domain.entity.Parcelle;
import com.memoire.agricompta.exception.ResourceNotFoundException;
import com.memoire.agricompta.repository.ParcelleRepository;
import com.memoire.agricompta.web.dto.ParcelleRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ParcelleService {
    private final ParcelleRepository repository;

    public List<Parcelle> findAll() {
        return repository.findAll();
    }

    public Parcelle findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parcelle introuvable : " + id));
    }

    public Parcelle create(ParcelleRequest request) {
        Parcelle parcelle = new Parcelle();
        apply(parcelle, request);
        return repository.save(parcelle);
    }

    public Parcelle update(Long id, ParcelleRequest request) {
        Parcelle parcelle = findById(id);
        apply(parcelle, request);
        return repository.save(parcelle);
    }

    public void delete(Long id) {
        repository.delete(findById(id));
    }

    private void apply(Parcelle parcelle, ParcelleRequest request) {
        parcelle.setNom(request.nom());
        parcelle.setSuperficieHa(request.superficieHa());
        parcelle.setLocalisation(request.localisation());
        parcelle.setTypeSol(request.typeSol());
        parcelle.setTypeIrrigation(request.typeIrrigation());
        parcelle.setDescription(request.description());
    }
}
