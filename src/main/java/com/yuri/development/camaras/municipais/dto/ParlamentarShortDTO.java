package com.yuri.development.camaras.municipais.dto;

import com.yuri.development.camaras.municipais.domain.Parlamentar;
import com.yuri.development.camaras.municipais.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParlamentarShortDTO {

    private Long id;
    private String name;
    private String politicalParty;
    private String urlImage;
    private Role role;

    public ParlamentarShortDTO(Parlamentar parlamentar){

        this.id = parlamentar.getId();
        this.name = parlamentar.getName();
        this.politicalParty = parlamentar.getPoliticalParty();
        this.urlImage = parlamentar.getUrlImage();
        this.role = parlamentar.getRoles().get(0) != null ? parlamentar.getRoles().get(0) : null;
    }
}
