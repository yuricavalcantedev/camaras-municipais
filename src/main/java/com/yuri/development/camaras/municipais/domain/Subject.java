package com.yuri.development.camaras.municipais.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.yuri.development.camaras.municipais.enums.EVoting;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "subjects")
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="session_uuid", nullable=false)
    @JsonBackReference(value = "session-subject")
    private Session session;

    @ManyToOne
    @JoinColumn(name = "voting_id", nullable = true)
    @JsonBackReference(value = "voting-subject")
    private Voting voting;
    private Integer saplMateriaId;

    private String description;
    private String originalTextUrl;

    @Transient
    private Integer subjectOrderSapl;

    @Enumerated(EnumType.STRING)
    private EVoting status = EVoting.NOT_VOTED;

    public Subject(Session session, String description, Integer saplMateriaId, String originalTextUrl, Integer subjectOrderSapl){

        this.session = session;
        this.description = description;
        this.saplMateriaId = saplMateriaId;
        this.originalTextUrl = originalTextUrl;
        this.subjectOrderSapl = subjectOrderSapl;
    }

    public Subject(Long id, Session session, String description, Integer saplMateriaId){
        this(session, description, 0, "", 0);
        this.id = id;
        this.saplMateriaId = saplMateriaId;
    }
}
