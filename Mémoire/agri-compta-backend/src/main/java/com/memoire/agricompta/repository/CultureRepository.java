package com.memoire.agricompta.repository;

import com.memoire.agricompta.domain.entity.Culture;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CultureRepository extends JpaRepository<Culture, Long> {
    List<Culture> findByExploitationId(Long exploitationId);
}
