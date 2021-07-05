package fi.sangre.renesans.application.assemble;

import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.application.model.discussion.DiscussionQuestion;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.application.utils.MultilingualUtils;
import fi.sangre.renesans.graphql.input.discussion.DiscussionQuestionInput;
import fi.sangre.renesans.persistence.model.metadata.discussion.DiscussionQuestionMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j

@Component
public class DiscussionQuestionAssembler {
    private final MultilingualUtils multilingualUtils;

    @NonNull
    public List<DiscussionQuestion> fromInput(@NonNull final List<DiscussionQuestionInput> input, @NonNull final String languageTag) {
        return input.stream()
                .map(question -> DiscussionQuestion.builder()
                        .id(Optional.ofNullable(question.getId())
                                .map(QuestionId::new)
                                .orElse(null))
                        .title(multilingualUtils.create(question.getTitle(), languageTag))
                        .active(question.getActive())
                        .build())
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));

    }

    @NonNull
    public List<DiscussionQuestion> fromMetadata(@Nullable final List<DiscussionQuestionMetadata> metadata) {
        if (metadata == null) {
            return ImmutableList.of();
        } else {
            return metadata.stream()
                    .map(question -> DiscussionQuestion.builder()
                            .id(new QuestionId(question.getId()))
                            .title(multilingualUtils.create(question.getTitles()))
                            .active(Boolean.TRUE.equals(question.getActive()))
                            .createdDate(question.getCreatedDate())
                            .build())
                    .collect(collectingAndThen(toList(), Collections::unmodifiableList));
        }


    }

}
