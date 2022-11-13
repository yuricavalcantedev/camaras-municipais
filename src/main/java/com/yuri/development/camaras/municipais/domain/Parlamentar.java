package com.yuri.development.camaras.municipais.domain;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class Parlamentar {

    public Parlamentar(ParlamentarFromAPI parlamentarFromAPI){
        this.id = parlamentarFromAPI.getId();
        this.name = parlamentarFromAPI.getNomeParlamentar();
        this.urlImage = parlamentarFromAPI.getFotografia();
        this.politicalParty = parlamentarFromAPI.getPartido();
        this.active = Boolean.parseBoolean(parlamentarFromAPI.getAtivo());

        if(StringUtils.isNotBlank(parlamentarFromAPI.getTitular())){
            this.main = parlamentarFromAPI.getTitular().equalsIgnoreCase("sim");
        }
    }

    private Long id;
    private String name;
    private String urlImage;
    private Boolean active;
    private Boolean main;
    private String politicalParty;
}
