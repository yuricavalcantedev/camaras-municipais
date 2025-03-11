package com.yuri.development.camaras.municipais.controller.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddSubjectRequest {

    private String type;
    private Integer number;
    private Integer year;
    private String author;
    private String description;
    private String originalTextUrl;
    private Integer subjectOrderSapl;
}
