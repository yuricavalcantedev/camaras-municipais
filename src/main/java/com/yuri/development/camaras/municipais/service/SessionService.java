package com.yuri.development.camaras.municipais.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuri.development.camaras.municipais.GlobalConstants;
import com.yuri.development.camaras.municipais.domain.*;
import com.yuri.development.camaras.municipais.domain.api.SessionFromAPI;
import com.yuri.development.camaras.municipais.domain.api.SubjectWrapperAPI;
import com.yuri.development.camaras.municipais.dto.*;
import com.yuri.development.camaras.municipais.enums.EPresence;
import com.yuri.development.camaras.municipais.enums.EVoting;
import com.yuri.development.camaras.municipais.exception.ApiErrorException;
import com.yuri.development.camaras.municipais.repository.SessionRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

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

    private final Logger logger = LoggerFactory.getLogger(SessionService.class.getName());

    @Transactional
    public ResponseEntity<?> create(SessionDTOCreate sessionDTOCreate){

        Session session = null;
        try{

            TownHall townHall = this.townHallService.findById(sessionDTOCreate.getTownHallId());
            Date today = Date.from(Instant.now());

            SessionFromAPI sessionFromAPI = null;
            List<Subject> subjectList = new ArrayList<>();
            List<ParlamentarPresence> parlamentarPresenceList = new ArrayList<>();

            session = this.getSessionByTownHallAndDate(townHall, today);

            if(session == null){
                String urlToGetSessionData = townHall.getApiURL().concat(GlobalConstants.SEARCH_SESSAO).replace("{id}", sessionDTOCreate.getSaplSessionId().toString());
                sessionFromAPI = restTemplate.getForObject(urlToGetSessionData, SessionFromAPI.class);

                UUID uuid = UUID.randomUUID();
                session = new Session(sessionDTOCreate.getSaplSessionId(), uuid.toString(), sessionFromAPI, townHall, today);
                session = this.sessionRepository.save(session);

                String urlToGetOrdemDiaList = townHall.getApiURL().concat(GlobalConstants.SEARCH_ORDEM_DIA_BY_SESSAO).replace("{id}", sessionDTOCreate.getSaplSessionId().toString());

                subjectList.addAll(this.retrieveSubjectListFromSAPLResponse(session, urlToGetOrdemDiaList));

                subjectList = this.subjectService.saveAll(subjectList);
                parlamentarPresenceList = this.getParlamentarPresenceList(session, townHall);
                parlamentarPresenceList = this.parlamentarPresenceService.saveAll(parlamentarPresenceList);

                session.setSubjectList(subjectList);
                session.setParlamentarPresenceList(parlamentarPresenceList);
                this.sessionRepository.save(session);

                return new ResponseEntity<>(session, HttpStatus.OK);
            }

            throw new NoSuchElementException("");

        }catch(NoSuchElementException ex){
            return new ResponseEntity<>(new ApiErrorException(1001, "Já existe uma sessão criada para o dia de hoje"), HttpStatus.BAD_REQUEST);
        }catch(RestClientException ex){
            return new ResponseEntity<>(new ApiErrorException(1001, "Erro de comunicação com o SAPL. Verifique se o SAPL está funcionando e tente novamente"), HttpStatus.INTERNAL_SERVER_ERROR);
        }catch(Exception ex){
            return new ResponseEntity<>(new ApiErrorException(1001, ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private List<Subject> retrieveSubjectListFromSAPLResponse(Session session, String url) throws JsonProcessingException {

        int pageNumber = 0;
        boolean exit = false;
        List<Subject> subjectList = new ArrayList<>();

        while(!exit){

            pageNumber = pageNumber + 1;
            String response = restTemplate.getForObject(url + "&page=" + pageNumber, String.class);
            SubjectWrapperAPI subjectWrapperAPI = new ObjectMapper().readValue(response, SubjectWrapperAPI.class);
            subjectList.addAll(subjectWrapperAPI.getSubjectList().stream().map(item -> new Subject(session, item.getContent(), item.getMateriaId())).collect(Collectors.toList()));
            exit = subjectWrapperAPI.getPagination().getNextPage() == null;
        }

        return subjectList;
    }

    public Boolean checkIfExistsOpenSessionToday(Long townHallId) {

        Date today = Date.from(Instant.now());
        TownHall townHall = this.townHallService.findById(townHallId);
        Optional<Session> optionalSession = this.sessionRepository.findByTownHallAndDate(townHall, today);
        return optionalSession.isPresent();
    }

    public SessionToParlamentarDTO findSessionTodayByTownhall(Long townHallId) {

        Date today = Date.from(Instant.now());
        TownHall townHall = this.townHallService.findById(townHallId);
        Optional<Session> optionalSession = this.sessionRepository.findByTownHallAndDate(townHall, today);
        if(optionalSession.isPresent()){
            Session session = optionalSession.get();
            String sessionSubjectURL = session.getTownHall().getApiURL() + GlobalConstants.HTML_PAGE_PAUTA_SESSAO.replace("{id}", session.getSaplId().toString());
            sessionSubjectURL = sessionSubjectURL.replace("api/", "");
            return new SessionToParlamentarDTO(session, sessionSubjectURL);
        }

        return null;
    }

    private Session getSessionByTownHallAndDate(TownHall townHall, Date date){

        return this.sessionRepository.findByTownHallAndDate(townHall, date).orElse(null);
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
    private List<ParlamentarPresence> getParlamentarPresenceList(Session session, TownHall townHall){

        List<ParlamentarPresence> parlamentarPresenceList = new ArrayList<>();
        List<Parlamentar> parlamentarList = this.parlamenterService.findAllByTownHall(townHall.getId());

        parlamentarList.forEach(parlamentar -> parlamentarPresenceList.add(new ParlamentarPresence(null, parlamentar, session, townHall.getId(), EPresence.OTHER)));
        return parlamentarPresenceList;
    }

    public List<ParlamentarPresence> getPresenceListBySession(String sessionUuid) {

        Session session = this.findByUuid(sessionUuid);

        if(session != null){
            return this.parlamentarPresenceService.findAllBySession(session);
        }

        return new ArrayList<ParlamentarPresence>();
    }

    public List<SpeakerSession> getSpeakerListBySession(String uuid){

        Session session = this.findByUuid(uuid);

        if(session != null){
            return this.speakerService.findAllBySession(session);
        }

        return new ArrayList<SpeakerSession>();
    }

    public Integer getNextSpeakerOrderBySession(String uuid){

        Integer order = this.sessionRepository.findSpeakerOrderBySession(uuid);
        return order + 1;
    }

    public SpeakerSession subscriptionInSpeakerList(String uuid, SpeakerSubscriptionDTO speakerDTO) {

        Session session = this.findByUuid(uuid);
        Parlamentar parlamentar = (Parlamentar) this.userService.findById(speakerDTO.getParlamentarId());
        TownHall townHall = this.townHallService.findById(speakerDTO.getTownhallId());

        if(session == null || parlamentar == null || townHall == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A sessão / parlamentar / câmara não existe");
        }

        Integer speakerOrder = this.getNextSpeakerOrderBySession(uuid);
        SpeakerSession speakerSession = new SpeakerSession(null, session, parlamentar.getId(), parlamentar.getName(), parlamentar.getPoliticalParty(),townHall.getId(), speakerOrder);
        speakerSession = this.speakerService.create(speakerSession);

        try{
            this.sessionRepository.updateSpeakerOrder(speakerOrder, uuid);
        }catch(Exception ex){
            //TODO voltar para perceber direito qual erro que ta acontecendo aqui
        }

        return speakerSession;
    }

    public void updatePresenceOfParlamentar(String uuid, ParlamentarPresenceDTO presenceDTO) {

        Session session = this.findByUuid(uuid);
        Parlamentar parlamentar = (Parlamentar) this.userService.findById(presenceDTO.getParlamentarId());

        if(session == null || parlamentar == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A sessão / parlamentar não existe");
        }
        this.parlamentarPresenceService.updatePresenceOfParlamentar(uuid, session, parlamentar, presenceDTO.getStatus());
    }

    public ResponseEntity<?> createVoting (String uuid, List<SubjectVotingDTO> subjectList) throws JsonProcessingException {

        Session session = this.findByUuid(uuid);
        if(this.votingService.existsOpenVoting(session)){
            return new ResponseEntity<>(new ApiErrorException(1001, "Não pode criar uma votação enquanto houver outra em andamento"), HttpStatus.BAD_REQUEST);
        }

        return this.votingService.create(session, subjectList);
    }

    public List<Subject>findAllSubjectsOfSession(String uuid, String status) {

        Session session = this.findByUuid(uuid);
        EVoting eVoting = EVoting.VOTED.name().equals(status) ? EVoting.VOTED : EVoting.NOT_VOTED;

        return this.subjectService.findAllBySessionAndStatus(session, eVoting);
    }

    public Voting closeVoting(String sessionUUID){

        Session session = this.findByUuid(sessionUUID);
        return this.votingService.closeVoting(session);
    }

    public void delete(String uuid) {

        Session session = this.findByUuid(uuid);
        if(session != null){
            this.sessionRepository.delete(session);
        }
    }

    public void deleteAll(){
        this.sessionRepository.deleteAll();
    }

    public void computeVote(String sessionUUID, VoteDTO vote) {

        Session session = this.findByUuid(sessionUUID);
        Parlamentar parlamentar = (Parlamentar) this.userService.findById(vote.getParlamentarId());

        if(session == null || parlamentar == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A sessão / parlamentar não existe");
        }

        this.votingService.computeVote(session, vote);
    }

    public SessionVotingInfoDTO findSessionVotingInfoBySessionAndVotingId(String uuid, Long id){

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
            return new SessionVotingInfoDTO(uuid, voting, parlamentarMap.get("table"), parlamentarMap.get("other"), session.getSpeakerList());
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id não pode ser vazio ou nulo");
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

    public ResponseEntity<?> getSessionVotingInfoStandardByUUID(String uuid){

        Session session = this.findByUuid(uuid);
        TownHall townHall = session.getTownHall();
        List<ParlamentarInfoStatusDTO> parlamentarInfoStatusDTOList = new ArrayList<>();


        for(ParlamentarPresence parlamentarPresence : session.getParlamentarPresenceList()){
            Parlamentar parlamentar = parlamentarPresence.getParlamentar();
            String role = null;
            Integer priority = 0;

            if(!isTableRoleFullyConfigured(townHall.getTableRoleList())){
                return new ResponseEntity<>(new ApiErrorException(4001, "The current townhall has not a table configured. \" +\n" +
                        "            \"Configure the table of roles and try it again"), HttpStatus.BAD_REQUEST);
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

    private boolean isTableRoleFullyConfigured(List<TableRole> tableRoleList){
        for(TableRole table : tableRoleList){
            if(table.getParlamentar() == null){
                return false;
            }
        }
        return true;
    }

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
}