package com.memoire.agricompta.repository;

import com.memoire.agricompta.domain.entity.OperationAgricole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationAgricoleRepository extends JpaRepository<OperationAgricole, Long> {
}
