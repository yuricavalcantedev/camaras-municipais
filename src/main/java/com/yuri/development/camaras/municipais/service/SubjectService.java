package com.yuri.development.camaras.municipais.service;

import com.yuri.development.camaras.municipais.domain.Session;
import com.yuri.development.camaras.municipais.domain.Subject;
import com.yuri.development.camaras.municipais.dto.SubjectVotingDTO;
import com.yuri.development.camaras.municipais.enums.EVoting;
import com.yuri.development.camaras.municipais.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SubjectService {

    @Autowired
    private SubjectRepository subjectRepository;

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

}
