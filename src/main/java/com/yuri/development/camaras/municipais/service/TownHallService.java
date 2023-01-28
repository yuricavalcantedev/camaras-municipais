package com.yuri.development.camaras.municipais.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuri.development.camaras.municipais.GlobalConstants;
import com.yuri.development.camaras.municipais.domain.TownHall;
import com.yuri.development.camaras.municipais.domain.api.LegislatureAPI;
import com.yuri.development.camaras.municipais.domain.api.LegislatureWrapperAPI;
import com.yuri.development.camaras.municipais.repository.TownHallRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TownHallService {


    @Autowired
    private LegislativeSubjectTypeService legislativeSubjectTypeService;
    @Autowired
    private TownHallRepository townHallRepository;

    public Long create(TownHall townhall){

        this.townHallRepository.findByName(townhall.getName());

        try{
             this.getCurrentLegislative(townhall);
             townhall = this.townHallRepository.save(townhall);
        }catch (JsonProcessingException ex){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu algum erro de comunicação com o SAPL");
        }catch (Exception ex){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
        return townhall.getId();
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

    public void delete(Long id){ this.townHallRepository.deleteById(id);}

    @Transactional
    public TownHall update(TownHall townHall) {

        TownHall townHallDB = null;

        try{
            Optional<TownHall> optTownHallDB = this.townHallRepository.findByName(townHall.getName());
            if(optTownHallDB.isPresent() && optTownHallDB.get().getId().equals(townHall.getId())){

                townHallDB = optTownHallDB.get();
                townHallDB.setName(townHall.getName());
                townHallDB.setCity(townHall.getCity());
                townHallDB.setLegislature(townHall.getLegislature());
                townHallDB.setApiURL(townHall.getApiURL());
                townHallDB.setUrlImage(townHall.getUrlImage());
                townHallDB.setLegislativeSubjectTypeList(townHall.getLegislativeSubjectTypeList());
                townHallDB.setLegislature(this.getCurrentLegislative(townHall));
                this.legislativeSubjectTypeService.saveAll(townHallDB.getLegislativeSubjectTypeList());
                this.townHallRepository.save(townHallDB);
            }
        }catch (JsonProcessingException ex){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu algum erro de comunicação com o SAPL");
        }catch (Exception ex){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        return townHallDB;
    }

    private String getCurrentLegislative(TownHall townHall) throws JsonProcessingException {

        String result = "";
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();
        List<LegislatureAPI> legislatureAPIList = new ArrayList<>();

        boolean exit = false;
        int pageNumber = 0, highestSaplNumber = 0;

        while(!exit){

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
