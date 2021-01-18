package fi.sangre.renesans.application.merge;

import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.application.model.discussion.DiscussionQuestion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Slf4j

@Component
public class DiscussionQuestionMerger {
    @NonNull
    public List<DiscussionQuestion> combine(@NonNull final List<DiscussionQuestion> existing, @Nullable final List<DiscussionQuestion> input) {
        if (input == null) {
            return ImmutableList.copyOf(existing);
        } else {
            //TODO: implement
            return ImmutableList.of();
        }
    }
}
