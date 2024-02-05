package com.yuri.development.camaras.municipais.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yuri.development.camaras.municipais.domain.Control;
import com.yuri.development.camaras.municipais.dto.ControlDTO;
import com.yuri.development.camaras.municipais.enums.EControlType;
import com.yuri.development.camaras.municipais.service.ControlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(value = "/control")
public class ControlController {

    @Autowired
    private ControlService controlService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @CrossOrigin(origins = {"http://localhost:4200", "https://camaras-municipais-frontend.vercel.app/"})
    public ResponseEntity<?> create(@RequestBody @Valid ControlDTO controlDTO) throws JsonProcessingException {
        return this.controlService.create(controlDTO);
    }

    @GetMapping(value = "/{type}/{townHallId}")
    @ResponseStatus(HttpStatus.OK)
    @CrossOrigin(origins = {"http://localhost:4200", "https://camaras-municipais-frontend.vercel.app/"})
    public List<Control> findByType(@PathVariable EControlType type, @PathVariable String townHallId) {
        if(type == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Control type nao pode ser nulo ou vazio");
        }
        return this.controlService.findByTypeAndTownHallId(type, townHallId);
    }

    @DeleteMapping(value = "/{controlId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @CrossOrigin(origins = {"http://localhost:4200", "https://camaras-municipais-frontend.vercel.app/"})
    public ResponseEntity<?> deleteById(@PathVariable Long controlId) {
        if(controlId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Control Id nao pode ser nulo");
        }
        this.controlService.deleteById(controlId);
        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
    }
}
