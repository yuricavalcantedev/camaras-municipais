package com.yuri.development.camaras.municipais.service;

import com.yuri.development.camaras.municipais.annotation.HLogger;
import com.yuri.development.camaras.municipais.domain.Parlamentar;
import com.yuri.development.camaras.municipais.domain.ParlamentarPresence;
import com.yuri.development.camaras.municipais.domain.Session;
import com.yuri.development.camaras.municipais.domain.TownHall;
import com.yuri.development.camaras.municipais.enums.EPresence;
import com.yuri.development.camaras.municipais.repository.ParlamentarPresenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.yuri.development.camaras.municipais.util.EventConstants.GET_PARLAMENTAR_PRESENCE_LIST_FOR_SESSION;
import static com.yuri.development.camaras.municipais.util.EventConstants.GET_PARLAMENTAR_PRESENCE_LIST_FOR_SESSION_DESCRIPTION;

@Service
public class ParlamentarPresenceService {

    @Autowired
    private ParlamentarPresenceRepository presenceRepository;

    @Autowired
    private ParlamenterService parlamenterService;

    public List<ParlamentarPresence> saveAll(List<ParlamentarPresence> parlamentarPresenceList){
        return presenceRepository.saveAll(parlamentarPresenceList);
    }

    public List<ParlamentarPresence> findAllBySession(Session session){
        return presenceRepository.findAllBySession(session);
    }

    public void updatePresenceOfParlamentar (Session session, Parlamentar parlamentar, String statusRequest){

        EPresence status = EPresence.valueOf(statusRequest);
        if(EPresence.valueOf(statusRequest) == null){
            //status does not exist, do nothing
        }else{
            Optional<ParlamentarPresence> optParlamentarPresence = findParlamentarPresenceBySessionIdAndParlamentar(session, parlamentar);
            if(optParlamentarPresence.isPresent()){
                ParlamentarPresence parlamentarPresence = optParlamentarPresence.get();
                parlamentarPresence.setStatus(status);
                presenceRepository.save(parlamentarPresence);
            }
        }
    }

    public Optional<ParlamentarPresence> findParlamentarPresenceBySessionIdAndParlamentar(Session session, Parlamentar parlamentar){
        return presenceRepository.findBySessionAndParlamentar(session, parlamentar);
    }

    public List<ParlamentarPresence> createListForSession(Session session, Long townHallId) {

        List<ParlamentarPresence> parlamentarPresenceList = new ArrayList<>();
        List<Parlamentar> parlamentarList = parlamenterService.findAllByTownHall(townHallId);

        parlamentarList.forEach(parlamentar -> parlamentarPresenceList.add(
                new ParlamentarPresence(null, parlamentar, session, townHallId, EPresence.OTHER)));

        return parlamentarPresenceList;
    }
}


