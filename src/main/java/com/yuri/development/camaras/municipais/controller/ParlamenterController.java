package com.yuri.development.camaras.municipais.controller;

import com.yuri.development.camaras.municipais.domain.Parlamentar;
import com.yuri.development.camaras.municipais.service.ParlamenterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ParlamenterController {

    @Autowired
    private ParlamenterService parlamenterService;

    @GetMapping("/parlamentares")
    public List<Parlamentar> getAllParlamentar(){
        return parlamenterService.getAllParlamentar();
    }
}
