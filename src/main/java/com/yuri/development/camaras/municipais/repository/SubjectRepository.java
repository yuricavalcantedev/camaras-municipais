package com.yuri.development.camaras.municipais.repository;

import com.yuri.development.camaras.municipais.domain.Session;
import com.yuri.development.camaras.municipais.domain.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository <Subject, Long> {

    @Query(value = "select * from subjects where session_uuid = ?1 and status = ?2", nativeQuery = true)
    List<Subject> findAllBySessionAndStatus(String uuid, String status);

    Optional<Subject> findBySessionAndId(Session session, Long id);

}
