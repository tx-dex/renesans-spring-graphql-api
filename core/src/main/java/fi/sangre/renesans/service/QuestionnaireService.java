package fi.sangre.renesans.service;

import fi.sangre.renesans.dto.CatalystDto;
import fi.sangre.renesans.dto.QuestionnaireDto;
import fi.sangre.renesans.model.Respondent;
import fi.sangre.renesans.model.RespondentGroup;
import graphql.GraphQLException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor

@Slf4j

@Service
public class QuestionnaireService {
    private final RespondentService respondentService;
    private final RespondentGroupService respondentGroupService;
    private final QuestionService questionService;

    @Transactional(readOnly = true)
    public QuestionnaireDto getQuestionnaire(final String respondentGroupId, final String respondentId) {
        if (respondentGroupId != null && respondentId != null) {
            throw new GraphQLException("Bad request: use only respondentGroupId or respondentId");
        } else if (respondentGroupId == null && respondentId == null) {
            throw new GraphQLException("Bad request: use either respondentGroupId or respondentId");
        }

        final RespondentGroup respondentGroup;
        final Respondent respondent;

        // find survey by invited respondent
        if (respondentId != null) {
            // find respondent
            respondent = respondentService.getInvitationRespondent(respondentId);

            if (respondent == null) {
                log.warn("Invalid identifier(respondentGroupId={}, respondentId={})", respondentGroupId, respondentId);
                throw new GraphQLException("Invalid identifier");
            }

            // find respondentGroup
            respondentGroup = respondentGroupService.getGroupForRespondent(respondent);
            // else find by respondent group id
        } else {
            // respondent to null
            respondent = null;
            // find respondentGroup
            respondentGroup = respondentGroupService.getRespondentGroupForSubmitting(respondentGroupId);
        }

        if (respondentGroup == null) {
            log.warn("Invalid identifier(respondentGroupId={}, respondentId={})", respondentGroupId, respondentId);
            throw new GraphQLException("Invalid identifier");
        }

        return QuestionnaireDto.builder()
                .id(respondentGroup.getSurvey().getId().toString())
                .respondent(respondent)
                .respondentGroup(respondentGroup)
                .build();
    }

    @Transactional(readOnly = true)
    public List<CatalystDto> getCatalystsWithQuestions(final QuestionnaireDto questionnaire) {
        return questionService.getCatalysts(questionnaire.getRespondentGroup());
    }
}
