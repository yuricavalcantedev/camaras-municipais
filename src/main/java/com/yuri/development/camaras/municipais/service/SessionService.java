package com.yuri.development.camaras.municipais.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yuri.development.camaras.municipais.GlobalConstants;
import com.yuri.development.camaras.municipais.annotation.HLogger;
import com.yuri.development.camaras.municipais.controller.request.AddSubjectRequest;
import com.yuri.development.camaras.municipais.domain.*;
import com.yuri.development.camaras.municipais.domain.api.SessionFromAPI;
import com.yuri.development.camaras.municipais.dto.*;
import com.yuri.development.camaras.municipais.enums.EPresence;
import com.yuri.development.camaras.municipais.enums.ESpeakerType;
import com.yuri.development.camaras.municipais.enums.EVoting;
import com.yuri.development.camaras.municipais.exception.ApiErrorException;
import com.yuri.development.camaras.municipais.exception.RSVException;
import com.yuri.development.camaras.municipais.exception.ResourceNotFoundException;
import com.yuri.development.camaras.municipais.repository.SessionRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.yuri.development.camaras.municipais.util.EventConstants.*;

@Service
public class SessionService {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private TownHallService townHallService;

    @Autowired
    private UserService userService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private ParlamentarPresenceService parlamentarPresenceService;

    @Autowired
    private SpeakerService speakerService;

    @Autowired
    private VotingService votingService;

    @Autowired
    private SAPLService saplService;

    Logger logger = Logger.getLogger(SessionService.class.getName());

    @Transactional
    @HLogger(id = CREATE_SESSION, description = CREATE_SESSION_DESCRIPTION, isResponseEntity = false)
    public Session create(SessionDTOCreate sessionDTOCreate) throws ResourceNotFoundException, ApiErrorException {

        TownHall townHall = townHallService.findById(sessionDTOCreate.getTownHallId());
        Date today = Date.from(Instant.now());

        if(existsSessionByTownHallAndDate(townHall, today)){
            logAndThrowException(Level.SEVERE,
                    new Object[]{TOWNHALL_HAS_SESSION_ALREADY, TOWNHALL_HAS_SESSION_ALREADY_DESCRIPTION}, HttpStatus.BAD_REQUEST);
        }

        String urlToGetSessionData = townHall.getApiURL().concat(GlobalConstants.SEARCH_SESSAO).replace("{id}",
                sessionDTOCreate.getSaplSessionId().toString());

        SessionFromAPI sessionFromAPI = saplService.findSession(urlToGetSessionData);
        Session session = new Session(sessionDTOCreate.getSaplSessionId(), UUID.randomUUID().toString(), sessionFromAPI, townHall, today);

        String urlToGetOrdemDiaList = townHall.getApiURL().concat(GlobalConstants.SEARCH_ORDEM_DIA_BY_SESSAO).
                replace("{id}", sessionDTOCreate.getSaplSessionId().toString());

        //I need to save now, so I can save subjectList inside below method and then work with it in a parallel thread.
        sessionRepository.save(session);

        session.setSubjectList(subjectService.retrieveSubjectListFromSAPL(session, urlToGetOrdemDiaList));
        session.setParlamentarPresenceList(parlamentarPresenceService.createListForSession(session, townHall.getId()));
        return sessionRepository.save(session);
    }

    @HLogger(id = CREATE_VOTING_FOR_SESSION, description = CREATE_VOTING_FOR_SESSION_DESCRIPTION, hasUUID = true)
    public ResponseEntity<?> createVoting (String uuid, List<SubjectVotingDTO> subjectList) throws JsonProcessingException {

        Session session = findByUuid(uuid);
        if(session == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, SAPL_SESSION_NOT_FOUND_DESCRIPTION);
        }
        if(votingService.existsOpenVoting(session)){
            return new ResponseEntity<>(new ApiErrorException(1001, "Não pode criar uma votação enquanto houver outra em andamento", HttpStatus.BAD_REQUEST),
                    HttpStatus.BAD_REQUEST);
        }

        return votingService.create(session, subjectList);
    }

    //@HLogger(id = GET_SESSION_VOTING_INFO_STANDARD_BY_UUID, description = GET_SESSION_VOTING_INFO_STANDARD_BY_UUID_DESCRIPTION, hasUUID = true)
    public ResponseEntity<?> getSessionVotingInfoStandardByUUID(String uuid){

        Session session = this.findByUuid(uuid);
        TownHall townHall = session.getTownHall();
        List<ParlamentarInfoStatusDTO> parlamentarInfoStatusDTOList = new ArrayList<>();


        for(ParlamentarPresence parlamentarPresence : session.getParlamentarPresenceList()){
            Parlamentar parlamentar = parlamentarPresence.getParlamentar();
            String role = null;
            Integer priority = 0;

            if(!isTableRoleFullyConfigured(townHall.getTableRoleList())){
                return new ResponseEntity<>(new ApiErrorException(TOWNHALL_HAS_NO_TABLE_ROLE_DEFINED,
                        TOWNHALL_HAS_NO_TABLE_ROLE_DEFINED_DESCRIPTION, HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
            }

            for(int i = 0; i < townHall.getTableRoleList().size(); i++){
                TableRole tableRole = townHall.getTableRoleList().get(i);
                if (tableRole.getParlamentar().getName().equals(parlamentar.getName())) {
                    role = tableRole.getName();
                    priority = tableRole.getPosition();
                }
            }
            parlamentarInfoStatusDTOList.add(new ParlamentarInfoStatusDTO(parlamentar, "NULL", role,  parlamentarPresence.getStatus(), priority));
        }

        HashMap<String, List<ParlamentarInfoStatusDTO>> parlamentarMap = this.splitParlamentarVotingList(session, parlamentarInfoStatusDTOList);
        return new ResponseEntity<>(new SessionVotingInfoDTO(uuid, null, parlamentarMap.get("table"), parlamentarMap.get("other"), session.getSpeakerList()), HttpStatus.OK);
    }

    @HLogger(id = UPDATE_PARLAMENTAR_PRESENCE_MANUALLY, description = UPDATE_PARLAMENTAR_PRESENCE_MANUALLY_DESCRIPTION, hasUUID = true)
    public void updatePresenceOfParlamentarList(String uuid, List<Long> parlamentarListId) {

        try{

            Session session = this.findByUuid(uuid);
            for(ParlamentarPresence pPresence : session.getParlamentarPresenceList()){
                for(int i = 0; i < parlamentarListId.size(); i++){
                    if(pPresence.getId().equals(parlamentarListId.get(i))){
                        pPresence.setStatus(pPresence.getStatus().equals(EPresence.PRESENCE) ? EPresence.ABSCENSE : EPresence.PRESENCE);
                    }
                }
            }
            this.parlamentarPresenceService.saveAll(session.getParlamentarPresenceList());
        }catch(IllegalArgumentException ex){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A opção escolhida não é válida");
        }catch(Exception ex){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erro inesperado. Contacte o administrador");
        }
    }

    public ResponseEntity<?> checkIfExistsOpenSessionToday(Long townHallId) {

        Date today = Date.from(Instant.now());
        TownHall townHall = this.townHallService.findTownhallById(townHallId);
        Optional<Session> optionalSession = this.sessionRepository.findByTownHallAndDate(townHall, today);
        return new ResponseEntity<>(optionalSession.isPresent(), HttpStatus.OK);
    }

    public ResponseEntity<?> findSessionTodayByTownhall(Long townHallId) {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Date today = Date.from(Instant.now());
        TownHall townHall = this.townHallService.findTownhallById(townHallId);
        Optional<Session> optionalSession = this.sessionRepository.findByTownHallAndDate(townHall, today);
        if(optionalSession.isPresent()){

            Session session = optionalSession.get();
            String sessionSubjectURL = session.getTownHall().getApiURL() + GlobalConstants.HTML_PAGE_PAUTA_SESSAO.replace("{id}", session.getSaplId().toString());
            sessionSubjectURL = sessionSubjectURL.replace("api/", "");
            stopWatch.stop();

            logger.log(Level.INFO, "Event_id = {0}, Event_description = {1}, Duration(ms) = {2}, Townhall{3}",
                    new Object[]{FIND_TODAY_SESSION_BY_TOWNHALL, FIND_TODAY_SESSION_BY_TOWNHALL_DESCRIPTION, stopWatch.getTotalTimeMillis(), townHall.getName()});

            return new ResponseEntity<>( new SessionToParlamentarDTO(session, sessionSubjectURL), HttpStatus.OK);
        }

        return null;
    }

    public Session findByUuid(String uuid){

        Session session = null;
        if(StringUtils.isBlank(uuid)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id não pode ser vazio ou nulo");
        }
        Optional<Session> optSession = this.sessionRepository.findByUuid(uuid);
        if(optSession.isPresent()){
            session = optSession.get();
            session.getParlamentarPresenceList().sort(Comparator.comparing(ParlamentarPresence::getId));
        }
        return session;
    }

    public List<ParlamentarPresence> getPresenceListBySession(String sessionUuid) {

        Session session = this.findByUuid(sessionUuid);

        if(session != null){
            return this.parlamentarPresenceService.findAllBySession(session);
        }

        return new ArrayList<>();
    }

    //TODO to remove
    public List<SpeakerSession> getSpeakerListBySession(String uuid){

        Session session = this.findByUuid(uuid);

        if(session != null){
            return this.speakerService.findAllBySession(session);
        }

        return new ArrayList<>();
    }

    @HLogger(id = PARLAMENTAR_SUBSCRIPTION, description = PARLAMENTAR_SUBSCRIPTION_DESCRIPTION, hasUUID = true)
    public ResponseEntity<?> subscriptionInSpeakerList(String uuid, SpeakerSubscriptionDTO speakerDTO) throws ResourceNotFoundException {

        Session session = findByUuid(uuid);
        Parlamentar parlamentar = (Parlamentar) userService.findById(speakerDTO.getParlamentarId());
        TownHall townHall = townHallService.findById(speakerDTO.getTownhallId());

        if(session == null || parlamentar == null || townHall == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A sessão / parlamentar / câmara não existe");
        }

        Integer speakerOrder = speakerService.retrieveNextOrder(session, speakerDTO.getType());
        SpeakerSession speakerSession = new SpeakerSession(null, session, parlamentar.getId(), parlamentar.getName(),
                parlamentar.getPoliticalParty(),townHall.getId(), speakerOrder, speakerDTO.getType());

        speakerService.create(speakerSession);
        return new ResponseEntity<>(speakerSession, HttpStatus.OK);
    }

    @HLogger(id = PARLAMENTAR_UNSUBSCRIPTION, description = PARLAMENTAR_UNSUBSCRIPTION_DESCRIPTION, hasUUID = true, isResponseEntity = false)
    public void unsubscribeSpeaker(String uuid, Long speakerId, ESpeakerType type) {

        Session session = findByUuid(uuid);
        speakerService.removeSpeakerFromList(session, speakerId, type);
        sessionRepository.save(session);
    }


    public void updatePresenceOfParlamentar(String uuid, ParlamentarPresenceDTO presenceDTO) {

        Session session = findByUuid(uuid);
        Parlamentar parlamentar = (Parlamentar) this.userService.findById(presenceDTO.getParlamentarId());

        if(session == null || parlamentar == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A sessão / parlamentar não existe");
        }
        parlamentarPresenceService.updatePresenceOfParlamentar(session, parlamentar, presenceDTO.getStatus());
    }

    @HLogger(id = FIND_SUBJECTS_FOR_SESSION, description = FIND_SUBJECTS_FOR_SESSION_DESCRIPTION, hasUUID = true)
    public List<Subject>findAllSubjectsOfSession(String uuid, String status) {

        Session session = this.findByUuid(uuid);
        EVoting eVoting = EVoting.VOTED.name().equals(status) ? EVoting.VOTED : EVoting.NOT_VOTED;

        return subjectService.findAllBySessionAndStatus(session, eVoting);
    }

    @HLogger(id = CLOSE_VOTING_FOR_SESSION, description = CLOSE_VOTING_FOR_SESSION_DESCRIPTION, hasUUID = true)
    public ResponseEntity<?> closeVoting(String sessionUUID) throws RSVException {

        Session session = findByUuid(sessionUUID);
        return new ResponseEntity<>(votingService.closeVoting(session), HttpStatus.OK);
    }

    @HLogger(id = DELETE_SESSION, description = DELETE_SESSION_DESCRIPTION, hasUUID = true)
    public void delete(String uuid) {

        Session session = findByUuid(uuid);
        if(session != null){
            sessionRepository.delete(session);
        }
    }

    public void computeVote(String sessionUUID, VoteDTO vote) {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Session session = findByUuid(sessionUUID);
        Parlamentar parlamentar = (Parlamentar) this.userService.findById(vote.getParlamentarId());

        if(session == null || parlamentar == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A sessão / parlamentar não existe");
        }

        this.votingService.computeVote(session, vote);
        stopWatch.stop();

        logger.log(Level.SEVERE, "Event_id = {0}, Event_description = {1}, Duration(ms) = {2}, Townhall = {3}",
                new Object[] {COMPUTE_VOTE, COMPUTE_VOLTE_DESCRIPTION, stopWatch.getTotalTimeMillis(), session.getTownHall().getName()});
    }

    public void resetVote(Session session, List<VoteDTO> votes) {
        StopWatch stopWatch = new StopWatch();
        votes.forEach(vote -> {

            stopWatch.start();

            Parlamentar parlamentar = (Parlamentar) this.userService.findById(vote.getParlamentarId());

            if (session == null || parlamentar == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A sessão / parlamentar não existe");
            }

            votingService.resetVote(vote);

            stopWatch.stop();
            logger.log(Level.SEVERE, "Event_id = {0}, Event_description = {1}, Duration(ms) = {2}, Townhall = {3}, Parlamentar: {4}",
                    new Object[]{RESET_VOTE, RESET_VOTE_DESCRIPTION, stopWatch.getTotalTimeMillis(), session.getTownHall().getName(), parlamentar.getName()});
        });
    }

    public ResponseEntity<?> findSessionVotingInfoBySessionAndVotingId(String uuid, Long id){

        Session session = this.findByUuid(uuid);
        TownHall townHall = session.getTownHall();
        Voting voting = session.getVotingList().stream().filter(v -> v.getId().equals(id)).findFirst().orElse(null);

        if(voting != null){
            List<ParlamentarInfoStatusDTO> parlamentarInfoStatusDTOList = new ArrayList<>();

            parlamentarInfoStatusDTOList.addAll(voting.getParlamentarVotingList().stream().map(parlamentarVoting -> {

                Parlamentar parlamentar = (Parlamentar) this.userService.findById(parlamentarVoting.getParlamentarId());
                Optional<ParlamentarPresence> optionalParlamentarPresence = this.parlamentarPresenceService.findParlamentarPresenceBySessionIdAndParlamentar(session, parlamentar);
                String role = null;
                EPresence ePresence = optionalParlamentarPresence.isPresent() ? optionalParlamentarPresence.get().getStatus() : EPresence.OTHER;
                Integer priority = 0;

                for(int i = 0; i < townHall.getTableRoleList().size(); i++){

                    TableRole tableRole = townHall.getTableRoleList().get(i);
                    if (tableRole.getParlamentar().getName().equals(parlamentar.getName())) {
                        role = tableRole.getName();
                        priority = tableRole.getPosition();
                    }
                }
                return new ParlamentarInfoStatusDTO(parlamentar, parlamentarVoting.getResult().toString(), role, ePresence, priority);

            }).collect(Collectors.toList()));

            parlamentarInfoStatusDTOList.sort(Comparator.comparing(ParlamentarInfoStatusDTO::getParlamentar, Comparator.comparing(Parlamentar::getName)));
            HashMap<String, List<ParlamentarInfoStatusDTO>> parlamentarMap = this.splitParlamentarVotingList(session, parlamentarInfoStatusDTOList);
            return new ResponseEntity<>(new SessionVotingInfoDTO(uuid, voting, parlamentarMap.get("table"), parlamentarMap.get("other"), session.getSpeakerList()), HttpStatus.OK) ;
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id não pode ser vazio ou nulo");
    }

    @HLogger(id = ADD_SUBJECT_MANUALLY_TO_SESSION, description = ADD_SUBJECT_MANUALLY_TO_SESSION_DESCRIPTION, hasUUID = true, isResponseEntity = false)
    public Session addSubject(String sessionUUID, AddSubjectRequest request) {

        Session session = findByUuid(sessionUUID);
        subjectService.addSubjectToSession(session, request);

        return sessionRepository.save(session);
    }

    @HLogger(id = REMOVE_SUBJECT_FROM_SESSION, description = REMOVE_SUBJECT_FROM_SESSION_DESCRIPTION, hasUUID = true)
    public void removeSubject(String sessionUUID, Long subjectId) {
        Session session = findByUuid(sessionUUID);
        session.getSubjectList().removeIf(subject -> subject.getId().equals(subjectId));
        sessionRepository.save(session);
    }


    private HashMap<String, List<ParlamentarInfoStatusDTO>> splitParlamentarVotingList (Session session, List<ParlamentarInfoStatusDTO> parlamentarInfoStatusList){

        HashMap<String, List<ParlamentarInfoStatusDTO>> map = new HashMap<>();
        List<ParlamentarInfoStatusDTO> parlamentarTableList = new ArrayList<>();
        List<ParlamentarInfoStatusDTO> otherParlamentarList = new ArrayList<>();

        for(int i = 0; i < parlamentarInfoStatusList.size(); i++){
            ParlamentarInfoStatusDTO parlamentarInfoStatus = parlamentarInfoStatusList.get(i);
            if(parlamentarInfoStatus.getRole() == null){
                otherParlamentarList.add(parlamentarInfoStatus);
            }else{
                parlamentarTableList.add(parlamentarInfoStatus);
            }
        }

        Comparator<ParlamentarInfoStatusDTO> compareByPriority = (ParlamentarInfoStatusDTO o1, ParlamentarInfoStatusDTO o2) -> o1.getPriority().compareTo( o2.getPriority());
        Collections.sort(parlamentarTableList, compareByPriority);

        map.put("table", parlamentarTableList);
        map.put("other", otherParlamentarList);
        return map;
    }

    private boolean existsSessionByTownHallAndDate(TownHall townHall, Date date){
        return sessionRepository.findByTownHallAndDate(townHall, date).isPresent();
    }

    private boolean isTableRoleFullyConfigured(List<TableRole> tableRoleList){
        for(TableRole table : tableRoleList){
            if(table.getParlamentar() == null){
                return false;
            }
        }
        return true;
    }

    @HLogger(id = FIND_SESSION_VOTING_INFO, description = FIND_SESSION_VOTING_INFO_DESCRIPTION, hasUUID = true)
    public ResponseEntity<?> resetSessionVotingInfoBySessionAndVotingId(String uuid, Long id) {

        Session session = this.findByUuid(uuid);
        Voting voting = session.getVotingList().stream().filter(v -> v.getId().equals(id)).findFirst().orElse(null);

        if (voting != null) {
            List<VoteDTO> votes = voting.getParlamentarVotingList().stream().map(parlamentarVoting -> {
                VoteDTO voteDTO = new VoteDTO();
                voteDTO.setParlamentarVotingId(parlamentarVoting.getId());
                voteDTO.setParlamentarId(parlamentarVoting.getParlamentarId());
                voteDTO.setOption(EVoting.NULL.toString());

                return voteDTO;
            }).collect(Collectors.toList());

            resetVote(session, votes);
            votingService.resetResultVote(voting);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id não pode ser vazio ou nulo ao resetar votos");
    }

    private void logAndThrowException(Level level, Object[] args, HttpStatus status) throws ApiErrorException{

        logger.log(level, "Event_id = {0}, Event_description = {1}", args);

        int errorCode = (Integer) args[0];
        String errorDescription = (String) args[1];

        throw new ApiErrorException(errorCode, errorDescription, status);
    }
}