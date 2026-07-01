package com.memoire.agricompta.web;

import com.memoire.agricompta.domain.entity.Recette;
import com.memoire.agricompta.service.RecetteService;
import com.memoire.agricompta.web.dto.RecetteRequest;
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
@RequestMapping("/api/recettes")
@RequiredArgsConstructor
public class RecetteController {
    private final RecetteService service;

    @GetMapping
    public List<Recette> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Recette findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Recette create(@Valid @RequestBody RecetteRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public Recette update(@PathVariable Long id, @Valid @RequestBody RecetteRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
