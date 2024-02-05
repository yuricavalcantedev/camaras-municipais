package com.yuri.development.camaras.municipais.domain;

import com.yuri.development.camaras.municipais.enums.EControlType;
import com.yuri.development.camaras.municipais.enums.EVoting;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "control")
public class Control {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private EControlType type;
    private String command;
    private String townHallId;

    public Control (EControlType type, String command, String townHallId){
        this.type = type;
        this.command = command;
        this.townHallId = townHallId;
    }
}
