package com.yuri.development.camaras.municipais.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "townhalls")
public class TownHall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    @OneToMany(mappedBy="townHall", cascade = CascadeType.REMOVE)
    @JsonManagedReference
    private List<Session> sessionList;

    @JsonIgnore
    @OneToMany(mappedBy = "townHall", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JsonManagedReference
    private List<User> userList;

    @OneToMany(mappedBy = "townHall", fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<LegislativeSubjectType> legislativeSubjectTypeList;

    public TownHall(String city, String name, String apiURL){
        this.city = city;
        this.name = name;
        this.apiURL = apiURL;
    }
}
