package com.memoire.agricompta.service;

import com.memoire.agricompta.domain.entity.OperationAgricole;
import com.memoire.agricompta.exception.ResourceNotFoundException;
import com.memoire.agricompta.repository.OperationAgricoleRepository;
import com.memoire.agricompta.web.dto.OperationAgricoleRequest;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OperationAgricoleService {
    private final OperationAgricoleRepository repository;
    private final LookupService lookupService;

    public List<OperationAgricole> findAll() {
        return repository.findAll();
    }

    public OperationAgricole findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operation agricole introuvable : " + id));
    }

    public OperationAgricole create(OperationAgricoleRequest request) {
        OperationAgricole operation = new OperationAgricole();
        apply(operation, request);
        return repository.save(operation);
    }

    public OperationAgricole update(Long id, OperationAgricoleRequest request) {
        OperationAgricole operation = findById(id);
        apply(operation, request);
        return repository.save(operation);
    }

    public void delete(Long id) {
        repository.delete(findById(id));
    }

    private void apply(OperationAgricole operation, OperationAgricoleRequest request) {
        operation.setTypeOperation(request.typeOperation());
        operation.setDateOperation(request.dateOperation());
        operation.setDescription(request.description());
        operation.setCoutMainOeuvre(request.coutMainOeuvre() == null ? BigDecimal.ZERO : request.coutMainOeuvre());
        operation.setCulture(lookupService.culture(request.cultureId()));
        operation.setUtilisateur(request.utilisateurId() == null ? null : lookupService.utilisateur(request.utilisateurId()));
    }
}
