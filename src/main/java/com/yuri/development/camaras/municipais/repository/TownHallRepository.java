package com.yuri.development.camaras.municipais.repository;

import com.yuri.development.camaras.municipais.domain.TownHall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TownHallRepository extends JpaRepository <TownHall, Long> {

    Optional<TownHall> findByName(String name);

    Optional<TownHall> findByApiURL(String apiURL);
}
