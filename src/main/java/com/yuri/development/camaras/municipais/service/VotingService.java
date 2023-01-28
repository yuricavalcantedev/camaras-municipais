package com.yuri.development.camaras.municipais.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuri.development.camaras.municipais.GlobalConstants;
import com.yuri.development.camaras.municipais.domain.*;
import com.yuri.development.camaras.municipais.domain.api.EmentaAPI;
import com.yuri.development.camaras.municipais.domain.api.EmentaWrapperAPI;
import com.yuri.development.camaras.municipais.domain.api.SessionFromAPI;
import com.yuri.development.camaras.municipais.domain.api.TipoMateriaWrapperAPI;
import com.yuri.development.camaras.municipais.dto.SubjectVotingDTO;
import com.yuri.development.camaras.municipais.dto.VoteDTO;
import com.yuri.development.camaras.municipais.enums.EPresence;
import com.yuri.development.camaras.municipais.enums.EVoting;
import com.yuri.development.camaras.municipais.repository.VotingRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VotingService {

    @Autowired
    private VotingRepository votingRepository;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private LegislativeSubjectTypeService legislativeSubjectTypeService;

    @Autowired
    private ParlamentarVotingService parlamentarVotingService;
    public List<Voting> findAllBySession(Session session){
        return this.votingRepository.findAllBySession(session);
    }

    @Transactional
    public Voting create(Session session, List<SubjectVotingDTO> subjectDTOList) throws JsonProcessingException {

        boolean existsVotedSubject = subjectService.existsVotedSubject(session, subjectDTOList);
        List<Subject> subjectList = subjectDTOList.stream().map(subject -> new Subject(subject.getId(), session, subject.getDescription(), subject.getSaplMateriaId())).collect(Collectors.toList());
        LegislativeSubjectType legislativeSubjectType = this.extractLegislativeSubjectTypeFromSubjectList(session.getTownHall(), subjectDTOList);

        Voting voting = new Voting(session, subjectList, EVoting.VOTING);
        voting.setLegislativeSubjectType(legislativeSubjectType);

        if(subjectList.size() == 1){
            voting.setDescription(this.getDescriptionFromSAPL(session.getTownHall(), subjectList.get(0)));
        }else{
            voting.setDescription(this.getVotingPluralDescriptionFromLegislativeSubjectType(legislativeSubjectType));
        }

        voting = this.votingRepository.save(voting);

        for(Subject subject : subjectList){
            subject.setVoting(voting);
            subject.setStatus(EVoting.VOTING);
        }

        List<ParlamentarVoting> parlamentarVotingList = new ArrayList<>();
        for(ParlamentarPresence parlamentarPresence : session.getParlamentarPresenceList()){

            String parlamentarName = parlamentarPresence.getParlamentar().getName();
            String politicalParty = parlamentarPresence.getParlamentar().getPoliticalParty();

            ParlamentarVoting parlamentarVoting = new ParlamentarVoting(null, voting, parlamentarPresence.getParlamentar().getId(), parlamentarName, politicalParty,EVoting.NULL);
            parlamentarVoting = this.parlamentarVotingService.save(parlamentarVoting);
            parlamentarVotingList.add(parlamentarVoting);
        }

        try{

            voting.setParlamentarVotingList(parlamentarVotingList);
            session.getVotingList().add(voting);
            this.subjectService.saveAll(subjectList);
        }catch(Exception ex){
            throw ex;
        }

        return voting;
    }

    private LegislativeSubjectType extractLegislativeSubjectTypeFromSubjectList(TownHall townHall, List<SubjectVotingDTO> subjectVotingDTOList){

        List<String> subjecTypeList = new ArrayList<>();
        boolean hasSameType = false;

        for(SubjectVotingDTO subject : subjectVotingDTOList){
            String [] splitType = subject.getDescription().split("nº");
            splitType = splitType[0].split("-");
            subjecTypeList.add(StringUtils.isNotBlank(splitType[1]) ? splitType[1].trim() : "");
        }

        hasSameType = subjecTypeList.stream().allMatch(type -> type.equals(subjecTypeList.get(0)));

        if(!hasSameType){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não existe uma configuração de resultado para esse tipo de matéria: " + subjecTypeList.get(0));
        }

        return this.legislativeSubjectTypeService.findByTownHallAndTitle(townHall, subjecTypeList.get(0));
    }

    private String getVotingPluralDescriptionFromLegislativeSubjectType(LegislativeSubjectType legislativeSubjectType){

        String [] firstWord = legislativeSubjectType.getTitle().split(" ");

        if(firstWord[0].endsWith("em")){
            return legislativeSubjectType.getTitle().replace("m", "ns");
        }

        if(firstWord[0].endsWith("ão")){
            return legislativeSubjectType.getTitle().replace("ão", "ões");
        }

        return legislativeSubjectType.getTitle() + "s";
    }

    private String getDescriptionFromSAPL(TownHall townHall, Subject subject) throws JsonProcessingException {

        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();
        String url = townHall.getApiURL() + GlobalConstants.GET_EMENTA_BY_SUBJECT.replace("{id}", subject.getSaplMateriaId().toString());
        EmentaAPI ementaAPI = objectMapper.readValue(restTemplate.getForObject(url, String.class), EmentaAPI.class);
        if(ementaAPI == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id da matéria inválido!");
        }

        return ementaAPI.getContent();
    }

    public boolean existsOpenVoting(Session session){

        for(int i = 0; i < session.getVotingList().size(); i++){
            if(session.getVotingList().get(i).getStatus().equals(EVoting.VOTING)){
                return true;
            }
        }

        return false;
    }

    public void computeVote(Session session, VoteDTO vote){

        ParlamentarVoting parlamentarVoting = this.parlamentarVotingService.findByIdAndParlamentarId(vote.getParlamentarVotingId(), vote.getParlamentarId());
        if(parlamentarVoting != null){
            for(EVoting eVoting : EVoting.values()){
                if(eVoting.name().equals(vote.getOption())){
                    parlamentarVoting.setResult(eVoting);
                }
            }
        }

        this.parlamentarVotingService.save(parlamentarVoting);
    }

    public Voting closeVoting(Session session) {

        if(this.existsOpenVoting(session)){

            Voting voting = session.getVotingList().stream().filter(v -> v.getStatus().equals(EVoting.VOTING)).findFirst().orElse(null);
            voting.setStatus(EVoting.VOTED);

            int presenceOnSession = session.getParlamentarPresenceList().stream().map(presence -> presence.getStatus().equals(EPresence.PRESENCE) ? 1 : 0).mapToInt(Integer::valueOf).sum();
            int numberOfVotes = voting.getParlamentarVotingList().stream().map(vote -> !vote.getResult().equals(EVoting.NULL) ? 1 : 0).mapToInt(Integer::valueOf).sum();
            voting.computeVotes(presenceOnSession, numberOfVotes);

            return this.votingRepository.save(voting);
        }else{
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nao existe uma votacao aberta no momento");
        }


    }
}
