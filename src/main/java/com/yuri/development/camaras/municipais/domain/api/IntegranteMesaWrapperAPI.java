package com.yuri.development.camaras.municipais.domain.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class IntegranteMesaWrapperAPI {

    private PaginationFromAPI pagination;

    @JsonProperty("results")
    private List<IntegranteMesaAPI> integranteMesaList;

}
