package fi.sangre.renesans.service;

import com.querydsl.core.BooleanBuilder;
import fi.sangre.renesans.dto.FiltersDto;
import fi.sangre.renesans.exception.NotTheSameCustomerException;
import fi.sangre.renesans.exception.RespondentGroupNotFoundException;
import fi.sangre.renesans.exception.RespondentNotFoundException;
import fi.sangre.renesans.graphql.input.AnswerInput;
import fi.sangre.renesans.graphql.input.RespondentInput;
import fi.sangre.renesans.model.*;
import fi.sangre.renesans.repository.AnswerRepository;
import fi.sangre.renesans.repository.RespondentRepository;
import graphql.GraphQLException;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
public class RespondentService {
    @Autowired
    private CustomerService customerService;
    @Autowired
    private RespondentRepository respondentRepository;
    @Autowired
    private RespondentGroupService respondentGroupService;
    @Autowired
    private RespondentOptionService respondentOptionService;
    @Autowired
    private AnswerService answerService;
    @Autowired
    private AnswerRepository answerRepository;
    @Autowired
    private InvitationService invitationService;
    @Autowired
    private FilterService filterService;

    public Respondent createRespondent(String respondentGroupId, RespondentInput respondentInput) {
        RespondentGroup respondentGroup = respondentGroupService.getRespondentGroupForSubmitting(respondentGroupId);
        if (respondentGroup == null) {
            throw new RespondentGroupNotFoundException(respondentGroupId);
        }

        RespondentOption position = respondentOptionService.getRespondentOption(respondentInput.getPosition());
        RespondentOption industry = respondentOptionService.getRespondentOption(respondentInput.getIndustry());
        RespondentOption segment = respondentOptionService.getRespondentOption(respondentInput.getSegment());

        Respondent respondent;
        String respondentId = respondentInput.getId();
        if (respondentId != null) {
            respondent = respondentRepository.findById(respondentId).orElse(null);
            if (respondent != null) {
                if (respondent.getState() == Respondent.State.FINISHED) {
                    throw new GraphQLException("Respondent already answered");
                }
            } else {
                respondent = new Respondent();
            }
        } else {
            respondent = new Respondent();
        }

        respondent.setState(Respondent.State.FINISHED);
        respondent.setRespondentGroup(respondentGroup);
        respondent.setName(respondentInput.getName());
        respondent.setEmail(respondentInput.getEmail());
        respondent.setAge(respondentInput.getAge());
        respondent.setPosition(position);
        respondent.setIndustry(industry);
        respondent.setSegment(segment);
        respondent.setPhone(respondentInput.getPhone());
        respondent.setGender(respondentInput.getGender());
        respondent.setCountry(respondentInput.getCountry());
        respondent.setExperience(respondentInput.getExperience());
        respondent.setConsent(respondentInput.getConsent());
        respondent.setLocale(respondentInput.getLocale());

        return respondentRepository.save(respondent);
    }

    @Transactional
    public Respondent submitSurvey(String respondentGroupId, RespondentInput respondentInput, List<AnswerInput> answers) {
        Respondent respondent = createRespondent(respondentGroupId, respondentInput);
        respondent.setAnswerTime(LocalDateTime.now(DateTimeZone.UTC).toDate());
        answerService.addAnswers(answers, respondent);
        return respondent;
    }

    public List<Respondent> getUniqueRespondentsInternally(final FiltersDto filters) {
        return getUniqueRespondents(filters);
    }

    @PostFilter("hasPermission(filterObject, 'READ')")
    public List<Respondent> getUniqueRespondents(final FiltersDto filters) {
        final QRespondent respondent = QRespondent.respondent;
        final BooleanBuilder respondentBuilder = filterService.apply(filters, respondent);

        //TODO refactor
        final Map<String, Respondent> uniqueRespondents = new HashMap<>();
        final List<Respondent> respondents = respondentRepository.findAll(respondentBuilder);
        respondents.forEach(r -> {
            if (r.getOriginalId() == null) {
                uniqueRespondents.put(r.getId(), r);
            } else {
                uniqueRespondents.put(r.getOriginalId(), r);
            }
        });

        return new ArrayList<>(uniqueRespondents.values());
    }

    public List<Respondent> getFinishedRespondents(RespondentGroup respondentGroup) {
        return respondentRepository.findByRespondentGroupAndState(respondentGroup, Respondent.State.FINISHED);
    }

    @PostAuthorize("hasPermission(returnObject, 'READ')")
    public Respondent getRespondent(final String id) {
        return respondentRepository.findById(id)
                .orElseThrow(() -> new RespondentNotFoundException(id));
    }

    public List<Respondent> getFinishedRespondentsByCustomer(final Customer customer) {
        List<RespondentGroup> respondentGroups = respondentGroupService.getRespondentGroups(customer);

        List<Respondent> respondents = new ArrayList<>();
        for (RespondentGroup respondentGroup : respondentGroups) {
            respondents.addAll(getFinishedRespondents(respondentGroup));
        }

        return respondents;
    }

    public Respondent moveRespondentToRespondentGroup(String respondentId, String respondentGroupId) {
        RespondentGroup respondentGroup = respondentGroupService.getRespondentGroup(respondentGroupId);
        Respondent respondent = getRespondent(respondentId);

        validateThatRespondentDoesNotBelongToGroup(respondentGroup, respondent);

        Long currentRespondentCustomerId = respondent.getRespondentGroup().getCustomer().getId();
        Long customerToBeMovedId = respondentGroup.getCustomer().getId();
        if (!currentRespondentCustomerId.equals(customerToBeMovedId)) {
            throw new NotTheSameCustomerException();
        }

        respondent.setRespondentGroup(respondentGroup);

        if (respondent.getInvitationHash() != null) {
            try {
                invitationService.moveInvitationToGroup(respondent.getInvitationHash(), respondentGroupId);
            } catch (Exception e) {
                log.error("Error moving respondent = {} to the group = {}", respondent, respondentGroup);
                log.error("Moving invitation failed");
                e.printStackTrace();
                throw new GraphQLException("Error while moving respondent");
            }
        }

        return respondentRepository.save(respondent);
    }

    @Transactional
    public Respondent copyRespondentToRespondentGroup(String respondentId, String respondentGroupId) {
        RespondentGroup respondentGroup = respondentGroupService.getRespondentGroup(respondentGroupId);
        Respondent respondent = getRespondent(respondentId);

        validateThatRespondentDoesNotBelongToGroup(respondentGroup, respondent);

        Long currentRespondentCustomerId = respondent.getRespondentGroup().getCustomer().getId();
        Long customerToBeMovedId = respondentGroup.getCustomer().getId();
        if (!currentRespondentCustomerId.equals(customerToBeMovedId)) {
            throw new NotTheSameCustomerException();
        }

        Respondent newRespondent = new Respondent(respondent);

        newRespondent.setId(null);
        newRespondent.setRespondentGroup(respondentGroup);
        newRespondent.setRespondentGroupId(respondentGroupId);
        newRespondent.setInvitationHash(null);

        Respondent savedRespondent = respondentRepository.save(newRespondent);

        newRespondent.getAnswers().forEach(answer -> {
            answer.setId(null);
            answer.setRespondent(savedRespondent);
        });

        answerRepository.saveAll(newRespondent.getAnswers());

        return savedRespondent;
    }


    @Transactional
    public List<Respondent> removeRespondents(List<Respondent> respondents) {
        if (respondents != null && !respondents.isEmpty()) {
            respondentRepository.deleteAll(respondents);
        }
        return respondents;
    }

    public Respondent removeRespondent(String id) {
        Respondent respondent = getRespondent(id);
        List<Respondent> respondents = removeRespondents(new ArrayList<>(Collections.singletonList(respondent)));

        return respondents.get(0);
    }

    public Respondent getInvitationRespondent(String respondentId) {
        Respondent respondent = respondentRepository.findById(respondentId).orElse(null);

        if (respondent != null) {
            // if respondent has not been created by sending an invitation return null
            if (respondent.getInvitationHash() == null) {
                return null;
            }

            // check if respondent state is finished
            Respondent.State state = respondent.getState();
            if (state == Respondent.State.FINISHED) {
                throw new GraphQLException("You have already answered the survey");
            }

            // if respondent state is "invited", update to "started"
            if (state == Respondent.State.INVITED) {
                respondent.setState(Respondent.State.STARTED);
                respondent = respondentRepository.save(respondent);
            }
        }

        return respondent;
    }

    private void validateThatRespondentDoesNotBelongToGroup(final RespondentGroup group, final Respondent respondent) {
        final String originalId = respondent.getOriginalId() != null ? respondent.getOriginalId() : respondent.getId(); // use id of the respondent is a original one
        if (respondentRepository.findOriginalRespondent(group.getId(), originalId).isPresent()) {
            throw new GraphQLException("Respondent already belongs to the group");
        }
    }
}
