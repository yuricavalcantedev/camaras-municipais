package com.yuri.development.camaras.municipais.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuri.development.camaras.municipais.GlobalConstants;
import com.yuri.development.camaras.municipais.domain.*;
import com.yuri.development.camaras.municipais.domain.api.AuthorAPI;
import com.yuri.development.camaras.municipais.domain.api.EmentaAPI;
import com.yuri.development.camaras.municipais.dto.SubjectVotingDTO;
import com.yuri.development.camaras.municipais.dto.VoteDTO;
import com.yuri.development.camaras.municipais.enums.EPresence;
import com.yuri.development.camaras.municipais.enums.EVoting;
import com.yuri.development.camaras.municipais.enums.EVotingTypeResult;
import com.yuri.development.camaras.municipais.exception.ApiErrorException;
import com.yuri.development.camaras.municipais.exception.RSVException;
import com.yuri.development.camaras.municipais.repository.VotingRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.yuri.development.camaras.municipais.util.EventConstants.DATABASE_STRUCUTRE_ERROR;
import static com.yuri.development.camaras.municipais.util.EventConstants.DATABASE_STRUCUTRE_ERROR_DESCRIPTION;

@Service
public class VotingService {

    @Autowired
    private VotingRepository votingRepository;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private LegislativeSubjectTypeService legislativeSubjectTypeService;

    @Autowired
    private TableRoleService tableRoleService;

    @Autowired
    private ParlamentarVotingService parlamentarVotingService;
    public List<Voting> findAllBySession(Session session){
        return votingRepository.findAllBySession(session);
    }

    @Transactional
    public ResponseEntity<?> create(Session session, List<SubjectVotingDTO> subjectDTOList) throws JsonProcessingException {

        Voting voting = null;

        try{
            boolean existsVotedSubject = subjectService.existsVotedSubject(session, subjectDTOList);
            List<Subject> subjectList = subjectDTOList.stream().map(subject -> new Subject(subject.getId(), session, subject.getDescription(), subject.getSaplMateriaId())).collect(Collectors.toList());
            LegislativeSubjectType legislativeSubjectType = extractLegislativeSubjectTypeFromSubjectList(session.getTownHall(), subjectDTOList);

            voting = new Voting(session, subjectList, EVoting.VOTING);
            voting.setLegislativeSubjectType(legislativeSubjectType);

            if(subjectList.size() == 1){
                voting.setDescription(getDescriptionFromSAPL(session.getTownHall(), subjectList.get(0)));
            }else{
                voting.setDescription(getVotingPluralDescriptionFromLegislativeSubjectType(legislativeSubjectType));
            }

            voting.setAuthor(getAuthorFromSAPL(session.getTownHall(), subjectList.get(0)));

            voting =
                    votingRepository.save(voting);

            for(Subject subject : subjectList){
                subject.setVoting(voting);
                subject.setStatus(EVoting.VOTING);
            }

            List<ParlamentarVoting> parlamentarVotingList = new ArrayList<>();
            for(ParlamentarPresence parlamentarPresence : session.getParlamentarPresenceList()){

                String parlamentarName = parlamentarPresence.getParlamentar().getName();
                String politicalParty = parlamentarPresence.getParlamentar().getPoliticalParty();
                ParlamentarVoting parlamentarVoting = new ParlamentarVoting(null, voting, parlamentarPresence.getParlamentar().getId(), parlamentarName, politicalParty,EVoting.NULL);
                parlamentarVoting = parlamentarVotingService.save(parlamentarVoting);
                parlamentarVotingList.add(parlamentarVoting);
            }

            voting.setParlamentarVotingList(parlamentarVotingList);
            session.getVotingList().add(voting);
            subjectService.saveAll(subjectList);
        }catch (RSVException ex){
            return new ResponseEntity<>(new ApiErrorException(1001, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }catch(DataIntegrityViolationException ex){
            return new ResponseEntity<>(new ApiErrorException(DATABASE_STRUCUTRE_ERROR, DATABASE_STRUCUTRE_ERROR_DESCRIPTION), HttpStatus.INTERNAL_SERVER_ERROR);
        }catch(Exception ex){
            return new ResponseEntity<>(new ApiErrorException(1001, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(voting, HttpStatus.OK);
    }

    private LegislativeSubjectType extractLegislativeSubjectTypeFromSubjectList(TownHall townHall, List<SubjectVotingDTO> subjectVotingDTOList) throws Exception {

        List<String> subjecTypeList = new ArrayList<>();
        boolean hasSameType = false;

        for(SubjectVotingDTO subject : subjectVotingDTOList){
            String [] splitType = subject.getDescription().split("nº");
            splitType = splitType[0].split("-");
            subjecTypeList.add(StringUtils.isNotBlank(splitType[1]) ? splitType[1].trim() : "");
        }

        hasSameType = subjecTypeList.stream().allMatch(type -> type.equals(subjecTypeList.get(0)));

        if(!hasSameType){
            throw new RSVException("Não se pode abrir uma votação com tipos diferentes de matérias");
        }

        List<LegislativeSubjectType> subjectTypeList = legislativeSubjectTypeService.findByTownHall(townHall);
        for(LegislativeSubjectType legislativeSubjectType : subjectTypeList){
            if(legislativeSubjectType.getTitle().equals(subjecTypeList.get(0))){
                return legislativeSubjectType;
            }
        }
        throw new RSVException("Não existe um tipo de matéria para a(s) ementa(s) selecionada(s)");
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

    private String getAuthorFromSAPL(TownHall townHall, Subject subject) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();

        String urlAuthor = townHall.getApiURL() + GlobalConstants.GET_EMENTA_AUTHOR_BY_SUBJECT.replace("{id}", subject.getSaplMateriaId().toString());
        AuthorAPI authorAPI = objectMapper.readValue(restTemplate.getForObject(urlAuthor, String.class), AuthorAPI.class);
        List<String> names = authorAPI.getResults()
                .stream()
                .map(author -> Arrays.stream(author.getStr().split("-")).findFirst().get().replace("Autoria: ", "").trim())
                .collect(Collectors.toList());

        return String.join(", ", names);
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

        ParlamentarVoting parlamentarVoting = parlamentarVotingService.findByIdAndParlamentarId(vote.getParlamentarVotingId(), vote.getParlamentarId());
        if(parlamentarVoting != null){
            for(EVoting eVoting : EVoting.values()){
                if(eVoting.name().equals(vote.getOption())){
                    parlamentarVoting.setResult(eVoting);
                }
            }
        }

        parlamentarVotingService.save(parlamentarVoting);
    }

    public Voting closeVoting(Session session) throws RSVException {

        if(existsOpenVoting(session)){

            Voting voting = session.getVotingList().stream()
                                        .filter(v -> v.getStatus().equals(EVoting.VOTING))
                                        .findFirst().get();

            voting.setStatus(EVoting.VOTED);
            computeVotesAndDecideResult(session, voting);

            return votingRepository.save(voting);
        }else{
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nao existe uma votacao aberta no momento");
        }
    }

    public void resetVote(VoteDTO vote) {

        ParlamentarVoting parlamentarVoting = parlamentarVotingService.findByIdAndParlamentarId(vote.getParlamentarVotingId(), vote.getParlamentarId());
        if (parlamentarVoting != null) {
            parlamentarVoting.setResult(EVoting.NULL);
        }

        parlamentarVotingService.save(parlamentarVoting);
    }

    private void computeVotesAndDecideResult(Session session, Voting voting) throws RSVException {

        Optional<TableRole> optPresidentRole = tableRoleService.findPresidentIdByTownhall(session.getTownHall());
        if(optPresidentRole.isEmpty()){
            //return httpobject
            throw new RSVException("Camara sem presidente");
        }

        int presidentId = Math.toIntExact(optPresidentRole.get().getParlamentar().getId());
        int numberOfParlamentaresTownhall = session.getTownHall().getUserList().size();
        int presentOnSession = 0, numberOfVotes = 0;

        int halfTownhallPlusOne = (int) Math.ceil((double) numberOfParlamentaresTownhall / 2);
        int twoThirds = (int) Math.floor((double) numberOfParlamentaresTownhall / 3) * 2;
        twoThirds = numberOfParlamentaresTownhall % 2 == 0 ? twoThirds + 1 : twoThirds;

        for(ParlamentarPresence presence : session.getParlamentarPresenceList()){
            presentOnSession += presence.getStatus().equals(EPresence.PRESENCE) ? 1 : 0;
        }

        for(ParlamentarVoting vote : voting.getParlamentarVotingList()){
            numberOfVotes += vote.getResult().equals(EVoting.NULL) ? 0 : 1;
        }

        List<ParlamentarVoting> votingListToBeConsidered = voting.getParlamentarVotingList();
        EVotingTypeResult votingTypeResult = voting.getLegislativeSubjectType().getResultType();

        //if this is true, I don't count president's vote, so I need to remove him/her from the list
//        if(votingTypeResult == EVotingTypeResult.MAIORIA_SIMPLES ||
////                (votingTypeResult == EVotingTypeResult.MAIORIA_ABSOLUTA && numberOfVotes > halfPresentPlusOne)){
////            Optional<ParlamentarVoting> optPresidentVoting = votingListToBeConsidered.stream()
////                    .filter(pVoting -> pVoting.getParlamentarId() == presidentId)
////                    .findFirst();
////            if(optPresidentVoting.isPresent()){
////                votingListToBeConsidered.remove(optPresidentVoting.get());
////            }else{
////                //return httpobject
////                throw new RSVException("");
////            }
////        }

        int yesCount = 0, noCount = 0, abstentionCount = 0;

        for(ParlamentarVoting parlamentarVote : votingListToBeConsidered){
            if(parlamentarVote.getResult().equals(EVoting.YES)) {
                yesCount = yesCount + 1;
            } else if(parlamentarVote.getResult().equals(EVoting.NO)) {
                noCount = noCount + 1;
            } else if(parlamentarVote.getResult().equals(EVoting.ABSTENTION)) {
                abstentionCount = abstentionCount + 1;
            }
        }


        String result = "REJEITADA - ";

        switch (votingTypeResult){
            case MAIORIA_SIMPLES:
                if(yesCount > noCount){ result = "APROVADA - ";}
                break;
            case MAIORIA_ABSOLUTA:
                if(yesCount >= halfTownhallPlusOne){ result = "APROVADA - ";}
                break;
            case MAIORIA_QUALIFICADA:
                if(yesCount >= twoThirds){result = "APROVADA - ";}
                break;
            default: result = "";
        }

        result = result + votingTypeResult.getDescription();

        voting.setYesCount(yesCount - abstentionCount);
        voting.setNoCount(noCount);
        voting.setAbstentionCount(abstentionCount);
        voting.setResult(result);
    }

    public void resetResultVote(Voting voting) {
        voting.setResult("");
        this.votingRepository.save(voting);
    }
}

