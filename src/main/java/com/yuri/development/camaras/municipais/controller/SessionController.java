package com.yuri.development.camaras.municipais.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yuri.development.camaras.municipais.controller.request.AddSubjectRequest;
import com.yuri.development.camaras.municipais.domain.ParlamentarPresence;
import com.yuri.development.camaras.municipais.domain.Session;
import com.yuri.development.camaras.municipais.domain.SpeakerSession;
import com.yuri.development.camaras.municipais.domain.Subject;
import com.yuri.development.camaras.municipais.dto.*;
import com.yuri.development.camaras.municipais.enums.ESpeakerType;
import com.yuri.development.camaras.municipais.exception.ApiErrorException;
import com.yuri.development.camaras.municipais.exception.RSVException;
import com.yuri.development.camaras.municipais.exception.ResourceNotFoundException;
import com.yuri.development.camaras.municipais.service.SessionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(value = "/sessions")
public class SessionController {

    @Value("${com.yuri.rsvgestaopublica.enableDeletion}")
    private boolean appEnableDeletion;

    @Autowired
    private SessionService sessionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> create(@RequestBody @Valid SessionDTOCreate sessionDTOCreate) throws ResourceNotFoundException, ApiErrorException {
        return new ResponseEntity<>(sessionService.create(sessionDTOCreate), HttpStatus.CREATED);
    }

    //TODO: remove - unnecessary method
    @GetMapping(value = "/check/townhall/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> checkIfExistsOpenSessionToday(@PathVariable ("id") Long townHallId){
        if(townHallId == null || townHallId == 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id da câmara não pode ser nulo");
        }

        return this.sessionService.checkIfExistsOpenSessionToday(townHallId);
    }

    @GetMapping(value = "/find/townhall/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> findSessionTodayByTownhall(@PathVariable ("id") Long townHallId){
        if(townHallId == null || townHallId == 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id da câmara não pode ser nulo");
        }

        return this.sessionService.findSessionTodayByTownhall(townHallId);
    }

    @GetMapping(value = "/{uuid}")
    @ResponseStatus(HttpStatus.OK)
    public Session findById(@PathVariable String uuid){

        return this.sessionService.findByUuid(uuid);
    }

    //TODO remove - unused method
    @GetMapping(value="/{uuid}/presence-list")
    @ResponseStatus(HttpStatus.OK)
    public List<ParlamentarPresence> getPresenceListBySession(@PathVariable ("uuid") String uuid){
        if(StringUtils.isBlank(uuid)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O id da sessão ou da câmara não pode ser nulo");
        }

        return this.sessionService.getPresenceListBySession(uuid);
    }

    @PutMapping(value = "/{uuid}/presence-list")
    @CrossOrigin(origins = {"http://localhost:4200", "https://camaras-municipais-frontend.vercel.app/"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePresenceOfParlamentar(@PathVariable ("uuid") String uuid, @RequestBody ParlamentarPresenceDTO presenceDTO){

        if(StringUtils.isBlank(uuid)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O id da sessão ou da câmara não pode ser nulo");
        }

        this.sessionService.updatePresenceOfParlamentar(uuid, presenceDTO);
    }

    @PutMapping(value = "/{uuid}/presence-list/manually/")
    @CrossOrigin(origins = {"http://localhost:4200", "https://camaras-municipais-frontend.vercel.app/"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePresenceOfParlamentarList(@PathVariable ("uuid") String uuid, @RequestBody List<Long> parlamentarListId){
        if(StringUtils.isBlank(uuid)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O id da sessão ou da câmara não pode ser nulo");
        }

        this.sessionService.updatePresenceOfParlamentarList(uuid, parlamentarListId);
    }

    //TODO: remove - unnecessary method
    @GetMapping(value="/{uuid}/speakers")
    @ResponseStatus(HttpStatus.OK)
    public List<SpeakerSession> getSpeakerListBySession(@PathVariable ("uuid") String uuid){
        if(StringUtils.isBlank(uuid)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O id da sessão ou da câmara não pode ser nulo");
        }

        return this.sessionService.getSpeakerListBySession(uuid);
    }

    @PostMapping(value = "/{uuid}/speakers")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> subscriptionInSpeakerList(@PathVariable ("uuid") String uuid, @RequestBody SpeakerSubscriptionDTO speakerDTO) throws ResourceNotFoundException {
        if(StringUtils.isBlank(uuid) || speakerDTO == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Objeto não pode ser nulo");
        }
        return sessionService.subscriptionInSpeakerList(uuid, speakerDTO);
    }

    @DeleteMapping(value = "/{uuid}/speakers/{id}/type/{type}")
    @CrossOrigin(origins = {"http://localhost:4200", "https://camaras-municipais-frontend.vercel.app/"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<?> unsubscribeSpeaker(@PathVariable ("uuid") String uuid, @PathVariable ("id") Long speakerId, @PathVariable ("type") ESpeakerType type) throws ResourceNotFoundException {
        if(StringUtils.isBlank(uuid)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O id da sessão ou da câmara não pode ser nulo");
        }
        sessionService.unsubscribeSpeaker(uuid, speakerId, type);
        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
    }

    @GetMapping(value = "/{uuid}/subjects")
    @ResponseStatus(HttpStatus.OK)
    public List<Subject> findAllSubjectsOfSession(@PathVariable String uuid, @RequestParam String status){

        if(StringUtils.isBlank(uuid) || StringUtils.isBlank(status)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O id da sessão ou status não pode ser nulo");
        }
        return this.sessionService.findAllSubjectsOfSession(uuid, status);
    }


    @PostMapping(value = "/{uuid}/voting")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createVoting(@PathVariable String uuid, @RequestBody List<SubjectVotingDTO> subjectList) throws JsonProcessingException {
        if(StringUtils.isBlank(uuid) || subjectList == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não se pode iniciar uma votação sem ter uma matéria para ser votada");
        }
        return this.sessionService.createVoting(uuid, subjectList);
    }
    @PutMapping(value = "/{uuid}/voting")
    @CrossOrigin(origins = {"http://localhost:4200", "https://camaras-municipais-frontend.vercel.app/"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void computeVote(@PathVariable ("uuid") String sessionUUID, @RequestBody VoteDTO vote){
        if(vote == null || vote.getParlamentarId() == null || StringUtils.isBlank(vote.getOption())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não pode enviar um objeto nulo ou que tenha valores nulos");
        }
        this.sessionService.computeVote(sessionUUID, vote);
    }

    @PutMapping(value = "/{uuid}/voting/close")
    @CrossOrigin(origins = {"http://localhost:4200", "https://camaras-municipais-frontend.vercel.app/"})
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> closeVoting(@PathVariable ("uuid") String sessionUUID) throws RSVException {
        if(StringUtils.isBlank(sessionUUID)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não pode enviar um objeto nulo ou que tenha valores nulos");
        }
        return this.sessionService.closeVoting(sessionUUID);
    }

    @DeleteMapping(value="/{uuid}")
    @CrossOrigin(origins = {"http://localhost:4200", "https://camaras-municipais-frontend.vercel.app/"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<?> delete(@PathVariable String uuid){
        if(StringUtils.isBlank(uuid)){
            return new ResponseEntity<>(new ApiErrorException(1002, "UUID não pode ser vazio ou nulo", HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
        }

        this.sessionService.delete(uuid);
        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
    }

    @GetMapping(value = "/{uuid}/voting-info/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> findSessionVotingInfoBySessionAndVotingId(@PathVariable String uuid, @PathVariable Long id){
        return this.sessionService.findSessionVotingInfoBySessionAndVotingId(uuid, id);
    }

    @GetMapping(value = "/{uuid}/voting-info/reset/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> resetSessionVotingInfoBySessionAndVotingId(@PathVariable String uuid, @PathVariable Long id){
        return this.sessionService.resetSessionVotingInfoBySessionAndVotingId(uuid, id);
    }

    @GetMapping(value = "/{uuid}/voting-info/standard")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getSessionVotingInfoStandardByUUID(@PathVariable String uuid){
        return this.sessionService.getSessionVotingInfoStandardByUUID(uuid);
    }

    @PostMapping(value = "/{uuid}/subjects")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> addSubject(@PathVariable String uuid, @RequestBody AddSubjectRequest request) throws ResourceNotFoundException {
        return new ResponseEntity<>(sessionService.addSubject(uuid, request), HttpStatus.CREATED);
    }

    @DeleteMapping(value = "/{uuid}/subjects/{id}")
    @CrossOrigin(origins = {"http://localhost:4200", "https://camaras-municipais-frontend.vercel.app/"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeSubject(@PathVariable String uuid, @PathVariable Long id) {
        sessionService.removeSubject(uuid, id);
    }

}
