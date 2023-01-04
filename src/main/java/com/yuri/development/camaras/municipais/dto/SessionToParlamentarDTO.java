package com.yuri.development.camaras.municipais.dto;

import com.yuri.development.camaras.municipais.domain.Session;
import com.yuri.development.camaras.municipais.domain.SpeakerSession;
import com.yuri.development.camaras.municipais.domain.Voting;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionToParlamentarDTO {

    private String uuid;
    private Long saplId;
    private String name;
    private List<SpeakerSession> speakerSessionList;
    private List<Voting> votingList;

    public SessionToParlamentarDTO(Session session){

        this.uuid = session.getUuid();
        this.saplId = session.getSaplId();
        this.name = session.getName();
        this.speakerSessionList = session.getSpeakerList();
        this.votingList = session.getVotingList();
    }
}
