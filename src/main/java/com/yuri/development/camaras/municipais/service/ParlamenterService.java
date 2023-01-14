package com.yuri.development.camaras.municipais.service;
import com.yuri.development.camaras.municipais.GlobalConstants;
import com.yuri.development.camaras.municipais.domain.Parlamentar;
import com.yuri.development.camaras.municipais.domain.Role;
import com.yuri.development.camaras.municipais.domain.User;
import com.yuri.development.camaras.municipais.domain.api.ParlamentarFromAPI;
import com.yuri.development.camaras.municipais.domain.TownHall;
import com.yuri.development.camaras.municipais.dto.UpdateUserRoleDTO;
import com.yuri.development.camaras.municipais.enums.ERole;
import com.yuri.development.camaras.municipais.repository.TownHallRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ParlamenterService {

    @Autowired
    private TownHallRepository townHallRepository;

    @Autowired
    private TownHallService townHallService;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    private Logger logger = (Logger) LoggerFactory.getLogger(ParlamenterService.class);

    private List<Parlamentar> findAll(TownHall townhall) {

        RestTemplate restTemplate = new RestTemplate();
        ParlamentarFromAPI [] returnFromAPI = null;
        String apiURL = townhall.getApiURL().concat(GlobalConstants.SEARCH_PARLAMENTAR);

        try{
            returnFromAPI = restTemplate.getForObject(apiURL, ParlamentarFromAPI[].class);
        }catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        List<Parlamentar> list = Arrays.stream(returnFromAPI)
                .filter(parlamentarFromAPI -> Boolean.parseBoolean(parlamentarFromAPI.getAtivo()))
                .map(parlamentarFromAPI -> createOrUpdateParlamentar(townhall, parlamentarFromAPI))
                .collect(Collectors.toList());
        return list;
    }

    public List<Parlamentar> findAllByTownHall(Long id){

        List<Parlamentar> parlamentarList = new ArrayList<>();
        Optional< TownHall> optionalTownHall = this.townHallRepository.findById(id);
        optionalTownHall.ifPresent(townHall -> parlamentarList.addAll(this.findAll(townHall)));

        return parlamentarList;
    }

    private Parlamentar createOrUpdateParlamentar(TownHall townhall, ParlamentarFromAPI parlamentarFromAPI){

        Parlamentar parlamentar = null;

        try{

            String [] splittedUsername = this.removeAccentsFromString(parlamentarFromAPI.getNomeParlamentar()).replace(" ",".").toLowerCase().split("\\.");
            String username = splittedUsername.length > 1 ? splittedUsername[0] + "." + splittedUsername[splittedUsername.length - 1] : splittedUsername[0];

            parlamentar = this.userService.findByUsername(username);

            if(parlamentar == null){

                parlamentar = new Parlamentar(parlamentarFromAPI);
                parlamentar.setId(0L);
                parlamentar.setUsername(username);
                parlamentar.setPassword(username);
                parlamentar = this.userService.createParlamentar(townhall, parlamentar);
            }else{
                this.userService.updateParlamentar(parlamentar, parlamentarFromAPI);
            }
        }catch (Exception e){
            this.logger.error(e.getMessage());
        }

        return parlamentar;
    }

    private String removeAccentsFromString(String name){

        String normalizer = Normalizer.normalize(name, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalizer).replaceAll("");
    }

    public Parlamentar updateUserToModeratorView(UpdateUserRoleDTO updateUserRoleDTO) {

        List<Role> basicRoleList = new ArrayList<>();
        List<Role> moderatorRoleList = new ArrayList<>();
        Optional<Role> optBasicUserRole = this.roleService.findByName(ERole.ROLE_USER);
        Optional<Role> optModeratorViewRole = this.roleService.findByName(ERole.ROLE_MODERATOR_VIEW);

        if(optBasicUserRole.isEmpty()){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro inesperado, contacte o administrador");
        }

        if(optModeratorViewRole.isEmpty()){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro inesperado, contacte o administrador");
        }

        basicRoleList.add(optBasicUserRole.get());

        Parlamentar parlamentar = (Parlamentar) this.userService.findById(updateUserRoleDTO.getParlamentarId());
        TownHall townHall = this.townHallService.findById(updateUserRoleDTO.getTownHallId());
        List<Parlamentar> parlamentarList = this.userService.findAllByTownhall(townHall);
        for(User parlamentarAux : parlamentarList){
            parlamentarAux.setRoles(basicRoleList);
        }

        this.userService.saveAllParlamentar(parlamentarList);

        try{
            moderatorRoleList.add(optModeratorViewRole.get());
            parlamentar.setRoles(moderatorRoleList);
            this.userService.save(parlamentar);
        }catch(Exception ex){
            String x = ex.getMessage();
        }


        return parlamentar;
    }
}
