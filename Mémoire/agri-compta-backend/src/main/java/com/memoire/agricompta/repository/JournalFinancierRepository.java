package com.memoire.agricompta.repository;

import com.memoire.agricompta.domain.entity.JournalFinancier;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JournalFinancierRepository extends JpaRepository<JournalFinancier, Long> {
    List<JournalFinancier> findByExploitationIdAndTypePieceAndPieceIdOrderByIdDesc(
            Long exploitationId, String typePiece, Long pieceId);
}
