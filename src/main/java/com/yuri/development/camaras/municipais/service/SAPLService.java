package com.yuri.development.camaras.municipais.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuri.development.camaras.municipais.GlobalConstants;
import com.yuri.development.camaras.municipais.annotation.HLogger;
import com.yuri.development.camaras.municipais.domain.api.AuthorAPI;
import com.yuri.development.camaras.municipais.domain.api.EmentaAPI;
import com.yuri.development.camaras.municipais.domain.api.SessionFromAPI;
import com.yuri.development.camaras.municipais.domain.api.SubjectWrapperAPI;
import com.yuri.development.camaras.municipais.exception.ApiErrorException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.yuri.development.camaras.municipais.util.EventConstants.*;

@Service
public class SAPLService {

    private static final RestTemplate restTemplate = new RestTemplate();
    private ObjectMapper objectMapper = new ObjectMapper();
    private Logger logger = Logger.getLogger(SAPLService.class.getName());

    private String GET_EMENTA_AUTHOR_BY_SUBJECT = "materia/autoria/?materia={id}";

    @HLogger(id = SAPL_FIND_SESSION, description = SAPL_FIND_SESSION_DESCRIPTION, isResponseEntity = false)
    public SessionFromAPI findSession(String url) throws ApiErrorException{

        SessionFromAPI sessionFromAPI = restTemplate.getForObject(url, SessionFromAPI.class);

        if(sessionFromAPI == null){
            logAndThrowException(Level.SEVERE,
                    new Object[]{SAPL_SESSION_NOT_FOUND, SAPL_SESSION_NOT_FOUND_DESCRIPTION}, HttpStatus.NOT_FOUND);
        }
        return sessionFromAPI;
    }

    @HLogger(id = SAPL_FIND_SUBJECT_LIST_PER_PAGE_CODE, description = SAPL_FIND_SUBJECT_LIST_PER_PAGE_DESCRIPTION, isResponseEntity = false)
    public SubjectWrapperAPI findSubjectListPerPage(String url, Integer page){

        SubjectWrapperAPI subjectWrapperAPI = null;
        String response = restTemplate.getForObject(url + "&page=" + page, String.class);

        try{
            subjectWrapperAPI = new ObjectMapper().readValue(response, SubjectWrapperAPI.class);
        }catch (JsonProcessingException e) {
            logger.severe(e.getMessage());
        }
        return subjectWrapperAPI;
    }

    public String getSubjectAuthor(String townHallUrl, String saplMateriaId) throws JsonProcessingException {

        String urlAuthor = townHallUrl + GET_EMENTA_AUTHOR_BY_SUBJECT.replace("{id}", saplMateriaId);
        AuthorAPI authorAPI = objectMapper.readValue(restTemplate.getForObject(urlAuthor, String.class), AuthorAPI.class);
        List<String> names = authorAPI.getResults()
                .stream()
                .map(author -> Arrays.stream(author.getStr().split("-")).findFirst().get().replace("Autoria: ", "").trim())
                .collect(Collectors.toList());

        return String.join(", ", names);
    }

    public EmentaAPI fetchOriginalEmenta(String apiUrl, Integer materiaId) {

        String url = apiUrl + GlobalConstants.GET_EMENTA_BY_SUBJECT.replace("{id}", materiaId.toString());
        EmentaAPI ementaAPI = null;                ;
        try{
            ementaAPI = objectMapper.readValue(restTemplate.getForObject(url, String.class), EmentaAPI.class);
        }catch (JsonProcessingException e) {
            logger.severe(e.getMessage());
        }

        if (ementaAPI == null) {
            logger.info("EmentaAPI is null - Id da matéria inválido! Url: " + url);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id da matéria inválido!");
        }

        return ementaAPI;
    }

    private void logAndThrowException(Level level, Object[] args, HttpStatus status) throws ApiErrorException{

        logger.log(level, "Event_id = {0}, Event_description = {1}", args);

        int errorCode = (Integer) args[0];
        String errorDescription = (String) args[1];

        throw new ApiErrorException(errorCode, errorDescription, status);
    }
}
