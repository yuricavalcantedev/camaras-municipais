package com.yuri.development.camaras.municipais.controller;

import com.yuri.development.camaras.municipais.domain.Parlamentar;
import com.yuri.development.camaras.municipais.service.ParlamenterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/parlamentares")
public class ParlamentarController {

    @Autowired
    private ParlamenterService parlamenterService;

    @GetMapping(value = "/townhalls/{id}")
    public List<Parlamentar> findAllByTownHall(@PathVariable ("id") Long id){
        return this.parlamenterService.findAllByTownHall(id);
    }
}
