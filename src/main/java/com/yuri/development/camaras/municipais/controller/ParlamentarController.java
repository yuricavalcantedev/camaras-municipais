package com.yuri.development.camaras.municipais.controller;

import com.yuri.development.camaras.municipais.domain.Parlamentar;
import com.yuri.development.camaras.municipais.domain.User;
import com.yuri.development.camaras.municipais.dto.UpdateParlamentarInfoDTO;
import com.yuri.development.camaras.municipais.dto.UpdateUserRoleDTO;
import com.yuri.development.camaras.municipais.service.ParlamenterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(value = "/parlamentares")
public class ParlamentarController {

    @Autowired
    private ParlamenterService parlamenterService;

    @GetMapping(value = "/townhalls/{id}")
    public List<Parlamentar> findAllByTownHall(@PathVariable ("id") Long id){
        return parlamenterService.findAllByTownHall(id);
    }

    @PutMapping(value = "/update-role")
    @ResponseStatus(HttpStatus.OK)
    @CrossOrigin(origins = {"http://localhost:4200", "https://camaras-municipais-frontend.vercel.app/"})
    public Parlamentar updateParlamentarToModeratorView(@RequestBody UpdateUserRoleDTO updateUserRoleDTO){

        if(updateUserRoleDTO == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Objeto não pode ser nulo");
        }
        return parlamenterService.updateUserToModeratorView(updateUserRoleDTO);
    }

    @PutMapping(value = "/update-info")
    @ResponseStatus(HttpStatus.OK)
    @CrossOrigin(origins = {"http://localhost:4200", "https://camaras-municipais-frontend.vercel.app/"})
    public User updateParlamentarInfo(@RequestBody @Valid UpdateParlamentarInfoDTO updateParlamentarInfoDTO){
        return parlamenterService.updateParlamentarInfo(updateParlamentarInfoDTO);
    }

}
