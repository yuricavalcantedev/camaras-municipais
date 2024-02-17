package com.yuri.development.camaras.municipais.repository;

import com.yuri.development.camaras.municipais.domain.TableRole;
import com.yuri.development.camaras.municipais.domain.TownHall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TableRoleRepository extends JpaRepository<TableRole, Long> {
    List<TableRole> findByTownHallOrderByPositionAsc(TownHall townHall);

    @Query(nativeQuery = true, value = "select * from table_role where name = 'PRES' and town_hall_id = ?")
    Optional<TableRole> findPresidentRoleByTownHall(Long townHallId);
}
