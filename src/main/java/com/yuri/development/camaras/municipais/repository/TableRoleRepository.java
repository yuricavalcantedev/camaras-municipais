package com.yuri.development.camaras.municipais.repository;

import com.yuri.development.camaras.municipais.domain.TableRole;
import com.yuri.development.camaras.municipais.domain.TownHall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TableRoleRepository extends JpaRepository<TableRole, Long> {
    List<TableRole> findByTownHall(TownHall townHall);
}
