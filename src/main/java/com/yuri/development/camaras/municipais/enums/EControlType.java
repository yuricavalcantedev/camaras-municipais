package com.yuri.development.camaras.municipais.enums;

import lombok.Getter;

@Getter
public enum EControlType {
    TIME("timeControl"),
    VOTING_PANEL("votingPanel");

    private final String value;

    EControlType(String value) {
        this.value = value;
    }
}