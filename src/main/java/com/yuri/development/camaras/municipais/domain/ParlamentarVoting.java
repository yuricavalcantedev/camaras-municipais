package com.yuri.development.camaras.municipais.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.yuri.development.camaras.municipais.enums.EVoting;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "parlamentar_voting")
public class ParlamentarVoting {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "voting_id", nullable = false)
    @JsonBackReference(value = "voting")
    private Voting voting;

    @NotNull
    private Long parlamentarId;

    @Transient
    private String parlamentarName;

    @Transient
    private String parlamentarPoliticalParty;

    @NotNull
    @Enumerated(EnumType.STRING)
    private EVoting result;

}
