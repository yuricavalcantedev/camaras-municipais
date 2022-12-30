package com.yuri.development.camaras.municipais.dto;

import com.yuri.development.camaras.municipais.domain.Role;
import com.yuri.development.camaras.municipais.domain.TownHall;
import com.yuri.development.camaras.municipais.domain.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoggedDTO {

    private Long id;
    private String name;
    private List<Role> roles;
    private TownHall townHall;

    public UserLoggedDTO(User user){
        this.id = user.getId();
        this.name = user.getName();
        this.roles = user.getRoles();
        this.townHall = user.getTownHall();
    }
}
