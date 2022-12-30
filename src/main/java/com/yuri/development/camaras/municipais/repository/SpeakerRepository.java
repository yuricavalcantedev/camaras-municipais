package com.yuri.development.camaras.municipais.repository;

import com.yuri.development.camaras.municipais.domain.Session;
import com.yuri.development.camaras.municipais.domain.SpeakerSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpeakerRepository extends JpaRepository<SpeakerSession, Long> {

    List<SpeakerSession> findAllBySession(Session session);
}
