package fi.sangre.renesans.service;

import fi.sangre.renesans.exception.CustomerNotFoundException;
import fi.sangre.renesans.exception.RemoveDefaultRespondentGroupException;
import fi.sangre.renesans.exception.RespondentGroupNotFoundException;
import fi.sangre.renesans.exception.SurveyNotFoundException;
import fi.sangre.renesans.graphql.input.RespondentGroupInput;
import fi.sangre.renesans.model.*;
import fi.sangre.renesans.repository.QuestionGroupRepository;
import fi.sangre.renesans.repository.RespondentGroupRepository;
import fi.sangre.renesans.repository.RespondentRepository;
import fi.sangre.renesans.repository.SurveyRepository;
import graphql.GraphQLException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.sangre.renesans.aaa.CacheConfig.AUTH_RESPONDENT_GROUP_IDS_CACHE;

@RequiredArgsConstructor

@Service
@Transactional
@CacheConfig(cacheManager = "authorizationCacheManager")
public class RespondentGroupService {
    private final RespondentGroupRepository respondentGroupRepository;
    private final RespondentRepository respondentRepository;
    private final QuestionGroupRepository questionGroupRepository;
    private final CustomerService customerService;
    private final SurveyRepository surveyRepository;

    public RespondentGroup getDefaultRespondentGroup(Survey survey) {
        if (survey != null) {
            return respondentGroupRepository
                    .findDefaultRespondentBySurveyAndIsDefaultTrue(survey);
        }
        return null;
    }

    public Set<RespondentGroup> getRespondentGroupsForRespondentsInternally(final List<String> respondentIds) {
        return getRespondentGroups(respondentIds);
    }

    @PostAuthorize("hasPermission(returnObject, 'READ')")
    public Set<RespondentGroup> getRespondentGroups(final List<String> respondentIds) {
        checkArgument(respondentIds != null && respondentIds.size() > 0, "respondent ids must not be empty or null");

        final Set<Respondent> respondents = respondentRepository.findAllByIdIn(respondentIds.stream().filter(Objects::nonNull).collect(Collectors.toSet()));
        return respondentGroupRepository.findAllByRespondentsIn(respondents);
    }

    @PostAuthorize("hasPermission(returnObject, 'READ')")
    public RespondentGroup getRespondentGroup(final String id) {
        if (id.equals("new")) {
            return new RespondentGroup();
        }

        return respondentGroupRepository.findById(id)
                .orElseThrow(() -> new RespondentGroupNotFoundException(id));
    }

    public List<RespondentGroup> getAllRespondentGroups() {
        return respondentGroupRepository.findAll();
    }

    @CacheEvict(cacheNames = AUTH_RESPONDENT_GROUP_IDS_CACHE, allEntries = true, condition = "#respondentGroupInput.id == null")
    public RespondentGroup storeRespondentGroup(RespondentGroupInput respondentGroupInput) {
        final RespondentGroup respondentGroup;

        List<Long> questionGroupIds = respondentGroupInput.getQuestionGroupIds();
        List<QuestionGroup> questionGroups = questionGroupRepository.findByIdInAndParentIsNull(questionGroupIds);
        if (questionGroups.isEmpty()) {
            throw new GraphQLException("Respondent group requires at least one catalyst");
        }

        if (respondentGroupInput.getId() != null) { // just update
            respondentGroup = respondentGroupRepository.findById(respondentGroupInput.getId())
            .orElseThrow(() -> new RespondentGroupNotFoundException(respondentGroupInput.getId()));

            respondentGroup.setTitle(respondentGroupInput.getTitle());
            respondentGroup.setDescription(respondentGroupInput.getDescription());
            respondentGroup.setDefaultLocale(respondentGroupInput.getDefaultLocale());

            // not allowing changing default respondent group question groups
            if (respondentGroup.getIsDefault()) {
                List<Long> existingQuestionGroupIds = respondentGroup.getQuestionGroups()
                        .stream()
                        .map(QuestionGroup::getId)
                        .collect(Collectors.toList());

                // check that not trying to change question groups for default question group in any way
                if (!questionGroupIds.containsAll(existingQuestionGroupIds) || !existingQuestionGroupIds.containsAll(questionGroupIds)) {
                    throw new GraphQLException("Default respondent group catalysts can not be edited");
                }
            }

            respondentGroup.setQuestionGroups(questionGroups);
        } else { // create new if not id was provided

            final Customer customer = customerService.getCustomer(respondentGroupInput.getCustomerId());
            if (customer == null) {
                throw new CustomerNotFoundException(respondentGroupInput.getCustomerId());
            }

            final Survey survey = surveyRepository.findById(respondentGroupInput.getSurveyId())
                    .orElseThrow(() -> new SurveyNotFoundException(respondentGroupInput.getSurveyId()));

            if (questionGroupIds == null) {
                throw new GraphQLException("Respondent group requires at least one catalyst");
            }

            respondentGroup = RespondentGroup.builder()
                    .customer(customer)
                    .survey(survey)
                    .title(respondentGroupInput.getTitle())
                    .description(respondentGroupInput.getDescription())
                    .isDefault(false)
                    .questionGroups(questionGroups)
                    .defaultLocale(respondentGroupInput.getDefaultLocale())
                    .build();
        }

        return respondentGroupRepository.save(respondentGroup);
    }

    public List<RespondentGroup> getRespondentGroups(Customer customer) {
        return respondentGroupRepository.findByCustomer(customer);
    }

    public RespondentGroup getRespondentGroups(Respondent respondent) {
        return respondentGroupRepository.findByRespondentsContaining(respondent);
    }

    public RespondentGroup getGroupForRespondent(Respondent respondent) {
        return respondentGroupRepository.findByRespondentId(respondent.getId());
    }

    @CacheEvict(cacheNames = AUTH_RESPONDENT_GROUP_IDS_CACHE, allEntries = true)
    public RespondentGroup removeRespondentGroup(String id) {
        RespondentGroup respondentGroup = getRespondentGroup(id);

        if (respondentGroup.getIsDefault().equals(true)) {
            throw new RemoveDefaultRespondentGroupException(respondentGroup.getId());
        }

        respondentGroupRepository.delete(respondentGroup);

        return respondentGroup;
    }

    public RespondentGroup getRespondentGroupForSubmitting(String id) {
        return respondentGroupRepository.findById(id).orElse(null);
    }
}
