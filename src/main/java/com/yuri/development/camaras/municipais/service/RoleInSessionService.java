package com.yuri.development.camaras.municipais.service;

import com.yuri.development.camaras.municipais.domain.RoleInSession;
import com.yuri.development.camaras.municipais.domain.Session;
import com.yuri.development.camaras.municipais.repository.RoleInSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class RoleInSessionService {

    @Autowired
    private RoleInSessionRepository roleInSessionRepository;

    public List<RoleInSession> findAllBySession(Session session){
        if(session != null){
            return this.roleInSessionRepository.findAllBySession(session);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sessao nao pode ser nula");
    }

    public List<RoleInSession> saveAll(List<RoleInSession> roleInSessionList){
        if(roleInSessionList != null){
            return roleInSessionList = this.roleInSessionRepository.saveAll(roleInSessionList);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nao pode salvar uma lista nula");
    }
}
