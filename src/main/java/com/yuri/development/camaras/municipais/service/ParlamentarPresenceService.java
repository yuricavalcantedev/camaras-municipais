package com.yuri.development.camaras.municipais.service;

import com.yuri.development.camaras.municipais.domain.Parlamentar;
import com.yuri.development.camaras.municipais.domain.ParlamentarPresence;
import com.yuri.development.camaras.municipais.domain.Session;
import com.yuri.development.camaras.municipais.enums.EPresence;
import com.yuri.development.camaras.municipais.repository.ParlamentarPresenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ParlamentarPresenceService {

    @Autowired
    private ParlamentarPresenceRepository presenceRepository;

    public List<ParlamentarPresence> saveAll(List<ParlamentarPresence> parlamentarPresenceList){
        return presenceRepository.saveAll(parlamentarPresenceList);
    }

    public List<ParlamentarPresence> findAllBySession(Session session){
        return presenceRepository.findAllBySession(session);
    }

    public void updatePresenceOfParlamentar (String uuid, Session session, Parlamentar parlamentar, String status){


        if(EPresence.PRESENCE.name().equals(status)){
            Optional<ParlamentarPresence> optParlamentarPresence = findParlamentarPresenceBySessionIdAndParlamentar(session, parlamentar);
            if(optParlamentarPresence.isPresent()){
                ParlamentarPresence parlamentarPresence = optParlamentarPresence.get();
                parlamentarPresence.setStatus(EPresence.PRESENCE);
                presenceRepository.save(parlamentarPresence);
            }
        }
    }

    public Optional<ParlamentarPresence> findParlamentarPresenceBySessionIdAndParlamentar(Session session, Parlamentar parlamentar){
        return presenceRepository.findBySessionAndParlamentar(session, parlamentar);
    }
}


