package com.yuri.development.camaras.municipais.repository;

import com.yuri.development.camaras.municipais.domain.Session;
import com.yuri.development.camaras.municipais.domain.TownHall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.Optional;

public interface SessionRepository extends JpaRepository <Session, String> {

    Optional<Session> findByTownHallAndDate(TownHall townhall, Date today);

    Optional<Session> findByUuid(String uuid);

    @Query(value = "select speaker_order from sessions where uuid = ?", nativeQuery = true)
    Integer findSpeakerOrderBySession(String uuid);

    @Query(value = "update sessions set speaker_order = ?1 where uuid = ?2", nativeQuery = true)
    void updateSpeakerOrder(Integer speakerOrder, String uuid);
}
