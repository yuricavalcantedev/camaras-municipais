package com.yuri.development.camaras.municipais.service;

import com.yuri.development.camaras.municipais.domain.Role;
import com.yuri.development.camaras.municipais.domain.User;
import com.yuri.development.camaras.municipais.dto.UserLoggedDTO;
import com.yuri.development.camaras.municipais.enums.ERole;
import com.yuri.development.camaras.municipais.exception.ApiErrorException;
import com.yuri.development.camaras.municipais.payload.LoginRequest;
import com.yuri.development.camaras.municipais.util.EventConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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

    private final Logger logger = LoggerFactory.getLogger(LoginService.class.getName());

    public ResponseEntity<?> signIn(LoginRequest loginRequest){

        User user;
        UserLoggedDTO userLoggedDTO;
        Role role;

        try {

            user = this.userService.findByUsernameSignIn(loginRequest);
            Long roleId = this.roleService.findByUserId(user.getId());
            role = this.roleService.findById(roleId);
            boolean hasOpenSessionToday = this.sessionService.checkIfExistsOpenSessionToday(user.getTownHall().getId()).hasBody();

            if(role.getName().equals(ERole.ROLE_USER) && !hasOpenSessionToday){
                logger.error("Event_id = " + EventConstants.COMMOM_USER_LOGIN_WITHOUT_OPEN_SESSION +
                                ", Event_description = " + EventConstants.COMMOM_USER_LOGIN_WITHOUT_OPEN_SESSION_DESCRIPTION,
                        user.getName());

                return new ResponseEntity<>(new ApiErrorException(EventConstants.COMMOM_USER_LOGIN_WITHOUT_OPEN_SESSION,
                        EventConstants.COMMOM_USER_LOGIN_WITHOUT_OPEN_SESSION_DESCRIPTION), HttpStatus.BAD_REQUEST);
            }

            List<Role> roles = new ArrayList<>();
            roles.add(role);
            user.setRoles(roles);

            userLoggedDTO = new UserLoggedDTO(user);

        } catch (Exception ex){
            logger.error("Event_id = " + EventConstants.LOGIN_UNSUCCESSFUL + ", Event_description = " +
                            EventConstants.LOGIN_UNSUCCESSFUL_DESCRIPTION);

            return new ResponseEntity<>(new ApiErrorException(1001, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }

        logger.info("Event_id= " + EventConstants.LOGIN_SUCCESS + ", Event_description= " +
                EventConstants.LOGIN_SUCCESS_DESCRIPTION, userLoggedDTO.getName());
        return new ResponseEntity<>(userLoggedDTO, HttpStatus.OK);
    }
}
