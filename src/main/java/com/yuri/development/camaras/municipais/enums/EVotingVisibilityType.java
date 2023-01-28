package com.yuri.development.camaras.municipais.enums;

public enum EVotingVisibilityType {

    SIMBOLICA, NOMINAL, SECRETA;

    public static EVotingVisibilityType searchType(String visibilityType){
        for(EVotingVisibilityType eType : EVotingVisibilityType.values()){
            if(eType.name().equals(visibilityType)){
                return eType;
            }
        }
        return null;
    }
}
