package com.yuri.development.camaras.municipais.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubjectVotingDTO {

    private Long id;
    private String description;
    private String status;
}
