package com.yuri.development.camaras.municipais.controller;

import com.yuri.development.camaras.municipais.domain.TableRole;
import com.yuri.development.camaras.municipais.domain.TownHall;
import com.yuri.development.camaras.municipais.exception.ApiErrorException;
import com.yuri.development.camaras.municipais.service.TownHallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/townhalls")
public class TownHallController {

    @Autowired
    private TownHallService townHallService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> create(@RequestBody TownHall townHall){
        return this.townHallService.create(townHall);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<TownHall> findAll(){
        return this.townHallService.findAll();
    }

    @GetMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TownHall findById(@PathVariable ("id") Long id){
        if(id == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id nao pode ser nulo");
        }
        return this.townHallService.findById(id);
    }

    @GetMapping(value = "/name/{name}")
    @ResponseStatus(HttpStatus.OK)
    public TownHall findByName(@PathVariable ("name") String name){
        return this.townHallService.findByName(name);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    @CrossOrigin(origins = {"http://localhost:4200", "https://camaras-municipais-frontend.vercel.app/"})
    public ResponseEntity<?> update (@RequestBody TownHall townHall){ return this.townHallService.update(townHall); }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @CrossOrigin(origins = {"http://localhost:4200", "https://camaras-municipais-frontend.vercel.app/"})
    public ResponseEntity<?> delete(@PathVariable("id") Long id){
        return this.townHallService.delete(id);
    }

}
