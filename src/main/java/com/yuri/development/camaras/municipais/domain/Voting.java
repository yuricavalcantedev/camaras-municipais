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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_uuid", nullable = false)
    @JsonBackReference
    private Session session;

    @OneToMany(mappedBy = "voting", cascade = CascadeType.REMOVE)
    @JsonManagedReference(value = "voting-subject")
    private List<Subject> subjectList;

    @OneToMany(mappedBy="voting", cascade = CascadeType.REMOVE)
    @JsonManagedReference(value = "voting")
    private List<ParlamentarVoting> parlamentarVotingList = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private EVoting status;

    @OneToOne()
    @JoinColumn(name="legislative_subject_type", referencedColumnName = "id")
    private LegislativeSubjectType legislativeSubjectType;

    private String description;

    private String subDescription;

    @Transient
    private Integer yesCount = 0;

    @Transient
    private Integer noCount = 0;

    @Transient
    private Integer abstentionCount = 0;

    private String result;
    public Voting (Session session, List<Subject> subjectList, EVoting status){
        this.session = session;
        this.subjectList = subjectList;
        this.status = status;
    }

    private String author;
}
