package com.yuri.development.camaras.municipais.service;

import com.yuri.development.camaras.municipais.domain.Parlamentar;
import com.yuri.development.camaras.municipais.domain.Session;
import com.yuri.development.camaras.municipais.domain.SpeakerSession;
import com.yuri.development.camaras.municipais.dto.SpeakerSubscriptionDTO;
import com.yuri.development.camaras.municipais.repository.SpeakerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpeakerService {

    @Autowired
    private SpeakerRepository speakerRepository;

    public List<SpeakerSession> findAllBySession(Session session){
        return this.speakerRepository.findAllBySession(session);
    }

    public SpeakerSession create(SpeakerSession speakerSession){

        return this.speakerRepository.save(speakerSession);
    }

}
