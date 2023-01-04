package com.yuri.development.camaras.municipais.repository;

import com.yuri.development.camaras.municipais.domain.RoleInSession;
import com.yuri.development.camaras.municipais.domain.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleInSessionRepository extends JpaRepository<RoleInSession, Long> {
    List<RoleInSession> findAllBySession(Session session);
}
