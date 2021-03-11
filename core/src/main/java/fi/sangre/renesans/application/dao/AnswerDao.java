package fi.sangre.renesans.application.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPAExpressions;
import fi.sangre.renesans.application.assemble.ParameterAnswerAssembler;
import fi.sangre.renesans.application.assemble.RespondentAssembler;
import fi.sangre.renesans.application.model.CatalystId;
import fi.sangre.renesans.application.model.ParameterId;
import fi.sangre.renesans.application.model.Respondent;
import fi.sangre.renesans.application.model.SurveyId;
import fi.sangre.renesans.application.model.answer.AnswerStatus;
import fi.sangre.renesans.application.model.answer.LikertQuestionAnswer;
import fi.sangre.renesans.application.model.answer.OpenQuestionAnswer;
import fi.sangre.renesans.application.model.answer.ParameterItemAnswer;
import fi.sangre.renesans.application.model.filter.RespondentFilter;
import fi.sangre.renesans.application.model.filter.RespondentParameterFilter;
import fi.sangre.renesans.application.model.parameter.Parameter;
import fi.sangre.renesans.application.model.parameter.ParameterItem;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.application.model.respondent.GuestId;
import fi.sangre.renesans.application.model.respondent.RespondentId;
import fi.sangre.renesans.application.utils.ParameterUtils;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.persistence.model.QSurveyRespondent;
import fi.sangre.renesans.persistence.model.SurveyRespondentState;
import fi.sangre.renesans.persistence.model.answer.*;
import fi.sangre.renesans.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.*;

@RequiredArgsConstructor
@Slf4j

@Component
public class AnswerDao {
    private final CatalystOpenQuestionAnswerRepository catalystOpenQuestionAnswerRepository;
    private final LikerQuestionAnswerRepository likerQuestionAnswerRepository;
    private final ParameterAnswerRepository parameterAnswerRepository;
    private final GuestParameterAnswerRepository guestParameterAnswerRepository;
    private final RespondentAssembler respondentAssembler;
    private final ParameterAnswerAssembler parameterAnswerAssembler;
    private final SurveyRespondentRepository surveyRespondentRepository;
    private final ParameterUtils parameterUtils;

    @NonNull
    @Transactional(readOnly = true)
    public Map<QuestionId, List<String>> getAllOpenAnswers(@NonNull final SurveyId surveyId, @NonNull final CatalystId catalystId, @NonNull final Set<RespondentId> respondentIds) {
        if (respondentIds.size() > 0) {
            final List<CatalystOpenQuestionAnswerEntity> answers = catalystOpenQuestionAnswerRepository
                    .findAllByIdSurveyIdAndCatalystIdAndIdRespondentIdInOrderByAnswerTimeDesc(surveyId.getValue(),
                            catalystId.getValue(),
                            RespondentId.toUUIDs(respondentIds));

            return from(answers);
        } else {
            return ImmutableMap.of();
        }
    }

    @NonNull
    @Transactional(readOnly = true)
    public Map<QuestionId, List<String>> getPublicOpenAnswers(@NonNull final SurveyId surveyId, @NonNull final CatalystId catalystId, @NonNull final Set<RespondentId> respondentIds) {
        if (respondentIds.size() > 0) {
            final List<CatalystOpenQuestionAnswerEntity> answers = catalystOpenQuestionAnswerRepository
                    .findAllByIdSurveyIdAndCatalystIdAndIsPublicIsTrueAndIdRespondentIdInOrderByAnswerTimeDesc(surveyId.getValue(),
                            catalystId.getValue(),
                            RespondentId.toUUIDs(respondentIds));
            return from(answers);
        } else {
            return ImmutableMap.of();
        }
    }


    @NonNull
    @Transactional(readOnly = true)
    public Set<OpenQuestionAnswer> getOpenAnswers(@NonNull final SurveyId surveyId, @NonNull final RespondentId respondentId) {
        return catalystOpenQuestionAnswerRepository.findAllByIdSurveyIdAndIdRespondentId(surveyId.getValue(), respondentId.getValue())
                .stream()
                .map(e -> OpenQuestionAnswer.builder()
                        .id(new QuestionId(e.getId().getQuestionId()))
                        .catalystId(new CatalystId(e.getCatalystId()))
                        .status(e.getStatus())
                        .isPublic(e.isPublic())
                        .response(e.getResponse())
                        .build())
                .collect(collectingAndThen(toSet(), Collections::unmodifiableSet));
    }

    @Transactional
    public void saveAnswer(@NonNull final OpenQuestionAnswer answer, @NonNull final SurveyId surveyId, @NonNull final RespondentId respondentId) {
        final QuestionAnswerId id = new QuestionAnswerId(surveyId.getValue(), respondentId.getValue(), answer.getId().getValue());

        final CatalystOpenQuestionAnswerEntity entity = CatalystOpenQuestionAnswerEntity.builder()
                .id(id)
                .catalystId(answer.getCatalystId().getValue())
                .response(answer.getResponse())
                .status(answer.getStatus())
                .isPublic(answer.isPublic())
                .build();
        catalystOpenQuestionAnswerRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public long countAllRespondentAnswers(@NonNull final SurveyId surveyId, @NonNull final RespondentId respondentId) {
        return countLikertRespondentAnswers(surveyId, respondentId)
                + countOpenRespondentAnswers(surveyId, respondentId);
    }

    @Transactional(readOnly = true)
    public long countOpenRespondentAnswers(@NonNull final SurveyId surveyId, @NonNull final RespondentId respondentId) {
        return catalystOpenQuestionAnswerRepository.countAllByIdSurveyIdAndIdRespondentId(surveyId.getValue(), respondentId.getValue());
    }

    @Transactional(readOnly = true)
    public long countLikertRespondentAnswers(@NonNull final SurveyId surveyId, @NonNull final RespondentId respondentId) {
        return likerQuestionAnswerRepository.countAllByIdSurveyIdAndIdRespondentId(surveyId.getValue(), respondentId.getValue());
    }

    @Transactional(readOnly = true)
    public long countAnsweredParameters(@NonNull final SurveyId surveyId, @NonNull final RespondentId respondentId) {
        return parameterAnswerRepository.countByIdSurveyIdAndIdRespondentIdAndTypeIs(
                surveyId.getValue(),
                respondentId.getValue(),
                ParameterAnswerType.ITEM);
    }

    @NonNull
    @Transactional(readOnly = true)
    public Set<LikertQuestionAnswer> getLikertAnswers(@NonNull final SurveyId surveyId, @NonNull final RespondentId respondentId) {
        return likerQuestionAnswerRepository.findAllByIdSurveyIdAndIdRespondentId(surveyId.getValue(), respondentId.getValue())
                .stream()
                .map(e -> LikertQuestionAnswer.builder()
                        .id(new QuestionId(e.getId().getQuestionId()))
                        .catalystId(new CatalystId(e.getCatalystId()))
                        .status(e.getStatus())
                        .response(e.getResponse())
                        .rate(e.getRate())
                        .build())
                .collect(collectingAndThen(toSet(), Collections::unmodifiableSet));
    }

    @Transactional
    public void saveAnswer(@NonNull final LikertQuestionAnswer answer, @NonNull final SurveyId surveyId, @NonNull final RespondentId respondentId) {
        final QuestionAnswerId id = new QuestionAnswerId(surveyId.getValue(), respondentId.getValue(), answer.getId().getValue());
        final LikertQuestionAnswerEntity entity = likerQuestionAnswerRepository.findById(id)
                .orElse(LikertQuestionAnswerEntity.builder()
                        .id(id)
                        .catalystId(answer.getCatalystId().getValue())
                        .build());

        entity.setStatus(answer.getStatus());
        entity.setResponse(answer.getResponse());

        likerQuestionAnswerRepository.save(entity);
    }

    @Transactional
    public void saveRating(@NonNull final LikertQuestionAnswer answer, @NonNull final SurveyId surveyId, @NonNull final RespondentId respondentId) {
        if (answer.getRate() < 0 || answer.getRate() > 5) {
            throw new SurveyException("Invalid rate value");
        }

        final QuestionAnswerId id = new QuestionAnswerId(surveyId.getValue(), respondentId.getValue(), answer.getId().getValue());
        final LikertQuestionAnswerEntity entity = likerQuestionAnswerRepository.findById(id)
                .orElse(LikertQuestionAnswerEntity.builder()
                        .id(id)
                        .catalystId(answer.getCatalystId().getValue())
                        .status(AnswerStatus.NOT_ANSWERED)
                        .build());

        entity.setRate(answer.getRate());

        likerQuestionAnswerRepository.save(entity);
    }

    @NonNull
    @Transactional(readOnly = true)
    public Collection<Respondent> getParametersAnswers(@NonNull final SurveyId surveyId, @NonNull final List<RespondentFilter> filters) {
        final BooleanBuilder filter = new BooleanBuilder(QParameterAnswerEntity.parameterAnswerEntity.survey.id.eq(surveyId.getValue()))
                .and(QParameterAnswerEntity.parameterAnswerEntity.type.eq(ParameterAnswerType.ITEM)) // There always should be only one last child selected with ITEM type
                .and(createRespondentFilters(surveyId, filters));

        return respondentAssembler.from(StreamSupport.stream(parameterAnswerRepository.findAll(filter).spliterator(), false)
                .collect(groupingBy(ParameterAnswerEntity::getRespondent)));
    }

    @NonNull
    @Transactional(readOnly = true)
    public Set<RespondentId> getAnsweredRespondentIds(@NonNull final SurveyId surveyId, @NonNull final List<RespondentFilter> filters) {
        if (filters.isEmpty()) {
            return getAnsweredRespondentIds(surveyId);
        } else {
            final BooleanBuilder filter = new BooleanBuilder(QParameterAnswerEntity.parameterAnswerEntity.survey.id.eq(surveyId.getValue()))
                    .and(QParameterAnswerEntity.parameterAnswerEntity.type.eq(ParameterAnswerType.ITEM)) // There always should be only one last child selected with ITEM type
                    .and(QParameterAnswerEntity.parameterAnswerEntity.respondent.state.eq(SurveyRespondentState.ANSWERED))
                    .and(createRespondentFilters(surveyId, filters));

            return StreamSupport.stream(parameterAnswerRepository.findAll(filter).spliterator(), false)
                    .collect(groupingBy(e -> new RespondentId(e.getId().getRespondentId())))
                    .keySet();
        }
    }

    @NonNull
    @Transactional(readOnly = true)
    public Set<RespondentId> getAnsweredRespondentIds(@NonNull final SurveyId surveyId) {
        final QSurveyRespondent respondent = QSurveyRespondent.surveyRespondent;

        final BooleanBuilder filter = new BooleanBuilder(respondent.surveyId.eq(surveyId.getValue())
                .and(respondent.state.eq(SurveyRespondentState.ANSWERED)));

        return StreamSupport.stream(surveyRespondentRepository.findAll(filter).spliterator(), false)
                .map(v -> new RespondentId(v.getId()))
                .collect(toSet());
    }

    @NonNull
    private Map<QuestionId, List<String>> from(@NonNull final List<CatalystOpenQuestionAnswerEntity> answers) {
        return answers.stream()
                .collect(groupingBy(v -> v.getId().getQuestionId()))
                .entrySet().stream()
                .collect(collectingAndThen(toMap(
                        e -> new QuestionId(e.getKey()),
                        e -> e.getValue().stream()
                                .map(CatalystOpenQuestionAnswerEntity::getResponse)
                                .collect(collectingAndThen(toList(), Collections::unmodifiableList))), Collections::unmodifiableMap));
    }

    @NonNull
    private BooleanBuilder createRespondentFilters(@NonNull final SurveyId surveyId, @NonNull final List<RespondentFilter> filters) {
        final BooleanBuilder filter = new BooleanBuilder();

        if (!filters.isEmpty()) {
            final QParameterAnswerEntity answers = new QParameterAnswerEntity("a");

            for (final RespondentFilter respondentFilter : filters) {
                if (respondentFilter instanceof RespondentParameterFilter) {
                    final List<UUID> parameterIds = ((RespondentParameterFilter) respondentFilter).getValues();

                    filter.and(JPAExpressions.selectOne()
                            .from(answers)
                            .where(answers.respondent.id.eq(QParameterAnswerEntity.parameterAnswerEntity.respondent.id)
                                    .and(answers.id.surveyId.eq(surveyId.getValue()).and(answers.id.parameterId.in(parameterIds))))
                            .exists());
                }
            }
        }

        return filter;
    }

    @NonNull
    @Transactional(readOnly = true)
    public Collection<ParameterItemAnswer> getParametersAnswers(@NonNull final SurveyId surveyId, @NonNull final GuestId guestId) {
        return parameterAnswerAssembler.fromGuestAnswers(
                guestParameterAnswerRepository.findAllByIdSurveyIdAndIdGuestIdAndTypeIs(surveyId.getValue(),
                        guestId.getValue(),
                        ParameterAnswerType.ITEM));
    }

    @NonNull
    @Transactional(readOnly = true)
    public Collection<ParameterItemAnswer> getParametersAnswers(@NonNull final SurveyId surveyId, @NonNull final RespondentId respondentId) {
        return parameterAnswerAssembler.fromRespondentAnswers(
                parameterAnswerRepository.findAllByIdSurveyIdAndIdRespondentIdAndTypeIs(surveyId.getValue(),
                        respondentId.getValue(),
                        ParameterAnswerType.ITEM));
    }

    @Transactional
    public void saveAnswer(@NonNull final Parameter answer, @NonNull final SurveyId surveyId, @NonNull final GuestId guestId) {
        if (answer instanceof ParameterItem) {
            final ParameterItem parameter = (ParameterItem) answer;
            final ParameterId parameterId = Objects.requireNonNull(parameter.getId());
            final ParameterId parentId = Objects.requireNonNull(parameter.getParent()).getId();
            final ParameterId rootId = Objects.requireNonNull(parameter.getRoot()).getId();

            final ImmutableList.Builder<GuestParameterAnswerEntity> answers = ImmutableList.builder();
            final GuestParameterAnswerEntity item = GuestParameterAnswerEntity.builder()
                    .id(new GuestParameterAnswerId(surveyId.getValue(), guestId.getValue(), parameterId.getValue()))
                    .type(ParameterAnswerType.ITEM)
                    .parentId(parentId.getValue())
                    .rootId(rootId.getValue())
                    .build();
            answers.add(item);

            Parameter parent = parameterUtils.getParent(parameter);
            while (parent != null) {
                answers.add(getParentEntity(parent, surveyId.getValue(), guestId, rootId.getValue()));
                parent = parameterUtils.getParent(parent);
            }

            guestParameterAnswerRepository.deleteAllByIdSurveyIdAndIdGuestIdAndRootId(surveyId.getValue(), guestId.getValue(), rootId.getValue());
            guestParameterAnswerRepository.saveAll(answers.build());
        } else {
            throw new SurveyException("Can only answer to the last child (with no children)");
        }
    }

    @Transactional
    public void saveAnswer(@NonNull final Parameter answer, @NonNull final SurveyId surveyId,
                           @NonNull final RespondentId respondentId) {
        if (answer instanceof ParameterItem) {
            final ParameterItem parameter = (ParameterItem) answer;
            final ParameterId parameterId = Objects.requireNonNull(parameter.getId());
            final ParameterId parentId = Objects.requireNonNull(parameter.getParent()).getId();
            final ParameterId rootId = Objects.requireNonNull(parameter.getRoot()).getId();

            final ImmutableList.Builder<ParameterAnswerEntity> answers = ImmutableList.builder();
            final ParameterAnswerEntity item = ParameterAnswerEntity.builder()
                    .id(new ParameterAnswerId(surveyId.getValue(), respondentId.getValue(), parameterId.getValue()))
                    .type(ParameterAnswerType.ITEM)
                    .parentId(parentId.getValue())
                    .rootId(rootId.getValue())
                    .build();
            answers.add(item);

            Parameter parent = parameterUtils.getParent(parameter);
            while (parent != null) {
                answers.add(getParentEntity(parent, surveyId.getValue(), respondentId, rootId.getValue()));
                parent = parameterUtils.getParent(parent);
            }

            parameterAnswerRepository.deleteAllByIdSurveyIdAndIdRespondentIdAndRootId(surveyId.getValue(), respondentId.getValue(), rootId.getValue());
            parameterAnswerRepository.saveAll(answers.build());
        } else {
            throw new SurveyException("Can only answer to the last child (with no children)");
        }
    }

    @NonNull
    private ParameterAnswerEntity getParentEntity(@NonNull final Parameter parameter,
                                                  @NonNull final UUID surveyId,
                                                  @NonNull final RespondentId respondentId,
                                                  @NonNull final UUID rootId) {
        final Parameter parent = parameterUtils.getParent(parameter);
        if (parent != null) {
            return ParameterAnswerEntity.builder()
                    .id(new ParameterAnswerId(surveyId, respondentId.getValue(), parameter.getId().getValue()))
                    .type(ParameterAnswerType.PARENT)
                    .parentId(parent.getId().getValue())
                    .rootId(rootId)
                    .build();
        } else {
            return ParameterAnswerEntity.builder()
                    .id(new ParameterAnswerId(surveyId, respondentId.getValue(), rootId))
                    .type(ParameterAnswerType.ROOT)
                    .parentId(null)
                    .rootId(null)
                    .build();
        }
    }

    @NonNull
    private GuestParameterAnswerEntity getParentEntity(@NonNull final Parameter parameter,
                                                  @NonNull final UUID surveyId,
                                                  @NonNull final GuestId guestId,
                                                  @NonNull final UUID rootId) {
        final Parameter parent = parameterUtils.getParent(parameter);
        if (parent != null) {
            return GuestParameterAnswerEntity.builder()
                    .id(new GuestParameterAnswerId(surveyId, guestId.getValue(), parameter.getId().getValue()))
                    .type(ParameterAnswerType.PARENT)
                    .parentId(parent.getId().getValue())
                    .rootId(rootId)
                    .build();
        } else {
            return GuestParameterAnswerEntity.builder()
                    .id(new GuestParameterAnswerId(surveyId, guestId.getValue(), rootId))
                    .type(ParameterAnswerType.ROOT)
                    .parentId(null)
                    .rootId(null)
                    .build();
        }
    }
}
