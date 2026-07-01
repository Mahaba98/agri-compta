package com.memoire.agricompta.web;

import com.memoire.agricompta.domain.entity.Utilisateur;
import com.memoire.agricompta.service.UtilisateurService;
import com.memoire.agricompta.web.dto.UtilisateurRequest;
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
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
public class UtilisateurController {
    private final UtilisateurService service;

    @GetMapping
    public List<Utilisateur> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Utilisateur findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Utilisateur create(@Valid @RequestBody UtilisateurRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public Utilisateur update(@PathVariable Long id, @Valid @RequestBody UtilisateurRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
