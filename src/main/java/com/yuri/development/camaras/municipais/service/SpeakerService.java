package com.yuri.development.camaras.municipais.service;

import com.yuri.development.camaras.municipais.domain.Session;
import com.yuri.development.camaras.municipais.domain.SpeakerSession;
import com.yuri.development.camaras.municipais.enums.ESpeakerType;
import com.yuri.development.camaras.municipais.repository.SpeakerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SpeakerService {

    @Autowired
    private SpeakerRepository speakerRepository;

    public List<SpeakerSession> findAllBySession(Session session){
        return speakerRepository.findAllBySession(session);
    }

    public SpeakerSession create(SpeakerSession speakerSession){

        return speakerRepository.save(speakerSession);
    }

    public int retrieveNextOrder(Session session, ESpeakerType type) {

        List<SpeakerSession> speakerList = session.getSpeakerList().stream().filter(s -> s.getType() == type)
                .collect(Collectors.toList());

        int nextOrder = speakerList.stream()
                .mapToInt(SpeakerSession::getSpeakerOrder)
                .max()
                .orElse(0);

        return nextOrder + 1;
    }

    public void removeSpeakerFromList(Session session, Long speakerId, ESpeakerType type) {

        List<SpeakerSession> speakerList = session.getSpeakerList().stream()
                .filter(s -> s.getType() == type)
                .collect(Collectors.toList());

        SpeakerSession speakerToRemove = speakerList.stream()
                .filter(s -> s.getParlamentarId().equals(speakerId))
                .findFirst()
                .orElse(null);

        session.getSpeakerList().remove(speakerToRemove);

        if (speakerToRemove != null) {
           updateSpeakerOrder(speakerList, speakerToRemove);
        }
    }

    public boolean hasSpeakerSubscribedByType(Session session, Long speakerId, ESpeakerType type) {
        return session.getSpeakerList().stream()
                .anyMatch(s -> s.getParlamentarId().equals(speakerId) && s.getType() == type);
    }

    private void updateSpeakerOrder(List<SpeakerSession> speakerList, SpeakerSession speakerToRemove) {

        int removedOrder = speakerToRemove.getSpeakerOrder();
        speakerList.remove(speakerToRemove);
        speakerRepository.delete(speakerToRemove);

        speakerList.stream()
                .filter(s -> s.getSpeakerOrder() > removedOrder)
                .forEach(s -> s.setSpeakerOrder(s.getSpeakerOrder() - 1));

        speakerRepository.saveAll(speakerList);
    }

}
