package com.memoire.agricompta.web;

import com.memoire.agricompta.domain.entity.ProduitStock;
import com.memoire.agricompta.service.ProduitStockService;
import com.memoire.agricompta.web.dto.ProduitStockRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/produits-stock")
@RequiredArgsConstructor
public class ProduitStockController {
    private final ProduitStockService service;

    @GetMapping
    public List<ProduitStock> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ProduitStock findById(@PathVariable Long id) {
        return service.findById(id);
    }


    @GetMapping("/alertes")
    public List<ProduitStock> alertes() {
        return service.findProduitsEnAlerte();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProduitStock create(@Valid @RequestBody ProduitStockRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public ProduitStock update(@PathVariable Long id, @Valid @RequestBody ProduitStockRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
