package com.yuri.development.camaras.municipais.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.yuri.development.camaras.municipais.domain.api.ParlamentarFromAPI;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.util.List;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@DiscriminatorValue("P")
public class Parlamentar extends User{

    @ManyToMany
    private List<Voting> votingList;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = true)
    private Session session;

    public Parlamentar(ParlamentarFromAPI parlamentarFromAPI){

        super.setId(parlamentarFromAPI.getId());
        super.setName(parlamentarFromAPI.getNomeParlamentar());
        this.urlImage = parlamentarFromAPI.getFotografia();
        this.politicalParty = parlamentarFromAPI.getPartido();
        this.active = Boolean.parseBoolean(parlamentarFromAPI.getAtivo());

        if(StringUtils.isNotBlank(parlamentarFromAPI.getTitular())){
            this.main = parlamentarFromAPI.getTitular().equalsIgnoreCase("sim");
        }
    }

    private String urlImage;
    private Boolean active;
    private Boolean main;
    private String politicalParty;

}
