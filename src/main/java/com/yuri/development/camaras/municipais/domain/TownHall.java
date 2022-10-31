package com.yuri.development.camaras.municipais.domain;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
@Data
@Table(name = "town_hall")
public class TownHall {

    @Id
    @GeneratedValue
    @Column(name = "town_hall_id")
    private Long id;

    @NotNull
    private String name;

    @NotNull
    private String apiURL;

    private String address;
}
