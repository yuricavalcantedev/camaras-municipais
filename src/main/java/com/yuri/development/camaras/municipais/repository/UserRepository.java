package com.yuri.development.camaras.municipais.repository;

import com.yuri.development.camaras.municipais.domain.UserTH;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserTH, Long> {
}
