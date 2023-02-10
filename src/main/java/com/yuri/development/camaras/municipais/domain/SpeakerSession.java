package com.yuri.development.camaras.municipais.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@Entity
@Table(name = "speakers")
@AllArgsConstructor
@NoArgsConstructor
public class SpeakerSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="session_uuid", nullable=false)
    @JsonBackReference
    private Session session;

    @NotNull
    @Column(name = "parlamentarId")
    private Long parlamentarId;

    @NotNull
    @Column(name = "parlamentar_name")
    private String parlamentarName;

    @NotNull
    @Column(name = "parlamentar_political_party")
    private String parlamentarPoliticalParty;

    @NotNull
    @Column(name = "townhall_id")
    private Long townHallId;

    @NotNull
    @Column(name = "speaker_order")
    private Integer speakerOrder;

}
