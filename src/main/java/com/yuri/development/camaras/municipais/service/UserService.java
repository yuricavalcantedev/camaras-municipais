package com.yuri.development.camaras.municipais.service;

import com.yuri.development.camaras.municipais.domain.Parlamentar;
import com.yuri.development.camaras.municipais.domain.Role;
import com.yuri.development.camaras.municipais.domain.TownHall;
import com.yuri.development.camaras.municipais.domain.User;
import com.yuri.development.camaras.municipais.domain.api.ParlamentarFromAPI;
import com.yuri.development.camaras.municipais.dto.ParlamentarShortDTO;
import com.yuri.development.camaras.municipais.dto.UserDTOUpdatePassword;
import com.yuri.development.camaras.municipais.dto.UserLoggedDTO;
import com.yuri.development.camaras.municipais.enums.ERole;
import com.yuri.development.camaras.municipais.exception.RSVException;
import com.yuri.development.camaras.municipais.payload.LoginRequest;
import com.yuri.development.camaras.municipais.repository.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TownHallService townHallService;

    @Autowired
    private RoleService roleService;

    private Logger logger = (Logger) LoggerFactory.getLogger(UserService.class);

    private static final Long TOWNHALL_ADMIN_ID = 1L;

    public List<User> findAllUsersByType(String userType){

        List<User> userList = null;
        userList = this.userRepository.findAllUsersByType(userType);
        userList = userList.stream()
                .map(user -> new User(user, this.townHallService.findById(user.getTownHall().getId())))
                .collect(Collectors.toList());

        return userList;
    }

    public User create(User user){

        try{

            if(this.userRepository.existsByUsername(user.getUsername())){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Já existe um usuário com esse login");
            }else{

                if(user.getRoles().get(0).getName() == ERole.ROLE_ADMIN){
                    user.setTownHall(this.townHallService.findById(TOWNHALL_ADMIN_ID));
                }
                user.setPassword(passwordEncoder.encode(user.getPassword()));
                user.setIsRecoveringPassword(false);
                user = this.userRepository.save(user);
            }
        }catch (Exception e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        return user;
    }

    public User findById(Long id){

        if(id == null || id == 0){
            return null;
        }

        Optional<User> user = this.userRepository.findById(id);
        if(user.isPresent()){
            return user.get();
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Utilizador não existe");
    }


    public User save(Parlamentar parlamentar){

        return this.userRepository.save(parlamentar);
    }
    public void delete(Long id){

        this.findById(id);
        this.userRepository.deleteById(id);
    }

    public void updateRecoveryPassword(Long id){

        try{
            this.findById(id);
            this.userRepository.updateRecoveryPassword(true, id);
        }catch (Exception ex){

        }
    }

    public UserDTOUpdatePassword updatePassword(UserDTOUpdatePassword userDTO){

        if(!userDTO.getPassword().equals(userDTO.getRepeatedPassword())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senhas não são iguais!");
        }

        try{
            User user = this.findById(userDTO.getId());
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            this.userRepository.save(user);

        }catch (Exception e){
            this.logger.error(e.getMessage());
        }

        return userDTO;
    }

    public List<User> findAllWhoWantsToRecoverPassword(){

        List <User> userList = null;

        try{
            userList = this.userRepository.findAllWhoWantsToRecoverPassword();
        }catch(Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        return userList;
    }

    public UserLoggedDTO findUserLoggedDTOByUsername(String username){

        UserLoggedDTO userLoggedDTO = null;
        Optional<User> optUser = this.userRepository.findByUsername(username);
        if(optUser.isPresent()){
            userLoggedDTO = new UserLoggedDTO(optUser.get());
        }
        return userLoggedDTO;
    }
    public Parlamentar findByUsername(String username){

        Parlamentar parlamentar = null;
        Optional<User> optParlamentar = this.userRepository.findByUsername(username);

        if(optParlamentar.isPresent()){
            parlamentar = (Parlamentar) optParlamentar.get();
        }

        return parlamentar;
    }

    public ParlamentarShortDTO findShortDTOByUsername(String username){

        Parlamentar parlamentar = null;
        Optional<User> optParlamentar = this.userRepository.findByUsername(username);

        if(optParlamentar.isPresent()){
            parlamentar = (Parlamentar) optParlamentar.get();
        }

        return new ParlamentarShortDTO(parlamentar);
    }

    public User findByUsernameSignIn(LoginRequest loginRequest) throws RSVException {

        Optional<User> optUser = this.userRepository.findByUsername(loginRequest.getUsername());
        if(optUser.isPresent()){
            if(passwordEncoder.matches(loginRequest.getPassword(), optUser.get().getPassword())){
                return optUser.get();
            }else{
                throw new RSVException("Senha incorreta");
            }
        }else{
            throw new RSVException("Usuário não encontrado");
        }
    }

    public Parlamentar createParlamentar(TownHall townHall, Parlamentar parlamentar){

        try{

            List<Role> roleList = new ArrayList<>();
            roleList.add(this.roleService.findByName(ERole.ROLE_USER).orElseThrow());

            if(this.userRepository.existsByUsername(parlamentar.getUsername())){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Já existe um usuário com esse nome");
            }else{

                parlamentar.setRoles(roleList);
                parlamentar.setPassword(passwordEncoder.encode(parlamentar.getPassword()));
                parlamentar.setIsRecoveringPassword(false);
                parlamentar.setTownHall(townHall);
                parlamentar = this.userRepository.save(parlamentar);
            }
        }catch (Exception e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        return parlamentar;
    }

    public Parlamentar updateParlamentar(Parlamentar parlamentar, ParlamentarFromAPI parlamentarFromAPI){

        try{

            if(StringUtils.isNotBlank(parlamentarFromAPI.getTitular())){
                parlamentar.setMain(parlamentarFromAPI.getTitular().equalsIgnoreCase("sim"));
            }
            parlamentar.setActive(Boolean.parseBoolean(parlamentarFromAPI.getAtivo()));
            parlamentar.setUrlImage(parlamentarFromAPI.getFotografia());
            parlamentar = this.userRepository.save(parlamentar);

        }catch (Exception e){
            this.logger.error(e.getMessage());
        }

        return parlamentar;
    }

    public List<Parlamentar> saveAllParlamentar(List<Parlamentar> parlamentarList){
        return this.userRepository.saveAll(parlamentarList);
    }

    public List<Parlamentar> findAllByTownhall(TownHall townHall){
        return this.userRepository.findByTownHall(townHall);
    }

    public List<User> findAllByTownHallAndType(TownHall townHall, String type){
        return this.userRepository.findByTownHallAndType(townHall.getId(), type);
    }
}
