package fi.sangre.renesans.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import fi.sangre.renesans.model.Answer;
import fi.sangre.renesans.model.Question;
import org.springframework.stereotype.Component;

@Component
public class AnswerResolver implements GraphQLResolver<Answer> {
    public Question getQuestion(Answer answer) {
        // todo fix
        return null;
    }
}
