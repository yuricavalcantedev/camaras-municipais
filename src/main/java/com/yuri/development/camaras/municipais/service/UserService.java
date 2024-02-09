package com.yuri.development.camaras.municipais.service;

import com.yuri.development.camaras.municipais.domain.*;
import com.yuri.development.camaras.municipais.domain.api.ParlamentarFromAPI;
import com.yuri.development.camaras.municipais.dto.ParlamentarShortDTO;
import com.yuri.development.camaras.municipais.dto.UserDTOUpdatePassword;
import com.yuri.development.camaras.municipais.dto.UserLoggedDTO;
import com.yuri.development.camaras.municipais.enums.ERole;
import com.yuri.development.camaras.municipais.exception.RSVException;
import com.yuri.development.camaras.municipais.payload.LoginRequest;
import com.yuri.development.camaras.municipais.repository.ParlamentarPresenceRepository;
import com.yuri.development.camaras.municipais.repository.ParlamentarVotingRepository;
import com.yuri.development.camaras.municipais.repository.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.yuri.development.camaras.municipais.util.EventConstants.PARLAMENTAR_DELETED;
import static com.yuri.development.camaras.municipais.util.EventConstants.PARLAMENTAR_DELETED_DESCRIPTION;

@Service
public class UserService {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ParlamentarPresenceRepository presenceRepository;

    @Autowired
    private ParlamentarVotingRepository parlamentarVotingRepository;

    @Autowired
    private TownHallService townHallService;

    @Autowired
    private RoleService roleService;

    private final Logger logger = Logger.getLogger(UserService.class.getName());

    private static final Long TOWNHALL_ADMIN_ID = 1L;

    public List<User> findAllUsersByType(String userType){

        List<User> userList = null;
        userList = userRepository.findAllUsersByType(userType);
        userList = userList.stream()
                .map(user -> new User(user, townHallService.findById(user.getTownHall().getId())))
                .collect(Collectors.toList());

        return userList;
    }

    public User create(User user){

        try{

            if(userRepository.existsByUsername(user.getUsername())){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Já existe um usuário com esse login");
            }else{

                if(user.getRoles().get(0).getName() == ERole.ROLE_ADMIN){
                    user.setTownHall(townHallService.findById(TOWNHALL_ADMIN_ID));
                }
                user.setPassword(passwordEncoder.encode(user.getPassword()));
                user.setIsRecoveringPassword(false);
                user = userRepository.save(user);
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

        Optional<User> user = userRepository.findById(id);
        if(user.isPresent()){
            return user.get();
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Utilizador não existe");
    }


    public User save(Parlamentar parlamentar){

        return userRepository.save(parlamentar);
    }
    public void delete(Long id){

        User parlamentar = findById(id);
        List<ParlamentarVoting> parlamentarVotingList = parlamentarVotingRepository.findByParlamentarId(id);
        if(!parlamentarVotingList.isEmpty()){
            parlamentarVotingRepository.deleteAll(parlamentarVotingList);
        }

        List<ParlamentarPresence> parlamentarPresenceList = presenceRepository.findByParlamentar(parlamentar);
        if(!parlamentarPresenceList.isEmpty()){
            presenceRepository.deleteAll(parlamentarPresenceList);
        }
        userRepository.delete(parlamentar);

        logger.log(Level.INFO, "Event_id = {0}, Event_description = {1} -> {2}",
                new Object[]{PARLAMENTAR_DELETED, PARLAMENTAR_DELETED_DESCRIPTION, parlamentar.getName()});
    }

    public void updateRecoveryPassword(Long id){

        try{
            findById(id);
            userRepository.updateRecoveryPassword(true, id);
        }catch (Exception ex){

        }
    }

    public UserDTOUpdatePassword updatePassword(UserDTOUpdatePassword userDTO){

        if(!userDTO.getPassword().equals(userDTO.getRepeatedPassword())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senhas não são iguais!");
        }

        try{
            User user = findById(userDTO.getId());
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            userRepository.save(user);

        }catch (Exception e){
            logger.severe(e.getMessage());
        }

        return userDTO;
    }

    public List<User> findAllWhoWantsToRecoverPassword(){

        List <User> userList = null;

        try{
            userList = userRepository.findAllWhoWantsToRecoverPassword();
        }catch(Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        return userList;
    }

    public UserLoggedDTO findUserLoggedDTOByUsername(String username){

        UserLoggedDTO userLoggedDTO = null;
        Optional<User> optUser = userRepository.findByUsername(username);
        if(optUser.isPresent()){
            userLoggedDTO = new UserLoggedDTO(optUser.get());
        }
        return userLoggedDTO;
    }
    public Parlamentar findByUsername(String username){

        Parlamentar parlamentar = null;
        Optional<User> optParlamentar = userRepository.findByUsername(username);

        if(optParlamentar.isPresent()){
            parlamentar = (Parlamentar) optParlamentar.get();
        }

        return parlamentar;
    }

    public ParlamentarShortDTO findShortDTOByUsername(String username){

        Parlamentar parlamentar = null;
        Optional<User> optParlamentar = userRepository.findByUsername(username);

        if(optParlamentar.isPresent()){
            parlamentar = (Parlamentar) optParlamentar.get();
        }

        return new ParlamentarShortDTO(parlamentar);
    }

    public User findByUsernameSignIn(LoginRequest loginRequest) throws RSVException {

        Optional<User> optUser = userRepository.findByUsername(loginRequest.getUsername());
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
            roleList.add(roleService.findByName(ERole.ROLE_USER).orElseThrow());

            if(userRepository.existsByUsername(parlamentar.getUsername())){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Já existe um usuário com esse nome");
            }else{

                parlamentar.setRoles(roleList);
                parlamentar.setPassword(passwordEncoder.encode(parlamentar.getPassword()));
                parlamentar.setIsRecoveringPassword(false);
                parlamentar.setTownHall(townHall);
                return userRepository.save(parlamentar);
            }
        }catch (Exception e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public Parlamentar updateParlamentar(Parlamentar parlamentar, ParlamentarFromAPI parlamentarFromAPI){

        try{

            if(StringUtils.isNotBlank(parlamentarFromAPI.getTitular())){
                parlamentar.setMain(parlamentarFromAPI.getTitular().equalsIgnoreCase("sim"));
            }
            parlamentar.setActive(Boolean.parseBoolean(parlamentarFromAPI.getAtivo()));
            parlamentar.setUrlImage(parlamentarFromAPI.getFotografia());
            parlamentar = userRepository.save(parlamentar);

        }catch (Exception e){
            logger.severe(e.getMessage());
        }

        return parlamentar;
    }

    public void saveAllParlamentar(List<Parlamentar> parlamentarList){
        userRepository.saveAll(parlamentarList);
    }

    public List<Parlamentar> findAllByTownhall(TownHall townHall){
        return userRepository.findByTownHall(townHall);
    }

    public List<User> findAllByTownHallAndType(TownHall townHall, String type){
        return userRepository.findByTownHallAndType(townHall.getId(), type);
    }
}
