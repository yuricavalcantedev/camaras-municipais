package com.yuri.development.camaras.municipais.dto;

import com.yuri.development.camaras.municipais.domain.Session;
import com.yuri.development.camaras.municipais.domain.SpeakerSession;
import com.yuri.development.camaras.municipais.domain.Voting;
import com.yuri.development.camaras.municipais.enums.EVoting;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionToParlamentarDTO {

    private String uuid;
    private Long saplId;
    private String name;
    private List<SpeakerSession> speakerSessionList;
    private Voting voting;

    private String sessionSubjectURL;

    public SessionToParlamentarDTO(Session session, String sessionSubjectURL){

        this.uuid = session.getUuid();
        this.saplId = session.getSaplId();
        this.name = session.getName();
        this.speakerSessionList = session.getSpeakerList();
        this.voting = session.getVotingList().stream().filter(voting -> voting.getStatus().equals(EVoting.VOTING)).findFirst().orElse(null);
        this.sessionSubjectURL = sessionSubjectURL;
    }
}
