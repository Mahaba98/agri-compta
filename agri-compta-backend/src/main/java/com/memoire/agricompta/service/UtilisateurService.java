package com.memoire.agricompta.service;

import com.memoire.agricompta.domain.entity.Utilisateur;
import com.memoire.agricompta.exception.BusinessException;
import com.memoire.agricompta.exception.ResourceNotFoundException;
import com.memoire.agricompta.repository.UtilisateurRepository;
import com.memoire.agricompta.web.dto.UtilisateurRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UtilisateurService {
    private final UtilisateurRepository repository;
    private final PasswordService passwordService;
    private final LookupService lookupService;
    private final AuthContext authContext;

    public List<Utilisateur> findAll() {
        return repository.findAll();
    }

    public Utilisateur findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + id));
    }

    public Utilisateur create(UtilisateurRequest request) {
        if (request.motDePasse() == null || request.motDePasse().isBlank()) {
            throw new BusinessException("Le mot de passe est obligatoire pour créer un utilisateur.");
        }
        Utilisateur utilisateur = new Utilisateur();
        apply(utilisateur, request);
        return repository.save(utilisateur);
    }

    public Utilisateur update(Long id, UtilisateurRequest request) {
        Utilisateur utilisateur = findById(id);
        apply(utilisateur, request);
        return repository.save(utilisateur);
    }

    public void delete(Long id) {
        repository.delete(findById(id));
    }

    private void apply(Utilisateur utilisateur, UtilisateurRequest request) {
        utilisateur.setNom(request.nom());
        utilisateur.setPrenom(request.prenom());
        utilisateur.setEmail(request.email());
        if (request.motDePasse() != null && !request.motDePasse().isBlank()) {
            utilisateur.setMotDePasse(passwordService.hash(request.motDePasse()));
        }
        utilisateur.setRole(request.role());
        utilisateur.setActif(request.actif());
        Long exploitationId = request.exploitationId() == null
                ? authContext.currentExploitationId()
                : request.exploitationId();
        utilisateur.setExploitation(lookupService.exploitation(exploitationId));
    }
}
