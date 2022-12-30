package com.yuri.development.camaras.municipais.domain;

import com.yuri.development.camaras.municipais.enums.EVoting;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "parlamentar_voting")
public class ParlamentarVoting {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "voting_id", nullable = false)
    private Voting voting;

    @NotNull
    private Long parlamentarId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private EVoting result;


}
