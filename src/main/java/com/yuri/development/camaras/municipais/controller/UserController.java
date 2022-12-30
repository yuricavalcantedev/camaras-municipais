package com.yuri.development.camaras.municipais.controller;

import com.yuri.development.camaras.municipais.domain.Parlamentar;
import com.yuri.development.camaras.municipais.domain.Role;
import com.yuri.development.camaras.municipais.domain.User;
import com.yuri.development.camaras.municipais.dto.UserDTOUpdatePassword;
import com.yuri.development.camaras.municipais.dto.UserLoggedDTO;
import com.yuri.development.camaras.municipais.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(value = "/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping()
    public List<User> findAll(){
        return this.userService.findAllUsersByType("U");
    }

    @GetMapping(value = "/{username}")
    @ResponseStatus(HttpStatus.OK)
    public Parlamentar findByUsername(@PathVariable String username){
        if(StringUtils.isBlank(username)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username nao pode ser nulo ou vazio");
        }

        return this.userService.findByUsername(username);
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@RequestBody @Valid User user){

        if(user == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O objeto não pode vir nulo");
        }

        return this.userService.create(user);
    }

    @PutMapping(value = "/{id}/updatepwd")
    @ResponseStatus(HttpStatus.OK)
    @CrossOrigin(origins = "http://localhost:4200")
    public UserDTOUpdatePassword updatePassword(@RequestBody @Valid UserDTOUpdatePassword userDTO){
        return this.userService.updatePassword(userDTO);
    }

    @PutMapping(value = "/{id}/update-roles")
    @ResponseStatus(HttpStatus.OK)
    public User updateRoleOfUser(@PathVariable Long id, List<Role> roles){
        if(roles == null || roles.size() == 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O utilizador precisa ser no mínimo uma responsabilidade");
        }

        return this.userService.updateRoleOfUser(id, roles);
    }

    @PutMapping(value = "/{id}/recoverypwd")
    public void updateRecoveryPassword(@PathVariable @Valid Long id){

        if(id == null || id == 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id não pode ser nulo ou ter valor 0");
        }

        this.userService.updateRecoveryPassword(id);
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @CrossOrigin(origins = "http://localhost:4200")
    public void delete(@PathVariable @Valid Long id){

        if(id == null || id == 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id não pode ser nulo ou ter valor 0");
        }

        this.userService.delete(id);
    }

    @GetMapping(value = "/recoverypwd")
    @ResponseStatus(HttpStatus.OK)
    public List<User> findAllWhoWantsToRecoveryPwd(){
        return this.userService.findAllWhoWantsToRecoverPassword();
    }
}
