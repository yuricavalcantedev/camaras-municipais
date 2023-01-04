package com.yuri.development.camaras.municipais.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.yuri.development.camaras.municipais.enums.EVoting;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "voting")
public class Voting {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_uuid", nullable = false)
    @JsonBackReference
    private Session session;

    @OneToMany(mappedBy = "voting", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Subject> subjectList;

    @OneToMany(mappedBy="voting", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<ParlamentarVoting> parlamentarVotingList = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private EVoting status;


    public Voting (Session session, List<Subject> subjectList, EVoting status){
        this.session = session;
        this.subjectList = subjectList;
        this.status = status;
    }
}
