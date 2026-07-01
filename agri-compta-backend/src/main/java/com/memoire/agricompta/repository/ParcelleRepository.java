package com.memoire.agricompta.repository;

import com.memoire.agricompta.domain.entity.Parcelle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParcelleRepository extends JpaRepository<Parcelle, Long> {
}
