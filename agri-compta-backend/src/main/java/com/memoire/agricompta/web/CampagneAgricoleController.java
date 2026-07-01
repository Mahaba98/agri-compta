package com.memoire.agricompta.web;

import com.memoire.agricompta.domain.entity.CampagneAgricole;
import com.memoire.agricompta.service.CampagneAgricoleService;
import com.memoire.agricompta.web.dto.CampagneAgricoleRequest;
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
@RequestMapping("/api/campagnes")
@RequiredArgsConstructor
public class CampagneAgricoleController {
    private final CampagneAgricoleService service;

    @GetMapping
    public List<CampagneAgricole> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public CampagneAgricole findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CampagneAgricole create(@Valid @RequestBody CampagneAgricoleRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public CampagneAgricole update(@PathVariable Long id, @Valid @RequestBody CampagneAgricoleRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
