package com.yuri.development.camaras.municipais.service;

import com.yuri.development.camaras.municipais.domain.*;
import com.yuri.development.camaras.municipais.dto.SubjectVotingDTO;
import com.yuri.development.camaras.municipais.dto.VoteDTO;
import com.yuri.development.camaras.municipais.enums.EPresence;
import com.yuri.development.camaras.municipais.enums.EVoting;
import com.yuri.development.camaras.municipais.repository.VotingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VotingService {

    @Autowired
    private VotingRepository votingRepository;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private ParlamentarVotingService parlamentarVotingService;
    public List<Voting> findAllBySession(Session session){
        return this.votingRepository.findAllBySession(session);
    }

    public Voting create(Session session, List<SubjectVotingDTO> subjectDTOList){

        boolean existsVotedSubject = subjectService.existsVotedSubject(session, subjectDTOList);

        List<Subject> subjectList = subjectDTOList.stream().map(subject -> new Subject(subject.getId(), session, subject.getDescription())).collect(Collectors.toList());
        Voting voting = new Voting(session, subjectList, EVoting.VOTING);
        voting = this.votingRepository.save(voting);

        for(Subject subject : subjectList){
            subject.setVoting(voting);
            subject.setStatus(EVoting.VOTING);
        }

        List<ParlamentarVoting> parlamentarVotingList = new ArrayList<>();
        for(ParlamentarPresence parlamentarPresence : session.getParlamentarPresenceList()){

            String parlamentarName = parlamentarPresence.getParlamentar().getName();
            String politicalParty = parlamentarPresence.getParlamentar().getPoliticalParty();

            ParlamentarVoting parlamentarVoting = new ParlamentarVoting(null, voting, parlamentarPresence.getParlamentar().getId(), parlamentarName, politicalParty,EVoting.NULL);
            parlamentarVoting = this.parlamentarVotingService.save(parlamentarVoting);
            parlamentarVotingList.add(parlamentarVoting);
        }

        try{

            voting.setParlamentarVotingList(parlamentarVotingList);
            session.getVotingList().add(voting);
            this.subjectService.saveAll(subjectList);
        }catch(Exception ex){
            throw ex;
        }


        return voting;
    }

    public boolean existsOpenVoting(Session session){

        for(int i = 0; i < session.getVotingList().size(); i++){
            if(session.getVotingList().get(i).getStatus().equals(EVoting.VOTING)){
                return true;
            }
        }

        return false;
    }

    public void computeVote(Session session, VoteDTO vote){

        ParlamentarVoting parlamentarVoting = this.parlamentarVotingService.findByIdAndParlamentarId(vote.getParlamentarVotingId(), vote.getParlamentarId());
        if(parlamentarVoting != null){
            for(EVoting eVoting : EVoting.values()){
                if(eVoting.name().equals(vote.getOption())){
                    parlamentarVoting.setResult(eVoting);
                }
            }
        }

        this.parlamentarVotingService.save(parlamentarVoting);
    }

    public Voting closeVoting(Session session) {

        if(this.existsOpenVoting(session)){

            Voting voting = session.getVotingList().stream().filter(v -> v.getStatus().equals(EVoting.VOTING)).findFirst().orElse(null);
            voting.setStatus(EVoting.VOTED);

            int presenceOnSession = session.getParlamentarPresenceList().stream().map(presence -> presence.getStatus().equals(EPresence.PRESENCE) ? 1 : 0).mapToInt(Integer::valueOf).sum();
            int numberOfVotes = voting.getParlamentarVotingList().stream().map(vote -> !vote.getResult().equals(EVoting.NULL) ? 1 : 0).mapToInt(Integer::valueOf).sum();
            voting.computeVotes(presenceOnSession, numberOfVotes);

            return this.votingRepository.save(voting);
        }else{
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nao existe uma votacao aberta no momento");
        }


    }
}
