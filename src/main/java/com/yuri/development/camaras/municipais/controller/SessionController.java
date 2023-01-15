package com.yuri.development.camaras.municipais.controller;

import com.yuri.development.camaras.municipais.domain.*;
import com.yuri.development.camaras.municipais.dto.*;
import com.yuri.development.camaras.municipais.service.SessionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(value = "/sessions")
public class SessionController {

    @Autowired
    private SessionService sessionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Session create(@RequestBody @Valid SessionDTOCreate sessionDTOCreate){
        return this.sessionService.create(sessionDTOCreate);
    }

    @GetMapping(value = "/check/townhall/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Boolean checkIfExistsOpenSessionToday(@PathVariable ("id") Long townHallId){
        if(townHallId == null || townHallId == 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id da câmara não pode ser nulo");
        }

        return this.sessionService.checkIfExistsOpenSessionToday(townHallId);
    }

    @GetMapping(value = "/find/townhall/{id}")
    @ResponseStatus(HttpStatus.OK)
    public SessionToParlamentarDTO findSessionTodayByTownhall(@PathVariable ("id") Long townHallId){
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

    @GetMapping(value="/{uuid}/speaker-list")
    @ResponseStatus(HttpStatus.OK)
    public List<SpeakerSession> getSpeakerListBySession(@PathVariable ("uuid") String uuid){
        if(StringUtils.isBlank(uuid)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O id da sessão ou da câmara não pode ser nulo");
        }

        return this.sessionService.getSpeakerListBySession(uuid);
    }

    @PostMapping(value = "/{uuid}/speaker-list")
    @ResponseStatus(HttpStatus.CREATED)
    public SpeakerSession subscriptionInSpeakerList(@PathVariable ("uuid") String uuid, @RequestBody SpeakerSubscriptionDTO speakerDTO){
        if(StringUtils.isBlank(uuid) || speakerDTO == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Objeto não pode ser nulo");
        }
        return this.sessionService.subscriptionInSpeakerList(uuid, speakerDTO);
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
    public Voting createVoting(@PathVariable String uuid, @RequestBody List<SubjectVotingDTO> subjectList){
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
    public Voting closeVoting(@PathVariable ("uuid") String sessionUUID){
        if(StringUtils.isBlank(sessionUUID)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não pode enviar um objeto nulo ou que tenha valores nulos");
        }

        return this.sessionService.closeVoting(sessionUUID);
    }

    @DeleteMapping(value="/{uuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String uuid){
        if(StringUtils.isBlank(uuid)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "UUID não pode ser vazio ou nulo");
        }

        this.sessionService.delete(uuid);
    }

    @DeleteMapping()
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAll(){
        this.sessionService.deleteAll();
    }


    @GetMapping(value = "/{uuid}/voting-info")
    @ResponseStatus(HttpStatus.OK)
    public SessionVotingInfoDTO findSessionVotingInfoByUUID(@PathVariable String uuid){

        return this.sessionService.findSessionVotingInfoByUUID(uuid);
    }

    @GetMapping(value = "/{uuid}/voting-info/standard")
    @ResponseStatus(HttpStatus.OK)
    public SessionVotingInfoDTO getSessionVotingInfoStandardByUUID(@PathVariable String uuid){

        return this.sessionService.getSessionVotingInfoStandardByUUID(uuid);
    }

}
