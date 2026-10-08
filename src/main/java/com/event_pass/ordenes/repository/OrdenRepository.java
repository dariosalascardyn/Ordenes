package com.event_pass.ordenes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.event_pass.ordenes.model.Orden;

@Repository
public interface OrdenRepository extends JpaRepository<Orden, Long> {
}
