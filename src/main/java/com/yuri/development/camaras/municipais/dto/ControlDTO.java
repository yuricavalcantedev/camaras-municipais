package com.yuri.development.camaras.municipais.dto;

import com.yuri.development.camaras.municipais.enums.EControlType;
import lombok.Data;

@Data
public class ControlDTO {
    private EControlType type;
    private String command;
    private String townHallId;
}
