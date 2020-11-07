package fi.sangre.renesans.service;

import fi.sangre.renesans.model.AnswerOption;
import fi.sangre.renesans.model.MultilingualKey;
import fi.sangre.renesans.model.Question;
import fi.sangre.renesans.repository.MultilingualKeyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static fi.sangre.renesans.model.Question.QuestionType;

//TODO: refactor, remove unused (in the WEC-342 client all inverted questions was changed to default ones)
@Service
public class AnswerOptionService {
    private static Map<Question.QuestionType, List<AnswerOption>> answerOptionsByQuestionType;

    private AnswerOption getOption(Integer index, MultilingualKey title, Integer value) {
        return AnswerOption.builder()
                .index(index)
                .title(title)
                .titleId(title.getId())
                .value(value)
                .build();
    }
    @Autowired
    public AnswerOptionService(MultilingualKeyRepository multilingualKeyRepository) {

        MultilingualKey KEY_NEVER = multilingualKeyRepository.findByKey("question_answerOption_never");
        MultilingualKey KEY_RARELY = multilingualKeyRepository.findByKey("question_answerOption_rarely");
        MultilingualKey KEY_SOMETIMES = multilingualKeyRepository.findByKey("question_answerOption_sometimes");
        MultilingualKey KEY_OFTEN = multilingualKeyRepository.findByKey("question_answerOption_often");
        MultilingualKey KEY_ALWAYS = multilingualKeyRepository.findByKey("question_answerOption_always");

        answerOptionsByQuestionType = new HashMap<>();
        answerOptionsByQuestionType.put(QuestionType.DEFAULT, new LinkedList<>(Arrays.asList(
                getOption(1, KEY_NEVER, 1),
                getOption(2, KEY_RARELY, 2),
                getOption(3, KEY_SOMETIMES, 3),
                getOption(4, KEY_OFTEN, 4),
                getOption(5, KEY_ALWAYS, 5)
        )));

        answerOptionsByQuestionType.put(QuestionType.INVERTED, new LinkedList<>(Arrays.asList(
                getOption(1, KEY_NEVER, 5),
                getOption(2, KEY_RARELY, 4),
                getOption(3, KEY_SOMETIMES, 3),
                getOption(4, KEY_OFTEN, 2),
                getOption(5, KEY_ALWAYS, 1)
        )));

        answerOptionsByQuestionType.put(QuestionType.DUAL, new LinkedList<>(Arrays.asList(
                getOption(1, KEY_NEVER, 5),
                getOption(2, KEY_RARELY, 4),
                getOption(3, KEY_SOMETIMES, 3),
                getOption(4, KEY_OFTEN, 4),
                getOption(5, KEY_ALWAYS, 5)
        )));
    }


    public List<AnswerOption> getAnswerOptions(Question question) {
        return answerOptionsByQuestionType.get(question.getQuestionType());
    }

    public Integer getAnswerValue(Integer index, Question question) {
        if (index < 1 && index > 5) {
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
