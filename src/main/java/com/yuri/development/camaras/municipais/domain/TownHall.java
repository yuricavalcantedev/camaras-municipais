package com.yuri.development.camaras.municipais.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import org.hibernate.annotations.Cascade;
import org.springframework.context.annotation.Lazy;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
@Data
@Table(name = "townhalls")
public class TownHall {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "town_hall_id")
    private Long id;

    @NotNull
    private String name;
    private String urlImage;

    @NotNull
    private String city;
    private String legislature;

    @NotNull
    private String apiURL;

    @JsonIgnore
    @OneToMany(mappedBy="townHall")
    private List<Session> sessionList;

    @JsonIgnore
    @OneToMany(mappedBy = "townHall", fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<User> userList;

    @OneToMany(mappedBy = "townHall", fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<LegislativeSubjectType> legislativeSubjectTypeList;

    private Integer updateLegislature = 0;
}
