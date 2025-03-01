package com.yuri.development.camaras.municipais.repository;

import com.yuri.development.camaras.municipais.domain.Session;
import com.yuri.development.camaras.municipais.domain.TownHall;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.Optional;

public interface SessionRepository extends JpaRepository <Session, String> {

    Optional<Session> findByTownHallAndDate(TownHall townhall, Date today);

    Optional<Session> findByUuid(String uuid);
}
