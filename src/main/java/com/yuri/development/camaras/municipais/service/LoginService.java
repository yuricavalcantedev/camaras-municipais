package com.yuri.development.camaras.municipais.service;

import com.yuri.development.camaras.municipais.domain.Role;
import com.yuri.development.camaras.municipais.domain.User;
import com.yuri.development.camaras.municipais.dto.UserLoggedDTO;
import com.yuri.development.camaras.municipais.enums.ERole;
import com.yuri.development.camaras.municipais.exception.ApiErrorException;
import com.yuri.development.camaras.municipais.exception.RSVException;
import com.yuri.development.camaras.municipais.payload.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
@Service
public class LoginService {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private SessionService sessionService;

    public ResponseEntity<?> signIn(LoginRequest loginRequest){

        User user = null;
        UserLoggedDTO userLoggedDTO = null;
        Role role = null;

        try {

            user = this.userService.findByUsernameSignIn(loginRequest);
            Long roleId = this.roleService.findByUserId(user.getId());
            role = this.roleService.findById(roleId);

            if(role.getName().equals(ERole.ROLE_USER) && !this.sessionService.checkIfExistsOpenSessionToday(user.getTownHall().getId())){
                return new ResponseEntity<>(new ApiErrorException(1001, "Não existe uma sessão aberta na data de hoje"), HttpStatus.BAD_REQUEST);
            }


            List<Role> roles = new ArrayList<>();
            roles.add(role);
            user.setRoles(roles);

            userLoggedDTO = new UserLoggedDTO(user);

        }catch (RSVException ex){
            return new ResponseEntity<>(new ApiErrorException(1001, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
        catch(Exception ex){
            return new ResponseEntity<>(new ApiErrorException(1001, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(userLoggedDTO, HttpStatus.OK);
    }
}
