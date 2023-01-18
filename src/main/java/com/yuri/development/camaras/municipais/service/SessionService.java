package com.yuri.development.camaras.municipais.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuri.development.camaras.municipais.GlobalConstants;
import com.yuri.development.camaras.municipais.domain.*;
import com.yuri.development.camaras.municipais.domain.api.PaginationFromAPI;
import com.yuri.development.camaras.municipais.domain.api.SessionFromAPI;
import com.yuri.development.camaras.municipais.dto.*;
import com.yuri.development.camaras.municipais.enums.EPresence;
import com.yuri.development.camaras.municipais.enums.EVoting;
import com.yuri.development.camaras.municipais.repository.SessionRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    @Autowired
    private RoleInSessionService roleInSessionService;

    private static RestTemplate restTemplate = new RestTemplate();

    private Logger logger = Logger.getLogger(SessionService.class.getName());

    public Session create(SessionDTOCreate sessionDTOCreate){

        Session session = null;

        try{


            TownHall townHall = this.townHallService.findById(sessionDTOCreate.getTownHallId());
            Date today = Date.from(Instant.now());

            SessionFromAPI sessionFromAPI = null;
            List<Subject> subjectList = new ArrayList<>();
            List<RoleInSession> roleInSessionList = new ArrayList<>();
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

                String urlToGetRoleInSessionList = townHall.getApiURL().concat(GlobalConstants.SEARCH_INTEGRANTE_MESA_BY_SESSAO).replace("{id}", sessionDTOCreate.getSaplSessionId().toString());
                roleInSessionList = this.retrieveRoleInSessionListFromSAPLResponse(session, urlToGetRoleInSessionList);
                roleInSessionList = this.roleInSessionService.saveAll(roleInSessionList);

                subjectList = this.subjectService.saveAll(subjectList);
                parlamentarPresenceList = this.getParlamentarPresenceList(session, townHall);
                parlamentarPresenceList = this.parlamentarPresenceService.saveAll(parlamentarPresenceList);

                session.setSubjectList(subjectList);
                session.setParlamentarPresenceList(parlamentarPresenceList);
                session.setRoleInSessionList(roleInSessionList);
                this.sessionRepository.save(session);

            }else{
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Já existe uma sessão criada para o dia de hoje");
            }

            return session;
        }catch(ResponseStatusException e){
            throw e;

        }catch(Exception ex){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    private List<Subject> retrieveSubjectListFromSAPLResponse(Session session, String url) throws JsonProcessingException {

        int pageNumber = 0;
        boolean exit = false;
        List<Subject> subjectList = new ArrayList<>();

        while(!exit){

            pageNumber = pageNumber + 1;
            String jsonString = restTemplate.getForObject(url + "&page=" + pageNumber, String.class);
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> mappedResponse = mapper.readValue(jsonString, Map.class);
            PaginationFromAPI pagination = this.paginationFromAPIMapper(mappedResponse);
            subjectList.addAll(this.subjectListFromAPIMapper(session, mappedResponse));
            exit = pagination.getNextPage() == null;
        }

        return subjectList;
    }

    private List<RoleInSession> retrieveRoleInSessionListFromSAPLResponse(Session session, String url) throws JsonProcessingException {
        int pageNumber = 0;
        boolean exit = false;
        List<RoleInSession> roleInSessionList = new ArrayList<>();

        while(!exit){

            pageNumber = pageNumber + 1;
            String jsonString = restTemplate.getForObject(url + "&page=" + pageNumber, String.class);
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> mappedResponse = mapper.readValue(jsonString, Map.class);
            PaginationFromAPI pagination = this.paginationFromAPIMapper(mappedResponse);
            roleInSessionList.addAll(this.roleInSessionListFromAPIMapper(session, mappedResponse));
            exit = pagination.getNextPage() == null;

        }

        return roleInSessionList;
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

    private PaginationFromAPI paginationFromAPIMapper(Map<String, Object> mappedResponse){

        LinkedHashMap<String, Integer>  paginationLinkedHashMap = (LinkedHashMap) mappedResponse.get("pagination");
        Integer previousPage = paginationLinkedHashMap.get("previous_page");
        Integer nextPage = paginationLinkedHashMap.get("next_page");
        Integer totalPage = paginationLinkedHashMap.get("total_page");

        return new PaginationFromAPI(previousPage,nextPage,totalPage);
    }

    private List<Subject> subjectListFromAPIMapper(Session session, Map<String, Object> mappedResponse){

        List<Subject> subjectList = new ArrayList<>();
        List<Object> dayOrderFromAPIList = (ArrayList<Object>) mappedResponse.get("results");
        for(int i = 0; i < dayOrderFromAPIList.size(); i++){
            LinkedHashMap<String, String> item = (LinkedHashMap<String, String>) dayOrderFromAPIList.get(i);
            subjectList.add(new Subject(session, item.get("__str__")));
        }
        return subjectList;
    }

    private List<RoleInSession> roleInSessionListFromAPIMapper(Session session, Map<String, Object> mappedResponse){

        List<RoleInSession> roleInSessionList = new ArrayList<>();
        List<Object> roleInSessionFromAPIList = (ArrayList<Object>) mappedResponse.get("results");
        for(int i = 0; i < roleInSessionFromAPIList.size(); i++){

            try{
                LinkedHashMap<String, Object> item = (LinkedHashMap<String, Object>) roleInSessionFromAPIList.get(i);
                String role = (String) item.get("__str__");
                String parlamentarName = "";
                Integer priority = (Integer) item.get("cargo");
                String[] splittedResult = role.split(" - ");

                if(splittedResult[1] != null){
                    role = splittedResult[0].trim();
                    parlamentarName = splittedResult[1].trim();
                }
                roleInSessionList.add(new RoleInSession(session, role, parlamentarName, priority));
            }catch (Exception ex){
                this.logger.log(Level.SEVERE, ex.getMessage());
            }
        }

        Comparator<RoleInSession> compareByPriority = (RoleInSession o1, RoleInSession o2) -> o1.getPriority().compareTo( o2.getPriority());
        Collections.sort(roleInSessionList, compareByPriority);

        return roleInSessionList;
    }

    private Session getSessionByTownHallAndDate(TownHall townHall, Date date){

        Optional<Session> optSession = this.sessionRepository.findByTownHallAndDate(townHall, date);
        return optSession.orElse(null);
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

    public Voting createVoting (String uuid, List<SubjectVotingDTO> subjectList){

        Session session = this.findByUuid(uuid);
        if(this.votingService.existsOpenVoting(session)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não pode criar uma votação enquanto houver outra em andamento");
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

    public SessionVotingInfoDTO findSessionVotingInfoByUUID(String uuid){

        Session session = this.findByUuid(uuid);
        Voting voting = session.getVotingList().stream().filter(v -> v.getStatus().equals(EVoting.VOTING)).findFirst().orElse(null);
        List<ParlamentarInfoStatusDTO> parlamentarInfoStatusDTOList = new ArrayList<>();

        parlamentarInfoStatusDTOList.addAll(voting.getParlamentarVotingList().stream().map(parlamentarVoting -> {

            Parlamentar parlamentar = (Parlamentar) this.userService.findById(parlamentarVoting.getParlamentarId());
            Optional<ParlamentarPresence> optionalParlamentarPresence = this.parlamentarPresenceService.findParlamentarPresenceBySessionIdAndParlamentar(session, parlamentar);
            String role = null;
            EPresence ePresence = optionalParlamentarPresence.isPresent() ? optionalParlamentarPresence.get().getStatus() : EPresence.OTHER;
            Integer priority = 0;

            for(int i = 0; i < session.getRoleInSessionList().size(); i++){
                if (session.getRoleInSessionList().get(i).getParlamentarName().equals(parlamentar.getName())) {
                    role = session.getRoleInSessionList().get(i).getRole();
                    priority = session.getRoleInSessionList().get(i).getPriority();
                }
            }
            return new ParlamentarInfoStatusDTO(parlamentar, parlamentarVoting.getResult().toString(), role, ePresence, priority);

        }).collect(Collectors.toList()));

        HashMap<String, List<ParlamentarInfoStatusDTO>> parlamentarMap = this.splitParlamentarVotingList(session, parlamentarInfoStatusDTOList);
        return new SessionVotingInfoDTO(uuid, voting, parlamentarMap.get("table"), parlamentarMap.get("other"), session.getSpeakerList());
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

    public SessionVotingInfoDTO getSessionVotingInfoStandardByUUID(String uuid){

        Session session = this.findByUuid(uuid);
        List<ParlamentarInfoStatusDTO> parlamentarInfoStatusDTOList = new ArrayList<>();

        parlamentarInfoStatusDTOList.addAll(session.getParlamentarPresenceList().stream().map(parlamentarPresence -> {

            Parlamentar parlamentar = parlamentarPresence.getParlamentar();
            String role = null;
            Integer priority = 0;

            for(int i = 0; i < session.getRoleInSessionList().size(); i++){
                if (session.getRoleInSessionList().get(i).getParlamentarName().equals(parlamentar.getName())) {
                    role = session.getRoleInSessionList().get(i).getRole();
                    priority = session.getRoleInSessionList().get(i).getPriority();
                }
            }
            return new ParlamentarInfoStatusDTO(parlamentar, "NULL", role,  parlamentarPresence.getStatus(), priority);

        }).collect(Collectors.toList()));

        HashMap<String, List<ParlamentarInfoStatusDTO>> parlamentarMap = this.splitParlamentarVotingList(session, parlamentarInfoStatusDTOList);
        return new SessionVotingInfoDTO(uuid, null, parlamentarMap.get("table"), parlamentarMap.get("other"), session.getSpeakerList());
    }

    public void updatePresenceOfParlamentarList(String uuid, List<Long> parlamentarListId) {

        Session session = this.findByUuid(uuid);
        for(ParlamentarPresence pPresence : session.getParlamentarPresenceList()){
            for(int i = 0; i < parlamentarListId.size(); i++){
                if(pPresence.getId().equals(parlamentarListId.get(i))){
                    pPresence.setStatus(EPresence.PRESENCE);
                }
            }
        }
        this.parlamentarPresenceService.saveAll(session.getParlamentarPresenceList());
    }
}
