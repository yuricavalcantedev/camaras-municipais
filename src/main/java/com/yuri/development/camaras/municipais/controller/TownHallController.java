package com.yuri.development.camaras.municipais.controller;

import com.yuri.development.camaras.municipais.domain.TownHall;
import com.yuri.development.camaras.municipais.service.TownHallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/townhalls")
public class TownHallController {

    @Autowired
    private TownHallService townHallService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@RequestBody TownHall townHall){
        return this.townHallService.create(townHall);
    }

    @GetMapping
    public List<TownHall> findAll(){
        return this.townHallService.findAll();
    }

    @GetMapping(value = "/{id}")
    public TownHall findById(@PathVariable ("id") Long id){
        return this.townHallService.findById(id);
    }

    @GetMapping(value = "/name/{name}")
    public TownHall findByName(@PathVariable ("name") String name){
        return this.townHallService.findByName(name);
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable("id") Long id){
        this.townHallService.delete(id);
    }
}
