package com.yuri.development.camaras.municipais.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "table_role")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "town_hall_id", nullable = false)
    @JsonBackReference
    private TownHall townHall;

    @NotNull
    private String name;

    @NotNull
    private Integer position;

    @OneToOne()
    @JoinColumn(name="parlamentar_id", referencedColumnName = "id")
    private Parlamentar parlamentar;

    public TableRole(TownHall townHall, String name, Integer position) {
        this.townHall = townHall;
        this.name = name;
        this.position = position;
    }
}
