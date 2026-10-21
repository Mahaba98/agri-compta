package com.memoire.agricompta.service;

import com.memoire.agricompta.domain.entity.CategorieDepense;
import com.memoire.agricompta.exception.ResourceNotFoundException;
import com.memoire.agricompta.repository.CategorieDepenseRepository;
import com.memoire.agricompta.web.dto.CategorieDepenseRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategorieDepenseService {
    private final CategorieDepenseRepository repository;

    public List<CategorieDepense> findAll() {
        return repository.findAll();
    }

    public CategorieDepense findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categorie introuvable : " + id));
    }

    public CategorieDepense create(CategorieDepenseRequest request) {
        CategorieDepense categorie = new CategorieDepense();
        apply(categorie, request);
        return repository.save(categorie);
    }

    public CategorieDepense update(Long id, CategorieDepenseRequest request) {
        CategorieDepense categorie = findById(id);
        apply(categorie, request);
        return repository.save(categorie);
    }

    public void delete(Long id) {
        repository.delete(findById(id));
    }

    private void apply(CategorieDepense categorie, CategorieDepenseRequest request) {
        categorie.setNom(request.nom());
        categorie.setDescription(request.description());
    }
}
