package com.memoire.agricompta.service;

import com.memoire.agricompta.domain.entity.JournalFinancier;
import com.memoire.agricompta.repository.JournalFinancierRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

@Service
@RequiredArgsConstructor
public class JournalFinancierService {
    private final JournalFinancierRepository repository;
    private final AuthContext authContext;
    private final JsonMapper mapper;

    public void record(String type, Long id, String action, Object avant, Object apres, String motif) {
        JournalFinancier entry = new JournalFinancier();
        entry.setTypePiece(type);
        entry.setPieceId(id);
        entry.setAction(action);
        entry.setExploitationId(authContext.currentExploitationId());
        entry.setUtilisateurId(authContext.currentUser().id());
        entry.setAuteur(authContext.currentUser().email());
        entry.setDateAction(LocalDateTime.now());
        entry.setAvant(avant == null ? null : mapper.writeValueAsString(avant));
        entry.setApres(apres == null ? null : mapper.writeValueAsString(apres));
        entry.setMotif(motif);
        repository.save(entry);
    }

    public List<JournalFinancier> history(String type, Long id) {
        return repository.findByExploitationIdAndTypePieceAndPieceIdOrderByIdDesc(
                authContext.currentExploitationId(), type, id);
    }
}
