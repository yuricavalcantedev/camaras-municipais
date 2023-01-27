package com.yuri.development.camaras.municipais.enums;

public enum EVotingTypeResult {

    MAIORIA_QUALIFICADA, MAIORIA_SIMPLES, MAIORIA_ABSOLUTA;

    public static EVotingTypeResult searchType(String type){
        for(EVotingTypeResult evotingType : EVotingTypeResult.values()){
            if(evotingType.equals(type)){
                return evotingType;
            }
        }
        return null;
    }
}
