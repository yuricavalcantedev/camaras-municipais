package com.yuri.development.camaras.municipais.service;

import com.yuri.development.camaras.municipais.domain.TownHall;
import com.yuri.development.camaras.municipais.repository.TownHallRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        townhall = this.townHallRepository.save(townhall);
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
        Optional<TownHall> optTownHallDB = this.townHallRepository.findByName(townHall.getName());
        if(optTownHallDB.isPresent() && optTownHallDB.get().getId().equals(townHall.getId())){
            
            townHallDB = optTownHallDB.get();
            townHallDB.setName(townHall.getName());
            townHallDB.setCity(townHall.getCity());
            townHallDB.setLegislature(townHall.getLegislature());
            townHallDB.setApiURL(townHall.getApiURL());
            townHallDB.setUrlImage(townHall.getUrlImage());
            townHallDB.setLegislativeSubjectTypeList(townHall.getLegislativeSubjectTypeList());

            this.legislativeSubjectTypeService.saveAll(townHallDB.getLegislativeSubjectTypeList());
            this.townHallRepository.save(townHallDB);
        }

        return townHallDB;
    }
}
