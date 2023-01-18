package com.yuri.development.camaras.municipais.dto;

import com.yuri.development.camaras.municipais.domain.Parlamentar;
import com.yuri.development.camaras.municipais.enums.EPresence;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParlamentarInfoStatusDTO {

    private Parlamentar parlamentar;
    private String result;
    private String role;
    private EPresence status;
    private Integer priority;
}
