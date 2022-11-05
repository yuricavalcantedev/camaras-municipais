package com.yuri.development.camaras.municipais.service;

import com.yuri.development.camaras.municipais.domain.User;
import com.yuri.development.camaras.municipais.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> findAll(){
        return this.userRepository.findAll();
    }

}
