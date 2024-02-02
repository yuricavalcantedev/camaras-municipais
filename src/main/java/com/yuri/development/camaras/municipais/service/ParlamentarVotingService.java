package com.yuri.development.camaras.municipais.service;

import com.yuri.development.camaras.municipais.domain.ParlamentarVoting;
import com.yuri.development.camaras.municipais.repository.ParlamentarVotingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ParlamentarVotingService {

    @Autowired
    private ParlamentarVotingRepository repository;

    public ParlamentarVoting save(ParlamentarVoting parlamentarVoting){
        return repository.save(parlamentarVoting);
    }

    public ParlamentarVoting findByIdAndParlamentarId(Long id, Long parlamentarId) {

        return repository.findByIdAndParlamentarId(id, parlamentarId).orElse(null);
    }
}
