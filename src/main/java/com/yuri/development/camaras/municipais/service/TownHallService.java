package com.yuri.development.camaras.municipais.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuri.development.camaras.municipais.GlobalConstants;
import com.yuri.development.camaras.municipais.domain.TownHall;
import com.yuri.development.camaras.municipais.domain.api.LegislatureAPI;
import com.yuri.development.camaras.municipais.domain.api.LegislatureWrapperAPI;
import com.yuri.development.camaras.municipais.exception.ApiErrorException;
import com.yuri.development.camaras.municipais.repository.TownHallRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TownHallService {


    @Autowired
    private LegislativeSubjectTypeService legislativeSubjectTypeService;
    @Autowired
    private TownHallRepository townHallRepository;

    public ResponseEntity<?> create(TownHall townhall){

        this.townHallRepository.findByName(townhall.getName());

        try{
             townhall.setLegislature(this.getCurrentLegislative(townhall));
             townhall = this.townHallRepository.save(townhall);
        }catch (JsonProcessingException ex){
            return new ResponseEntity<>(new ApiErrorException(5000, "Ocorreu algum erro de comunicação com o SAPL"), HttpStatus.INTERNAL_SERVER_ERROR);
        }catch (Exception ex){
            return new ResponseEntity<>(new ApiErrorException(1004, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(townhall.getId(), HttpStatus.OK);
    }

    public List<TownHall> findAll(){

        List<TownHall> townHallList =  this.townHallRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        for(TownHall townHall : townHallList){
            townHall.setLegislativeSubjectTypeList(this.legislativeSubjectTypeService.findByTownHall(townHall));
        }
        return townHallList;
    }

    public TownHall findById(Long id){
        return this.townHallRepository.findById(id).orElseThrow();
    }

    public TownHall findByName(String name){
        return this.townHallRepository.findByName(name).orElseThrow();
    }

    public ResponseEntity<?> delete(Long id){

        try{
            this.findById(id);
            this.townHallRepository.deleteById(id);
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

            townHallDB = this.findById(townHall.getId());
            townHallDB.setName(townHall.getName());
            townHallDB.setCity(townHall.getCity());
            townHallDB.setLegislature(townHall.getLegislature());
            townHallDB.setApiURL(townHall.getApiURL());
            townHallDB.setUrlImage(townHall.getUrlImage());
            townHallDB.setLegislature(this.getCurrentLegislative(townHall));

            if(townHall.getLegislativeSubjectTypeList() != null){
                townHallDB.setLegislativeSubjectTypeList(townHall.getLegislativeSubjectTypeList());
                this.legislativeSubjectTypeService.saveAll(townHallDB.getLegislativeSubjectTypeList());
            }

            this.townHallRepository.save(townHallDB);
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
