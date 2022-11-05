package com.yuri.development.camaras.municipais.repository;

import com.yuri.development.camaras.municipais.domain.Role;
import com.yuri.development.camaras.municipais.enums.ERole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName (ERole name);
}
