package com.memoire.agricompta.repository;

import com.memoire.agricompta.domain.entity.Culture;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CultureRepository extends JpaRepository<Culture, Long> {
}
