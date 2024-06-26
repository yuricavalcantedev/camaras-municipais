package com.yuri.development.camaras.municipais.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.yuri.development.camaras.municipais.enums.EVoting;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

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

    @Enumerated(EnumType.STRING)
    private EVoting status = EVoting.NOT_VOTED;

    public Subject(Session session, String description, Integer saplMateriaId, String originalTextUrl){

        this.session = session;
        this.description = description;
        this.saplMateriaId = saplMateriaId;
        this.originalTextUrl = originalTextUrl;
    }

    public Subject(Long id, Session session, String description, Integer saplMateriaId){
        this(session, description, 0, "");
        this.id = id;
        this.saplMateriaId = saplMateriaId;
    }
}
