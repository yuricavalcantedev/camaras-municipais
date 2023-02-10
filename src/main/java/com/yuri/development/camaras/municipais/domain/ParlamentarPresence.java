package com.yuri.development.camaras.municipais.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.yuri.development.camaras.municipais.enums.EPresence;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "parlamentar_presence")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParlamentarPresence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parlamentar_id", nullable = false)
    private Parlamentar parlamentar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_uuid", nullable = false)
    @JsonBackReference
    private Session session;

    @NotNull
    @Column(name = "townhall_id")
    private Long townHallId;

    @Enumerated(EnumType.STRING)
    private EPresence status;

}
