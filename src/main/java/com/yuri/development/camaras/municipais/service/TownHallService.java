package com.yuri.development.camaras.municipais.service;

import com.yuri.development.camaras.municipais.domain.TownHall;
import com.yuri.development.camaras.municipais.repository.TownHallRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TownHallService {

    @Autowired
    private TownHallRepository townHallRepository;

    public Long create(TownHall townhall){

        this.townHallRepository.findByName(townhall.getName());
        townhall = this.townHallRepository.save(townhall);
        return townhall.getId();
    }

    public List<TownHall> findAll(){ return this.townHallRepository.findAll();}

    public TownHall findById(Long id){return this.townHallRepository.findById(id).orElseThrow();}

    public TownHall findByName(String name){
        return this.townHallRepository.findByName(name).orElseThrow();
    }

    public void delete(Long id){ this.townHallRepository.deleteById(id);}
}
