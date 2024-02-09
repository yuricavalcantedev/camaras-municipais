package com.yuri.development.camaras.municipais.repository;

import com.yuri.development.camaras.municipais.domain.ParlamentarVoting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParlamentarVotingRepository extends JpaRepository<ParlamentarVoting, Long> {

    Optional<ParlamentarVoting> findByIdAndParlamentarId(Long id, Long parlamentarId);

    List<ParlamentarVoting> findByParlamentarId(Long parlamentarId);
}
