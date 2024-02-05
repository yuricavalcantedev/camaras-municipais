package com.yuri.development.camaras.municipais.repository;

import com.yuri.development.camaras.municipais.domain.Control;
import com.yuri.development.camaras.municipais.enums.EControlType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ControlRepository extends JpaRepository <Control, Long> {
    List<Control> findByTypeAndTownHallId(EControlType type, String parliamentaryUsername);
}
