package com.yuri.development.camaras.municipais.service;

import com.yuri.development.camaras.municipais.domain.TableRole;
import com.yuri.development.camaras.municipais.domain.TownHall;
import com.yuri.development.camaras.municipais.repository.TableRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TableRoleService {

    @Autowired
    private TableRoleRepository tableRoleRepository;

    public List<TableRole> findAllByTownhall(TownHall townHall){
        if(townHall != null){
            return tableRoleRepository.findByTownHallOrderByPositionAsc(townHall);
        }

        return null;
    }

    public Optional<TableRole> findPresidentIdByTownhall(TownHall townHall){
        return tableRoleRepository.findPresidentRoleByTownHall(townHall.getId());
    }

    public List<TableRole> saveAll(List<TableRole> tableRoleList){
        return tableRoleRepository.saveAll(tableRoleList);
    }
}
