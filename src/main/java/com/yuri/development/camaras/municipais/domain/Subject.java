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
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name="session_uuid", nullable=false)
    @JsonBackReference
    private Session session;

    @ManyToOne
    @JoinColumn(name = "voting_id", nullable = true)
    @JsonBackReference
    private Voting voting;

    private String description;

    @Enumerated(EnumType.STRING)
    private EVoting status = EVoting.NOT_VOTED;

    public Subject(Session session, String description){

        this.session = session;
        this.description = description;
    }

    public Subject(Long id, Session session, String description){
        this(session, description);
        this.id = id;
    }
}
