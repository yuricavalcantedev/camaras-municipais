package com.yuri.development.camaras.municipais.service;

import com.yuri.development.camaras.municipais.domain.UserTH;
import com.yuri.development.camaras.municipais.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<UserTH> findAll(){
        return this.userRepository.findAll();
    }

}
