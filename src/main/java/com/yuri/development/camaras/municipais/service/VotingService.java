package com.yuri.development.camaras.municipais.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yuri.development.camaras.municipais.domain.*;
import com.yuri.development.camaras.municipais.dto.SubjectVotingDTO;
import com.yuri.development.camaras.municipais.dto.VoteDTO;
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
import org.springframework.web.server.ResponseStatusException;

import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.yuri.development.camaras.municipais.util.EventConstants.*;

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

    @Autowired
    private SAPLService saplService;

    Logger logger = Logger.getLogger(VotingService.class.getName());
    public List<Voting> findAllBySession(Session session){
        return votingRepository.findAllBySession(session);
    }

    @Transactional
    public ResponseEntity<?> create(Session session, List<SubjectVotingDTO> subjectDTOList) throws JsonProcessingException {

        Voting voting = null;

        try{

            Set<Long> subjectDTOIds = subjectDTOList.stream()
                    .map(SubjectVotingDTO::getId)
                    .collect(Collectors.toSet());

            List<Subject> subjectList = session.getSubjectList().stream().filter(subject -> subjectDTOIds.contains(subject.getId())).collect(Collectors.toList());
            LegislativeSubjectType legislativeSubjectType = extractLegislativeSubjectTypeFromSubjectList(session.getTownHall(), subjectDTOList);

            voting = new Voting(session, subjectList, EVoting.VOTING);
            voting.setLegislativeSubjectType(legislativeSubjectType);
            voting.setAuthor(getAuthorsFromSAPL(session.getTownHall(), subjectList));
            settingAndCustomizingVotingDescription(voting);

            voting = votingRepository.save(voting);

            for(Subject subject : subjectList){
                subject.setVoting(voting);
                subject.setStatus(EVoting.VOTING);
            }

            List<ParlamentarVoting> parlamentarVotingList = new ArrayList<>();

            //add only the active parlamentares
            for(ParlamentarPresence parlamentarPresence : session.getParlamentarPresenceList()){

                if(parlamentarPresence.getParlamentar().getActive()){

                    String parlamentarName = parlamentarPresence.getParlamentar().getName();
                    String politicalParty = parlamentarPresence.getParlamentar().getPoliticalParty();
                    ParlamentarVoting parlamentarVoting = new ParlamentarVoting(null, voting,
                            parlamentarPresence.getParlamentar().getId(), parlamentarName, politicalParty,EVoting.NULL);
                    parlamentarVoting = parlamentarVotingService.save(parlamentarVoting);
                    parlamentarVotingList.add(parlamentarVoting);
                }
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

    private void settingAndCustomizingVotingDescription(Voting voting) {

        if(voting.getSubjectList().size() == 1){
            voting.setDescription(voting.getLegislativeSubjectType().getTitle());
            voting.setSubDescription(voting.getSubjectList().get(0).getOriginalEmenta());
        }else{
            voting.setDescription(getRightPluralType(voting.getLegislativeSubjectType().getTitle()) + " (Bloco)");

            String subDescription = "Nºs: ";
            for(Subject subject : voting.getSubjectList()){
                subDescription += extractOrdinalNumbers(subject.getDescription()) + ", ";
            }
            //TODO - change this hardcoded year
            subDescription = subDescription.substring(0, subDescription.length() - 2);
            subDescription += " (de 2025)";
            voting.setSubDescription(subDescription);
        }
    }

    private String extractOrdinalNumbers(String input) {

        String result = "";
        Pattern pattern = Pattern.compile("nº\\s*(\\d+)");
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            result = matcher.group(1);
        }

        return result;
    }

    private String getRightPluralType(String type) {

        if(type.endsWith("em")){
            return type.replace("m", "ns");
        }

        if(type.endsWith("ão")){
            return type.replace("ão", "ões");
        }

        return type + "s";
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

    private void addTypeFromOriginalSAPLSubject(Subject subject, List<String> subjecTypeList) {
        String [] splitType = subject.getDescription().split("nº");
        splitType = splitType[0].split("-");
        subjecTypeList.add(StringUtils.isNotBlank(splitType[1]) ? splitType[1].trim() : "");
    }

    private String getAuthorsFromSAPL(TownHall townHall, List<Subject> subjectList) throws JsonProcessingException {

        Set<String> authorsSet = new HashSet<>();
        for(Subject subject : subjectList){
            String author = subject.getIsManuallyAdded() ? subject.getAuthor() :
                    saplService.getSubjectAuthor(townHall.getApiURL(), subject.getSaplMateriaId().toString());
            authorsSet.add(author);
        }
        if(authorsSet.size() > 5){
            return "Vários autores";
        }
        return authorsSet.toString().substring(1, authorsSet.toString().length() - 1);
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

            voting =  votingRepository.save(voting);
            return voting;
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

        int halfTownhallPlusOne = (int) Math.ceil((double) numberOfParlamentaresTownhall / 2);
        int twoThirds = (int) Math.floor((double) numberOfParlamentaresTownhall / 3) * 2;
        twoThirds = numberOfParlamentaresTownhall % 2 == 0 ? twoThirds + 1 : twoThirds;
        List<ParlamentarVoting> votingListToBeConsidered = voting.getParlamentarVotingList();
        EVotingTypeResult votingTypeResult = voting.getLegislativeSubjectType().getResultType();

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

        MessageFormat messageFormat = new MessageFormat(VOTING_RESULT_DESCRIPTION);
        String eventDescription = messageFormat.format(new Object[]{session.getTownHall().getName(), voting.getId(),
                votingListToBeConsidered.size(), votingTypeResult, yesCount, noCount, abstentionCount });

        logger.log(Level.INFO, "Event_id = {0}, Event_description = {1}", new Object[]{VOTING_RESULT, eventDescription});
    }

    public void resetResultVote(Voting voting) {
        voting.setResult("");
        this.votingRepository.save(voting);
    }
}

