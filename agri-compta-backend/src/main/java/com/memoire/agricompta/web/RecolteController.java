package com.memoire.agricompta.web;

import com.memoire.agricompta.domain.entity.Recolte;
import com.memoire.agricompta.service.RecolteService;
import com.memoire.agricompta.web.dto.RecolteRequest;
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
@RequestMapping("/api/recoltes")
@RequiredArgsConstructor
public class RecolteController {
    private final RecolteService service;

    @GetMapping
    public List<Recolte> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Recolte findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Recolte create(@Valid @RequestBody RecolteRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public Recolte update(@PathVariable Long id, @Valid @RequestBody RecolteRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
