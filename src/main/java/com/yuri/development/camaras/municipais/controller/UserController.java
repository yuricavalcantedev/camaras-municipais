package com.yuri.development.camaras.municipais.controller;

import com.yuri.development.camaras.municipais.domain.UserTH;
import com.yuri.development.camaras.municipais.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping()
    public List<UserTH> findAll(){
        return this.userService.findAll();
    }
}
