package com.yuri.development.camaras.municipais.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuri.development.camaras.municipais.GlobalConstants;
import com.yuri.development.camaras.municipais.annotation.HLogger;
import com.yuri.development.camaras.municipais.domain.*;
import com.yuri.development.camaras.municipais.domain.api.EmentaAPI;
import com.yuri.development.camaras.municipais.domain.api.SessionFromAPI;
import com.yuri.development.camaras.municipais.domain.api.SubjectWrapperAPI;
import com.yuri.development.camaras.municipais.dto.*;
import com.yuri.development.camaras.municipais.enums.EPresence;
import com.yuri.development.camaras.municipais.enums.EVoting;
import com.yuri.development.camaras.municipais.exception.ApiErrorException;
import com.yuri.development.camaras.municipais.exception.RSVException;
import com.yuri.development.camaras.municipais.repository.SessionRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
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
    private ParlamenterService parlamenterService;

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

    private static final RestTemplate restTemplate = new RestTemplate();

    Logger logger = Logger.getLogger(SessionService.class.getName());

    @Transactional
    public ResponseEntity<?> create(SessionDTOCreate sessionDTOCreate){

        Session session;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try{

            TownHall townHall = townHallService.findById(sessionDTOCreate.getTownHallId());
            Date today = Date.from(Instant.now());

            SessionFromAPI sessionFromAPI;
            List<Subject> subjectList = new ArrayList<>();
            List<ParlamentarPresence> parlamentarPresenceList;

            session = getSessionByTownHallAndDate(townHall, today);

            if(session == null){
                String urlToGetSessionData = townHall.getApiURL().concat(GlobalConstants.SEARCH_SESSAO).replace("{id}",
                        sessionDTOCreate.getSaplSessionId().toString());

                sessionFromAPI = restTemplate.getForObject(urlToGetSessionData, SessionFromAPI.class);
                if(sessionFromAPI == null){
                    logger.log(Level.WARNING, "Event_id = {0}, Event_description = {1}, Duration(ms) = {2}",
                            new Object []{SAPL_SESSION_NOT_FOUND, SAPL_SESSION_NOT_FOUND_DESCRIPTION, stopWatch.getTotalTimeMillis()});

                    return new ResponseEntity<>(new ApiErrorException(SAPL_SESSION_NOT_FOUND,
                            SAPL_SESSION_NOT_FOUND_DESCRIPTION), HttpStatus.BAD_REQUEST);
                }

                logger.log(Level.INFO, "Event_id = {0}, Event_description = {1}, Duration(ms)= {2}",
                        new Object[]{SAPL_SESSION_FOUND, SAPL_SESSION_FOUND_DESCRIPTION, stopWatch.getTotalTimeMillis()});

                UUID uuid = UUID.randomUUID();
                session = new Session(sessionDTOCreate.getSaplSessionId(), uuid.toString(), sessionFromAPI, townHall, today);
                session = sessionRepository.save(session);

                String urlToGetOrdemDiaList = townHall.getApiURL().concat(GlobalConstants.SEARCH_ORDEM_DIA_BY_SESSAO).
                        replace("{id}", sessionDTOCreate.getSaplSessionId().toString());

                subjectList.addAll(retrieveSubjectListFromSAPLResponse(session, urlToGetOrdemDiaList));

                subjectList = subjectService.saveAll(subjectList);
                parlamentarPresenceList = getParlamentarPresenceList(session, townHall);
                parlamentarPresenceList = parlamentarPresenceService.saveAll(parlamentarPresenceList);

                session.setSubjectList(subjectList);
                session.setParlamentarPresenceList(parlamentarPresenceList);
                sessionRepository.save(session);

                stopWatch.stop();

                String createSessionDescription = CREATE_SESSION_DESCRIPTION + townHall.getName();
                logger.log(Level.INFO, "Event_id = {0}, Event_description = {1}, Duration(ms) = {2}",
                        new Object[] {CREATE_SESSION, createSessionDescription, stopWatch.getTotalTimeMillis()});
                return new ResponseEntity<>(session, HttpStatus.CREATED);
            }

            throw new RSVException(TOWNHALL_HAS_SESSION_ALREADY_DESCRIPTION);

        }catch(NoSuchElementException ex){
            logger.log(Level.SEVERE, "Event_id = {0}, Event_description = {1}",
                    new Object[]{TOWNHALL_NOT_FOUND, TOWNHALL_NOT_FOUND_DESCRIPTION});
            return new ResponseEntity<>(new ApiErrorException(TOWNHALL_NOT_FOUND,
                    TOWNHALL_NOT_FOUND_DESCRIPTION), HttpStatus.BAD_REQUEST);
        }catch(RSVException ex){
            logger.log(Level.SEVERE,"Event_id = {0}, Event_description = {1}",
                    new Object[]{TOWNHALL_HAS_SESSION_ALREADY, TOWNHALL_HAS_SESSION_ALREADY_DESCRIPTION});
            return new ResponseEntity<>(new ApiErrorException(TOWNHALL_HAS_SESSION_ALREADY,
                    TOWNHALL_HAS_SESSION_ALREADY_DESCRIPTION), HttpStatus.BAD_REQUEST);
        }catch(RestClientException ex){
            logger.log(Level.SEVERE,"Event_id = {0}, Event_description = {1}",
                    new Object[]{ERROR_COMMUNICATION_SAPL, ERROR_COMMUNICATION_SAPL_DESCRIPTION});
            return new ResponseEntity<>(new ApiErrorException(ERROR_COMMUNICATION_SAPL,
                    ERROR_COMMUNICATION_SAPL_DESCRIPTION), HttpStatus.INTERNAL_SERVER_ERROR);
        }catch(Exception ex){
            logger.log(Level.SEVERE, "Event_id = {0}, Event_description = {1}",
                    new Object[]{ERROR_UNEXPECTED_EXCEPTION, ex.getMessage()});
            return new ResponseEntity<>(new ApiErrorException(ERROR_UNEXPECTED_EXCEPTION,
                    ERROR_UNEXPECTED_EXCEPTION_DESCRIPTION), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @HLogger(id = CREATE_VOTING_FOR_SESSION, description = CREATE_VOTING_FOR_SESSION_DESCRIPTION, hasUUID = true)
    public ResponseEntity<?> createVoting (String uuid, List<SubjectVotingDTO> subjectList) throws JsonProcessingException {

        Session session = findByUuid(uuid);
        if(session == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, SAPL_SESSION_NOT_FOUND_DESCRIPTION);
        }
        if(votingService.existsOpenVoting(session)){
            return new ResponseEntity<>(new ApiErrorException(1001, "Não pode criar uma votação enquanto houver outra em andamento"),
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
                        TOWNHALL_HAS_NO_TABLE_ROLE_DEFINED_DESCRIPTION), HttpStatus.BAD_REQUEST);
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
        TownHall townHall = this.townHallService.findById(townHallId);
        Optional<Session> optionalSession = this.sessionRepository.findByTownHallAndDate(townHall, today);
        return new ResponseEntity<>(optionalSession.isPresent(), HttpStatus.OK);
    }

    public ResponseEntity<?> findSessionTodayByTownhall(Long townHallId) {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Date today = Date.from(Instant.now());
        TownHall townHall = this.townHallService.findById(townHallId);
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
            session.getSubjectList().sort(Comparator.comparing(Subject::getId));
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
    public ResponseEntity<?> subscriptionInSpeakerList(String uuid, SpeakerSubscriptionDTO speakerDTO) {

        Session session = findByUuid(uuid);
        Parlamentar parlamentar = (Parlamentar) userService.findById(speakerDTO.getParlamentarId());
        TownHall townHall = this.townHallService.findById(speakerDTO.getTownhallId());

        if(session == null || parlamentar == null || townHall == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A sessão / parlamentar / câmara não existe");
        }

        Integer speakerOrder = getNextSpeakerOrderBySession(uuid);
        SpeakerSession speakerSession = new SpeakerSession(null, session, parlamentar.getId(), parlamentar.getName(),
                parlamentar.getPoliticalParty(),townHall.getId(), speakerOrder);
        speakerSession = speakerService.create(speakerSession);

        try{
            this.sessionRepository.updateSpeakerOrder(speakerOrder, uuid);
        }catch(Exception ex){
            logger.log(Level.SEVERE, "Event_id = {0}, Event_description = {1}",
                    new Object[]{ERROR_UNEXPECTED_EXCEPTION, ERROR_UNEXPECTED_EXCEPTION_DESCRIPTION});
        }

        return new ResponseEntity<>(speakerSession, HttpStatus.OK);
    }

    public void updatePresenceOfParlamentar(String uuid, ParlamentarPresenceDTO presenceDTO) {

        Session session = findByUuid(uuid);
        Parlamentar parlamentar = (Parlamentar) this.userService.findById(presenceDTO.getParlamentarId());

        if(session == null || parlamentar == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A sessão / parlamentar não existe");
        }
        parlamentarPresenceService.updatePresenceOfParlamentar(uuid, session, parlamentar, presenceDTO.getStatus());
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

    public void resetResulVote(Voting voting) {
        votingService.resetResultVote(voting);
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

            HashMap<String, List<ParlamentarInfoStatusDTO>> parlamentarMap = this.splitParlamentarVotingList(session, parlamentarInfoStatusDTOList);
            return new ResponseEntity<>(new SessionVotingInfoDTO(uuid, voting, parlamentarMap.get("table"), parlamentarMap.get("other"), session.getSpeakerList()), HttpStatus.OK) ;
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id não pode ser vazio ou nulo");
    }

    private Integer getNextSpeakerOrderBySession(String uuid){

        Integer order = this.sessionRepository.findSpeakerOrderBySession(uuid);
        return order + 1;
    }

    @HLogger(id = GET_PARLAMENTAR_PRESENCE_LIST_FOR_SESSION, description = GET_PARLAMENTAR_PRESENCE_LIST_FOR_SESSION_DESCRIPTION, hasUUID = true)
    private List<ParlamentarPresence> getParlamentarPresenceList(Session session, TownHall townHall) {

        List<ParlamentarPresence> parlamentarPresenceList = new ArrayList<>();
        List<Parlamentar> parlamentarList = this.parlamenterService.findAllByTownHall(townHall.getId());

        parlamentarList.forEach(parlamentar -> parlamentarPresenceList.add(new ParlamentarPresence(null, parlamentar, session, townHall.getId(), EPresence.OTHER)));
        return parlamentarPresenceList;
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

    private Session getSessionByTownHallAndDate(TownHall townHall, Date date){

        return this.sessionRepository.findByTownHallAndDate(townHall, date).orElse(null);
    }

    private boolean isTableRoleFullyConfigured(List<TableRole> tableRoleList){
        for(TableRole table : tableRoleList){
            if(table.getParlamentar() == null){
                return false;
            }
        }
        return true;
    }

    private List<Subject> retrieveSubjectListFromSAPLResponse(Session session, String url) throws JsonProcessingException {

        int pageNumber = 0;
        boolean exit = false;
        List<Subject> subjectList = new ArrayList<>();

        while (!exit) {

            pageNumber = pageNumber + 1;
            String response = restTemplate.getForObject(url + "&page=" + pageNumber, String.class);
            SubjectWrapperAPI subjectWrapperAPI = new ObjectMapper().readValue(response, SubjectWrapperAPI.class);
            subjectList.addAll(subjectWrapperAPI.getSubjectList().stream().map(item -> {

                try {
                    String originalTextUrl = this.getOriginalTextUrlFromSAPL(session.getTownHall(), item.getMateriaId());
                    return new Subject(session, item.getContent(), item.getMateriaId(), originalTextUrl);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList()));
            exit = subjectWrapperAPI.getPagination().getNextPage() == null;
        }

        return subjectList;
    }

    private String getOriginalTextUrlFromSAPL(TownHall townHall, Integer materiaId) throws JsonProcessingException {

        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();
        String url = townHall.getApiURL() + GlobalConstants.GET_EMENTA_BY_SUBJECT.replace("{id}", materiaId.toString());
        EmentaAPI ementaAPI = objectMapper.readValue(restTemplate.getForObject(url, String.class), EmentaAPI.class);
        if (ementaAPI == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id da matéria inválido!");
        }

        return ementaAPI.getOriginalTextUrl();
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

            this.resetVote(session, votes);
            this.resetResulVote(voting);
        }



        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id não pode ser vazio ou nulo ao resetar votos");
    }
}