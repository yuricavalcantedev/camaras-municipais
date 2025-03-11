package com.yuri.development.camaras.municipais.service;

import com.yuri.development.camaras.municipais.annotation.HLogger;
import com.yuri.development.camaras.municipais.controller.request.AddSubjectRequest;
import com.yuri.development.camaras.municipais.domain.Session;
import com.yuri.development.camaras.municipais.domain.Subject;
import com.yuri.development.camaras.municipais.domain.api.EmentaAPI;
import com.yuri.development.camaras.municipais.domain.api.SubjectAPI;
import com.yuri.development.camaras.municipais.domain.api.SubjectWrapperAPI;
import com.yuri.development.camaras.municipais.enums.EVoting;
import com.yuri.development.camaras.municipais.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static com.yuri.development.camaras.municipais.util.EventConstants.*;

@Service
public class SubjectService {

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private SAPLService saplService;

    Logger logger = Logger.getLogger(SubjectService.class.getName());

    public List<Subject> saveAll (List<Subject> subjectList){
        return subjectRepository.saveAll(subjectList);
    }

    public List<Subject> findAllBySessionAndStatus(Session session, EVoting eVoting){
        return subjectRepository.findAllBySessionAndStatus(session.getUuid(), eVoting.name());
    }

    @HLogger(id = RETRIEVING_SUBJECT_LIST_FROM_SAPL_CODE, description = RETRIEVING_SUBJECT_LIST_FROM_SAPL_DESCRIPTION, isResponseEntity = false)
    public List<Subject> retrieveSubjectListFromSAPL(Session session, String url) {

        int pageNumber = 0;
        boolean exit = false;
        List<Subject> subjectList = new ArrayList<>();

        while (!exit) {
            pageNumber +=1;
            SubjectWrapperAPI subjectWrapperAPI = saplService.findSubjectListPerPage(url, pageNumber);
            for(SubjectAPI subjectAPI : subjectWrapperAPI.getSubjectList()){
                subjectList.add(new Subject(session, subjectAPI.getContent(), subjectAPI.getMateriaId(), null, subjectAPI.getOrder()));
            }
            exit = subjectWrapperAPI.getPagination().getNextPage() == null;
        }

        subjectList.sort(Comparator.comparingInt(Subject::getSubjectOrderSapl));
        subjectRepository.saveAll(subjectList);

        //I don't want to hold the main thread for this operation.
        //So, I'm going to create a new thread to get the original text url for each subject.
        //While the main thread finishes the session creation, the new thread will be getting the original text url for each subject.
        new Thread(() -> getOriginalEmentaUrlForSubjectList(session)).start();
        return subjectList;
    }

    public void addSubjectToSession(Session session, AddSubjectRequest request){

        Subject subject = new Subject(session, request.getDescription(), request.getSaplMateriaId(), request.getOriginalTextUrl(), request.getSubjectOrderSapl());
        session.getSubjectList().add(subject);
    }

    private void getOriginalEmentaUrlForSubjectList(Session session){

        long startTime = System.currentTimeMillis();
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        int threadsCreationThreshold = availableProcessors / 2;
        logger.info("Available processors: " + availableProcessors + ". ThreadsCreationThreshold: " + threadsCreationThreshold);
        ExecutorService executorService = Executors.newFixedThreadPool(threadsCreationThreshold);

        List<Subject> subjectList = session.getSubjectList();
        for(Subject subject : subjectList){
            executorService.execute(() -> {
                EmentaAPI ementaAPI = saplService.fetchOriginalEmenta(session.getTownHall().getApiURL(), subject.getSaplMateriaId());
                subject.setOriginalTextUrl(ementaAPI.getOriginalTextUrl());
                subject.setOriginalEmenta(ementaAPI.getContent());
            });
        }

        executorService.shutdown();
        try{
            executorService.awaitTermination(30L, TimeUnit.SECONDS);
            subjectRepository.saveAll(subjectList);
        }catch (InterruptedException e){
            logger.info(e.getMessage());
        }

        long duration = System.currentTimeMillis() - startTime;

        StringBuilder sBuilder = new StringBuilder();
        sBuilder.append("Method: ").append("getOriginalEmentaUrlForSubjectList()");
        sBuilder.append(" -> Event_id= ").append(FETCHING_EMENTA_URL_FOR_SUBJECT_LIST)
                .append(", Event_description= ").append(FETCHING_EMENTA_URL_FOR_SUBJECT_LIST_DESCRIPTION)
                .append(", Duration= ").append(duration).append("ms");
        logger.info(sBuilder.toString());
    }

}
