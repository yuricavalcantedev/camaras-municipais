package com.yuri.development.camaras.municipais.dto;

import com.yuri.development.camaras.municipais.domain.Voting;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionVotingInfoDTO {

    private String sessionUUID;
    private Voting voting;
    private List<ParlamentarInfoStatusDTO> parlamentarTableList;
    private List<ParlamentarInfoStatusDTO> parlamentarList;

}
