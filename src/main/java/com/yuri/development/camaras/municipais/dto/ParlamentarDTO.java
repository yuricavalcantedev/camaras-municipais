package com.yuri.development.camaras.municipais.dto;

import com.yuri.development.camaras.municipais.enums.EPresence;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParlamentarDTO {

    private Long id;
    private String name;
    private String username;
    private String politicalParty;
    private EPresence status;

}
