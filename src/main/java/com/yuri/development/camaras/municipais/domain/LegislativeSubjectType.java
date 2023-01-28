package com.yuri.development.camaras.municipais.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.yuri.development.camaras.municipais.domain.api.TipoMateriaAPI;
import com.yuri.development.camaras.municipais.enums.EVotingTypeResult;
import com.yuri.development.camaras.municipais.enums.EVotingVisibilityType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LegislativeSubjectType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String title;

    @NotNull
    private Integer sequenceSAPL;

    @ManyToOne
    @JoinColumn(name = "town_hall_id", nullable = false)
    @JsonBackReference
    private TownHall townHall;

    @Enumerated(EnumType.STRING)
    private EVotingTypeResult resultType;

    @Enumerated(EnumType.STRING)
    private EVotingVisibilityType visibilityType;

    public LegislativeSubjectType (TownHall townHall, TipoMateriaAPI tipoMateriaAPI){
        this.townHall = townHall;
        this.title = tipoMateriaAPI.getName();
        this.sequenceSAPL = tipoMateriaAPI.getSequenceFromSAPL();
        this.resultType = EVotingTypeResult.MAIORIA_SIMPLES;
        this.visibilityType = EVotingVisibilityType.SIMBOLICA;
    }
}
