package com.yuri.development.camaras.municipais.service;

import com.yuri.development.camaras.municipais.annotation.HLogger;
import com.yuri.development.camaras.municipais.domain.Session;
import com.yuri.development.camaras.municipais.domain.Subject;
import com.yuri.development.camaras.municipais.domain.api.SubjectAPI;
import com.yuri.development.camaras.municipais.domain.api.SubjectWrapperAPI;
import com.yuri.development.camaras.municipais.dto.SubjectVotingDTO;
import com.yuri.development.camaras.municipais.enums.EVoting;
import com.yuri.development.camaras.municipais.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static com.yuri.development.camaras.municipais.util.EventConstants.RETRIEVING_SUBJECT_LIST_FROM_SAPL_CODE;
import static com.yuri.development.camaras.municipais.util.EventConstants.RETRIEVING_SUBJECT_LIST_FROM_SAPL_DESCRIPTION;

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

    public boolean existsVotedSubject(Session session, List<SubjectVotingDTO> subjectList){

        boolean result = true;
        for(int i = 0 ; i < subjectList.size(); i++){
            Optional<Subject> optSubject = subjectRepository.findBySessionAndId(session, subjectList.get(i).getId());
            if(optSubject.isPresent() && optSubject.get().getStatus().equals(EVoting.VOTED)){
                result = false;
                break;
            }
        }

        return result;
    }

    @HLogger(id = RETRIEVING_SUBJECT_LIST_FROM_SAPL_CODE, description = RETRIEVING_SUBJECT_LIST_FROM_SAPL_DESCRIPTION, isResponseEntity = false)
    public List<Subject> retrieveSubjectListFromSAPLResponse(Session session, String url) {

        int pageNumber = 0;
        boolean exit = false;
        List<Subject> subjectList = new ArrayList<>();

        int availableProcessors = Runtime.getRuntime().availableProcessors();
        int threadsCreationThreshold = availableProcessors / 2;
        logger.info("Available processors: " + availableProcessors + ". ThreadsCreationThreshold: " + threadsCreationThreshold);
        ExecutorService executorService = Executors.newFixedThreadPool(threadsCreationThreshold);

        while (!exit) {

            pageNumber +=1;
            SubjectWrapperAPI subjectWrapperAPI = saplService.findSubjectListPerPage(url, pageNumber);
            for(SubjectAPI subjectAPI : subjectWrapperAPI.getSubjectList()){
                executorService.execute(() -> {
                    getOriginalUrlTextAndAddSubjectToList(session, subjectAPI, subjectList);
                });
            }
            exit = subjectWrapperAPI.getPagination().getNextPage() == null;
        }

        executorService.shutdown();

        try{
            executorService.awaitTermination(30L, TimeUnit.SECONDS);
        }catch (InterruptedException e){
            logger.info(e.getMessage());
        }

        return subjectList;
    }

    private void getOriginalUrlTextAndAddSubjectToList(Session session, SubjectAPI subjectAPI, List<Subject> subjectList){
        String originalTextUrl = saplService.fetchOriginalEmentaTextUrl(session.getTownHall().getApiURL(), subjectAPI.getMateriaId());
        subjectList.add(new Subject(session, subjectAPI.getContent(), subjectAPI.getMateriaId(), originalTextUrl));
    }

}
