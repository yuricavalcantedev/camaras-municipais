package com.yuri.development.camaras.municipais.repository;

import com.yuri.development.camaras.municipais.domain.LegislativeSubjectType;
import com.yuri.development.camaras.municipais.domain.TownHall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LegislativeSubjectTypeRepository extends JpaRepository<LegislativeSubjectType, Long> {

    List<LegislativeSubjectType> findByTownHall(TownHall townHall);

    LegislativeSubjectType findByTownHallAndTitle(TownHall townHall, String title);
}
