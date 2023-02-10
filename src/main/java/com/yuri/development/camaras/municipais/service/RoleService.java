package com.yuri.development.camaras.municipais.service;

import com.yuri.development.camaras.municipais.domain.Role;
import com.yuri.development.camaras.municipais.enums.ERole;
import com.yuri.development.camaras.municipais.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    public List<Role> getAll(){
        return this.roleRepository.findAll(Sort.by("id"));
    }

    public Long findByUserId(Long userId){
        return this.roleRepository.findByUser(userId);
    }

    public Role findById(Long id){
        return this.roleRepository.findById(id).orElseThrow();
    }

    public Optional<Role> findByName(ERole name){
        return this.roleRepository.findByName(name);
    }

    public Role save(Role role){ return this.roleRepository.save(role); }
}
