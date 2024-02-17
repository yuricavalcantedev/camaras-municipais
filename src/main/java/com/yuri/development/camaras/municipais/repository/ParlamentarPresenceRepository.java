package com.yuri.development.camaras.municipais.repository;

import com.yuri.development.camaras.municipais.domain.Parlamentar;
import com.yuri.development.camaras.municipais.domain.ParlamentarPresence;
import com.yuri.development.camaras.municipais.domain.Session;
import com.yuri.development.camaras.municipais.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParlamentarPresenceRepository extends JpaRepository<ParlamentarPresence, Long> {

    List<ParlamentarPresence> findAllBySession(Session session);

    Optional<ParlamentarPresence> findBySessionAndParlamentar(Session session, Parlamentar parlamentar);

    @Query(nativeQuery = true, value = "select * from parlamentar_presence where parlamentar_id = ?")
    List<ParlamentarPresence> findByParlamentarId(Long parlamentarId);
}
