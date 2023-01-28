package com.yuri.development.camaras.municipais.service;

import com.yuri.development.camaras.municipais.domain.LegislativeSubjectType;
import com.yuri.development.camaras.municipais.domain.TownHall;
import com.yuri.development.camaras.municipais.repository.LegislativeSubjectTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LegislativeSubjectTypeService {

    @Autowired
    private LegislativeSubjectTypeRepository repository;

    public List<LegislativeSubjectType> findAll(){
        return this.repository.findAll();
    }

    public LegislativeSubjectType findByTownHallAndTitle(TownHall townHall, String title){
        return this.repository.findByTownHallAndTitle(townHall, title);
    }
    public LegislativeSubjectType save(LegislativeSubjectType legislativeSubjectType){
        return this.repository.save(legislativeSubjectType);
    }

    public List<LegislativeSubjectType> saveAll(List<LegislativeSubjectType> legislativeSubjectTypeList){
        return this.repository.saveAll(legislativeSubjectTypeList);
    }

    public List<LegislativeSubjectType> findByTownHall(TownHall townHall){
        return this.repository.findByTownHall(townHall);
    }
}
