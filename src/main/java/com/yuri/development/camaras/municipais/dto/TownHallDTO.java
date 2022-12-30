package com.yuri.development.camaras.municipais.dto;

import com.yuri.development.camaras.municipais.domain.Session;
import com.yuri.development.camaras.municipais.domain.TownHall;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TownHallDTO {

    private Long id;
    private String name;
    private String urlImage;
    private String city;
    private List<Session> sessionList;

    public TownHallDTO(TownHall townHall){

        this.id = townHall.getId();
        this.name = townHall.getName();
        this.urlImage = townHall.getUrlImage();
        this.city = townHall.getCity();
        this.sessionList = townHall.getSessionList();
    }
}
