package com.yuri.development.camaras.municipais.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuri.development.camaras.municipais.GlobalConstants;
import com.yuri.development.camaras.municipais.domain.TownHall;
import com.yuri.development.camaras.municipais.domain.User;
import com.yuri.development.camaras.municipais.domain.api.LegislatureAPI;
import com.yuri.development.camaras.municipais.domain.api.LegislatureWrapperAPI;
import com.yuri.development.camaras.municipais.exception.ApiErrorException;
import com.yuri.development.camaras.municipais.repository.TownHallRepository;
import com.yuri.development.camaras.municipais.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class TownHallService {


    @Autowired
    private LegislativeSubjectTypeService legislativeSubjectTypeService;
    @Autowired
    private TownHallRepository townHallRepository;

    @Autowired
    private TableRoleService tableRoleService;

    @Autowired
    private UserRepository userRepository;

    public ResponseEntity<?> create(TownHall townhall){

        townHallRepository.findByName(townhall.getName());

        try{
             if(!townhall.getApiURL().equals("empty")){
                 townhall.setLegislature(this.getCurrentLegislative(townhall));
             }
            townhall = townHallRepository.save(townhall);
        }catch (JsonProcessingException ex){
            return new ResponseEntity<>(new ApiErrorException(5000, "Ocorreu algum erro de comunicação com o SAPL"), HttpStatus.INTERNAL_SERVER_ERROR);
        }catch (Exception ex){
            return new ResponseEntity<>(new ApiErrorException(1004, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(townhall.getId(), HttpStatus.OK);
    }

    public List<TownHall> findAll(){

        List<TownHall> townHallList =  townHallRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));

        for(int i = 0; i < townHallList.size(); i ++){

            TownHall townHall = townHallList.get(i);

            townHall.setLegislativeSubjectTypeList(legislativeSubjectTypeService.findByTownHall(townHall));
            townHall.setTableRoleList(tableRoleService.findAllByTownhall(townHall));

            List<User> userList = userRepository.findByTownHallAndType(townHall.getId(), "P");
            userList = userList.stream()
                                .sorted(Comparator.comparing(User::getName))
                                .map(user -> new User(user.getId(), user.getName(), user.getUsername(), user.getRoles(), user.getIsRecoveringPassword()))
                                .collect(Collectors.toList());

            townHall.setUserList(userList);
            townHallList.set(i, townHall);
        }
        return townHallList;
    }

    public TownHall findById(Long id){

        TownHall townHall = townHallRepository.findById(id).orElseThrow();
        townHall.setTableRoleList(tableRoleService.findAllByTownhall(townHall));

        return townHall;
    }

    public TownHall findByName(String name){
        return townHallRepository.findByName(name).orElseThrow();
    }

    public TownHall findByApiURL(String apiURL){
        return townHallRepository.findByApiURL(apiURL).orElse(null);
    }
    public ResponseEntity<?> delete(Long id){

        try{
            findById(id);
            townHallRepository.deleteById(id);
        }catch (NoSuchElementException ex){
            return new ResponseEntity<>(new ApiErrorException(1001, "Recurso nao encontrado"), HttpStatus.BAD_REQUEST);
        }catch (Exception ex){
            return new ResponseEntity<>(new ApiErrorException(1001, "Erro inesperado. Contacte o adminstrador."), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(id, HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> update(TownHall townHall) {

        TownHall townHallDB = null;
        try{

            townHallDB = findById(townHall.getId());
            townHallDB.setName(townHall.getName());
            townHallDB.setCity(townHall.getCity());
            townHallDB.setLegislature(townHall.getLegislature());
            townHallDB.setApiURL(townHall.getApiURL());
            townHallDB.setUrlImage(townHall.getUrlImage());
            townHallDB.setLegislature(getCurrentLegislative(townHall));

            if(townHall.getLegislativeSubjectTypeList() != null){
                townHallDB.setLegislativeSubjectTypeList(townHall.getLegislativeSubjectTypeList());
                legislativeSubjectTypeService.saveAll(townHallDB.getLegislativeSubjectTypeList());
            }

            if(townHall.getTableRoleList() != null){
                townHallDB.setTableRoleList(townHall.getTableRoleList());
                tableRoleService.saveAll(townHallDB.getTableRoleList());
            }

            townHallRepository.save(townHallDB);
        }catch (JsonProcessingException ex){
            return new ResponseEntity<>(new ApiErrorException(5001, "Ocorreu algum erro de comunicação com o SAPL"), HttpStatus.INTERNAL_SERVER_ERROR);
        }catch (NoSuchElementException ex){
            return new ResponseEntity<>(new ApiErrorException(1001, "Recurso nao encontrado"), HttpStatus.BAD_REQUEST);
        }catch (Exception ex){
            return new ResponseEntity<>(new ApiErrorException(5001, "Ocorreu um erro inesperado"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(townHallDB, HttpStatus.OK);
    }

    private String getCurrentLegislative(TownHall townHall) throws JsonProcessingException {

        String result = "";
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();
        List<LegislatureAPI> legislatureAPIList = new ArrayList<>();

        boolean exit = false;
        int pageNumber = 0, highestSaplNumber = 0;

        while(!exit){

            pageNumber++;
            LegislatureWrapperAPI wrapper = objectMapper.readValue(restTemplate.getForObject(townHall.getApiURL() + GlobalConstants.GET_CURRENT_LEGISLATURE + "?page=" + pageNumber, String.class), LegislatureWrapperAPI.class);
            for(LegislatureAPI legislatureAPI : wrapper.getResults()){
                if(legislatureAPI.getSaplNumber() > highestSaplNumber){
                    result = legislatureAPI.getTitle();
                    highestSaplNumber = legislatureAPI.getSaplNumber();
                }
            }
            exit = wrapper.getPagination().getNextPage() == null;
        }
        return result;
    }
}
