package com.yuri.development.camaras.municipais.domain.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaginationFromAPI {

    @JsonProperty("previous_page")
    private Integer previousPage;

    @JsonProperty("next_page")
    private Integer nextPage;

    @JsonProperty("total_page")
    private Integer totalPage;

}
