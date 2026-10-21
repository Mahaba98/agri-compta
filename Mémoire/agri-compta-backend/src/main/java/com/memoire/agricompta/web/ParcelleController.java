package com.memoire.agricompta.web;

import com.memoire.agricompta.domain.entity.Parcelle;
import com.memoire.agricompta.service.ParcelleService;
import com.memoire.agricompta.web.dto.ParcelleRequest;
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
@RequestMapping("/api/parcelles")
@RequiredArgsConstructor
public class ParcelleController {
    private final ParcelleService service;

    @GetMapping
    public List<Parcelle> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Parcelle findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Parcelle create(@Valid @RequestBody ParcelleRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public Parcelle update(@PathVariable Long id, @Valid @RequestBody ParcelleRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
