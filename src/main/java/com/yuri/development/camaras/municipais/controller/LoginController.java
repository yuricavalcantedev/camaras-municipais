package com.yuri.development.camaras.municipais.controller;

import com.yuri.development.camaras.municipais.domain.User;
import com.yuri.development.camaras.municipais.dto.UserLoggedDTO;
import com.yuri.development.camaras.municipais.payload.LoginRequest;
import com.yuri.development.camaras.municipais.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/signIn")
public class LoginController {

    @Autowired
    private LoginService loginService; //nothing

    @PostMapping
    public ResponseEntity<?> signIn(@RequestBody @Valid LoginRequest loginRequest){
        return this.loginService.signIn(loginRequest);
    }
}
