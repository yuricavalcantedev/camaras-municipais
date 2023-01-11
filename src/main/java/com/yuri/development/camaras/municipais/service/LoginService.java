package com.yuri.development.camaras.municipais.service;

import com.yuri.development.camaras.municipais.domain.Role;
import com.yuri.development.camaras.municipais.domain.User;
import com.yuri.development.camaras.municipais.dto.UserLoggedDTO;
import com.yuri.development.camaras.municipais.enums.ERole;
import com.yuri.development.camaras.municipais.payload.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    public UserLoggedDTO signIn(LoginRequest loginRequest){

        User user = null;
        UserLoggedDTO userLoggedDTO = null;
        Role role = null;

        try {

            user = this.userService.findByUsernameSignIn(loginRequest);
            Long roleId = this.roleService.findByUserId(user.getId());
            role = this.roleService.findById(roleId);

            if(role.getName().equals(ERole.ROLE_USER) && !this.sessionService.checkIfExistsOpenSessionToday(user.getTownHall().getId())){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não existe uma sessão aberta na data de hoje");
            }


            List<Role> roles = new ArrayList<>();
            roles.add(role);
            user.setRoles(roles);

            userLoggedDTO = new UserLoggedDTO(user);

        }catch(Exception e){
            throw e;
        }

        return userLoggedDTO;
    }
}
