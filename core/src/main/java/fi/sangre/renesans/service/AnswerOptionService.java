package fi.sangre.renesans.service;

import fi.sangre.renesans.model.Question;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static fi.sangre.renesans.model.Question.QuestionType;

@RequiredArgsConstructor
@Slf4j

@Service
public class AnswerOptionService {

    public Integer getAnswerValue(Integer index, Question question) {
        if (index < 1 || index > 5) {
            throw new IndexOutOfBoundsException();
        }

        Integer value = null;
        if (question.getQuestionType() == QuestionType.DEFAULT) {
            value = index; // 12345
        } else if (question.getQuestionType() == QuestionType.INVERTED) {
            value = (getMaxAnswerValue() + 1) - index; // 54321
        } else if (question.getQuestionType() == QuestionType.DUAL) {
            value = index <= 3 ? (getMaxAnswerValue() + 1) - index : index; // 54345
        }

        return value;
    }

    public Integer getMaxAnswerValue() {
        return 5;
    }
}
