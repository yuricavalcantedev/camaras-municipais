package com.yuri.development.camaras.municipais.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuri.development.camaras.municipais.GlobalConstants;
import com.yuri.development.camaras.municipais.domain.*;
import com.yuri.development.camaras.municipais.domain.api.DayOrderFromAPI;
import com.yuri.development.camaras.municipais.domain.api.DayOrderPayLoadFromAPI;
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

    public Session create(SessionDTOCreate sessionDTOCreate){

        Session session = null;

        try{


            TownHall townHall = this.townHallService.findById(sessionDTOCreate.getTownHallId());
            Date today = Date.from(Instant.now());

            SessionFromAPI sessionFromAPI = null;
            List<Subject> subjectList = new ArrayList<>();
            List<RoleInSession> roleInSessionList = new ArrayList<>();
            List<ParlamentarPresence> parlamentarPresenceList = new ArrayList<>();

            //session = this.getSessionByTownHallAndDate(townHall, today);
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
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ja existe uma sessao criada para o dia de hoje");
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
        return optionalSession.map(session -> new SessionToParlamentarDTO(session)).orElse(null);

    }

    private PaginationFromAPI paginationFromAPIMapper(Map<String, Object> mappedResponse){

        LinkedHashMap<String, Integer>  paginationLinkedHashMap = (LinkedHashMap) mappedResponse.get("pagination");
        Integer previousPage = paginationLinkedHashMap.get("previous_page");
        Integer nextPage = paginationLinkedHashMap.get("next_page");
        Integer totalPage = paginationLinkedHashMap.get("total_page");

        PaginationFromAPI pagination = new PaginationFromAPI(previousPage,nextPage,totalPage);
        return pagination;
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

            LinkedHashMap<String, String> item = (LinkedHashMap<String, String>) roleInSessionFromAPIList.get(i);
            String role = item.get("__str__");
            String parlamentarName = "";
            String[] splittedResult = role.split(" - ");

            if(splittedResult[1] != null){
                role = splittedResult[0].trim();
                parlamentarName = splittedResult[1].trim();
            }

            roleInSessionList.add(new RoleInSession(session, role, parlamentarName));
        }
        return roleInSessionList;
    }

    private Session getSessionByTownHallAndDate(TownHall townHall, Date date){

        Optional<Session> optSession = this.sessionRepository.findByTownHallAndDate(townHall, date);
        return optSession.orElse(null);
    }

    public Session findByUuid(String uuid){

        Session session = null;
        if(StringUtils.isBlank(uuid)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id nao pode ser vazio ou nulo");
        }
        Optional<Session> optSession = this.sessionRepository.findByUuid(uuid);
        if(optSession.isPresent()){
            session = optSession.get();
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A sessao/parlamentar/camara nao existe");
        }

        Integer speakerOrder = this.getNextSpeakerOrderBySession(uuid);
        SpeakerSession speakerSession = new SpeakerSession(null, session, parlamentar.getId(), parlamentar.getName(), parlamentar.getPoliticalParty(),townHall.getId(), speakerOrder);
        speakerSession = this.speakerService.create(speakerSession);
        this.sessionRepository.updateSpeakerOrder(speakerOrder, uuid);
        return speakerSession;
    }

    public void updatePresenceOfParlamentar(String uuid, ParlamentarPresenceDTO presenceDTO) {

        Session session = this.findByUuid(uuid);
        Parlamentar parlamentar = (Parlamentar) this.userService.findById(presenceDTO.getParlamentarId());

        if(session == null || parlamentar == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A sessao/parlamentar nao existe");
        }
        this.parlamentarPresenceService.updatePresenceOfParlamentar(uuid, session, parlamentar, presenceDTO.getStatus());
    }

    public Voting createVoting (String uuid, List<SubjectVotingDTO> subjectList){

        Session session = this.findByUuid(uuid);
        if(this.votingService.existsOpenVoting(session)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nao pode criar uma votacao enquanto houver outra em andamento");
        }

        return this.votingService.create(session, subjectList);
    }
    public List<Subject>findAllSubjectsOfSession(String uuid, String status) {

        Session session = this.findByUuid(uuid);
        EVoting eVoting = EVoting.VOTED.name().equals(status) ? EVoting.VOTED : EVoting.NOT_VOTED;

        return this.subjectService.findAllBySessionAndStatus(session, eVoting);
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A sessao/parlamentar nao existe");
        }

        this.votingService.computeVote(session, vote);
    }
}
