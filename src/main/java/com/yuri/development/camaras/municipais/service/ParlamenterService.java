package com.yuri.development.camaras.municipais.service;

import com.yuri.development.camaras.municipais.GlobalConstants;
import com.yuri.development.camaras.municipais.domain.Parlamentar;
import com.yuri.development.camaras.municipais.domain.Role;
import com.yuri.development.camaras.municipais.domain.TownHall;
import com.yuri.development.camaras.municipais.domain.User;
import com.yuri.development.camaras.municipais.domain.api.ParlamentarFromAPI;
import com.yuri.development.camaras.municipais.dto.UpdateParlamentarInfoDTO;
import com.yuri.development.camaras.municipais.dto.UpdateUserRoleDTO;
import com.yuri.development.camaras.municipais.enums.ERole;
import com.yuri.development.camaras.municipais.repository.TownHallRepository;
import com.yuri.development.camaras.municipais.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.yuri.development.camaras.municipais.util.EventConstants.SAPL_PARLAMENTAR_LIST;
import static com.yuri.development.camaras.municipais.util.EventConstants.SAPL_PARLAMENTAR_LIST_DESCRIPTION;

@Service
public class ParlamenterService {

    @Autowired
    private TownHallRepository townHallRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TownHallService townHallService;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    Logger logger = Logger.getLogger(ParlamenterService.class.getName());

    public List<Parlamentar> findAllByTownHall(Long id){

        List<Parlamentar> parlamentarList = new ArrayList<>();
        Optional< TownHall> optionalTownHall = townHallRepository.findById(id);
        optionalTownHall.ifPresent(townHall -> parlamentarList.addAll(this.findAll(townHall)));

        return parlamentarList;
    }

    public Parlamentar updateUserToModeratorView(UpdateUserRoleDTO updateUserRoleDTO) {

        List<Role> basicRoleList = new ArrayList<>();
        List<Role> moderatorRoleList = new ArrayList<>();
        Optional<Role> optBasicUserRole = roleService.findByName(ERole.ROLE_USER);
        Optional<Role> optModeratorViewRole = roleService.findByName(ERole.ROLE_MODERATOR_VIEW);

        if(optBasicUserRole.isEmpty()){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro inesperado, contacte o administrador");
        }

        if(optModeratorViewRole.isEmpty()){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro inesperado, contacte o administrador");
        }

        basicRoleList.add(optBasicUserRole.get());

        Parlamentar parlamentar = (Parlamentar) userService.findById(updateUserRoleDTO.getParlamentarId());
        TownHall townHall = townHallService.findTownhallById(updateUserRoleDTO.getTownHallId());
        List<Parlamentar> parlamentarList = userService.findAllByTownhall(townHall);
        for(User parlamentarAux : parlamentarList){
            parlamentarAux.setRoles(basicRoleList);
        }

        userService.saveAllParlamentar(parlamentarList);

        try{
            moderatorRoleList.add(optModeratorViewRole.get());
            parlamentar.setRoles(moderatorRoleList);
            userService.save(parlamentar);
        }catch(Exception ex){
            String x = ex.getMessage();
        }


        return parlamentar;
    }

    private List<Parlamentar> findAll(TownHall townhall) {

        RestTemplate restTemplate = new RestTemplate();
        ParlamentarFromAPI [] returnFromAPI = null;
        String apiURL = townhall.getApiURL().concat(GlobalConstants.SEARCH_PARLAMENTAR);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try{
            returnFromAPI = restTemplate.getForObject(apiURL, ParlamentarFromAPI[].class);
        }catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        stopWatch.stop();
        logger.log(Level.INFO, "Event_id = {0}, Event_description = {1}, Duration(ms) = {2}",
                new Object[]{SAPL_PARLAMENTAR_LIST, SAPL_PARLAMENTAR_LIST_DESCRIPTION, stopWatch.getTotalTimeMillis()});

        List<Parlamentar> list = Arrays.stream(returnFromAPI)
                .filter(parlamentarFromAPI -> Boolean.parseBoolean(parlamentarFromAPI.getAtivo()))
                .map(parlamentarFromAPI -> createOrUpdateParlamentar(townhall, parlamentarFromAPI))
                .collect(Collectors.toList());

        deactivateOldParlamentarsFromTownhall(townhall, list);
        return list;
    }

    public Parlamentar updateParlamentarInfo(UpdateParlamentarInfoDTO updateParlamentarInfoDTO) {

        if(updateParlamentarInfoDTO == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Objeto não pode ser nulo");
        }

        Parlamentar parlamentar = (Parlamentar) userService.findById(updateParlamentarInfoDTO.getId());
        parlamentar.setName(updateParlamentarInfoDTO.getName());

        return (Parlamentar) userService.save(parlamentar);
    }

    private Parlamentar createOrUpdateParlamentar(TownHall townhall, ParlamentarFromAPI parlamentarFromAPI){

        Parlamentar parlamentar = null;

        try{

            String [] splittedUsername = removeAccentsFromString(parlamentarFromAPI.getNomeParlamentar()).replace(" ",".").toLowerCase().split("\\.");
            String username = splittedUsername.length > 1 ? splittedUsername[0] + "." + splittedUsername[splittedUsername.length - 1] : splittedUsername[0];

            parlamentar = userService.findByUsername(username);

            if(parlamentar == null){

                parlamentar = new Parlamentar(parlamentarFromAPI);
                parlamentar.setId(0L);
                parlamentar.setUsername(username);
                parlamentar.setPassword(username);
                parlamentar = userService.createParlamentar(townhall, parlamentar);
            }else{
                userService.updateParlamentar(parlamentar, parlamentarFromAPI);
            }
        }catch (Exception e){
            logger.log(Level.SEVERE, e.getMessage());
        }

        return parlamentar;
    }

    //TODO: this could be a new funcionality in admin settings
    @Transactional
    private void deactivateOldParlamentarsFromTownhall(TownHall townHall, List<Parlamentar> parlamentarListFromSAPL){

        List<Parlamentar> parlamentarListFromDB = userService.findAllByTownhall(townHall);
        for(Parlamentar parlamentar : parlamentarListFromDB){
            if(!parlamentarListFromSAPL.contains(parlamentar)){
                userService.deactivateParlamentar(parlamentar.getId());
            }
        }
    }

    private String removeAccentsFromString(String name){

        String normalizer = Normalizer.normalize(name, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalizer).replaceAll("");
    }

}
