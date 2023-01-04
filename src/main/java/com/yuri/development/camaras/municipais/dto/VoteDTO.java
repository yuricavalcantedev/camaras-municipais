package com.yuri.development.camaras.municipais.dto;

import com.yuri.development.camaras.municipais.enums.EVoting;
import lombok.Data;

@Data
public class VoteDTO {

    private Long parlamentarVotingId;
    private Long parlamentarId;
    private String option;
}
