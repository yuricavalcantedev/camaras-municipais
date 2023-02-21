package com.yuri.development.camaras.municipais.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.yuri.development.camaras.municipais.enums.EVoting;
import com.yuri.development.camaras.municipais.enums.EVotingTypeResult;
import com.yuri.development.camaras.municipais.enums.EVotingVisibilityType;
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

    public void computeVotes(Integer presenceOnSessionCount, Integer numberOfVotes){

        int numberOfParlamentaresTownhall = this.parlamentarVotingList.size();

        for(ParlamentarVoting voting : this.parlamentarVotingList){
            if(voting.getResult().equals(EVoting.YES)) {
                this.yesCount ++;
            } else if(voting.getResult().equals(EVoting.NO)) {
                this.noCount ++;
            } else if(voting.getResult().equals(EVoting.ABSTENTION)) {
                this.abstentionCount ++;
            }
        }

        String auxResult = "REJEITADA";
        if(this.yesCount > this.noCount){
            auxResult = "APROVADA";
        }
        if(this.abstentionCount > this.yesCount){
            auxResult = "REJEITADA";
        }
        this.result = auxResult + " - " + this.legislativeSubjectType.getResultType().getDescription();
    }
}
