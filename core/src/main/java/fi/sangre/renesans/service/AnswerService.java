package fi.sangre.renesans.service;


import fi.sangre.renesans.graphql.input.AnswerInput;
import fi.sangre.renesans.model.Answer;
import fi.sangre.renesans.model.Question;
import fi.sangre.renesans.model.Respondent;
import fi.sangre.renesans.repository.AnswerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class AnswerService {
    @Autowired
    private AnswerRepository answerRepository;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private AnswerOptionService answerOptionService;

    public List<Answer> addAnswers(List<AnswerInput> answers, Respondent respondent) {
        List<Answer> answersList = new ArrayList<>();
        for (AnswerInput answerInput : answers) {
            Question question = questionService.getQuestion(answerInput.getQuestionId());

            Answer answer = Answer.builder()
                    .id(answerInput.getId())
                    .answerIndex(answerInput.getAnswerIndex())
                    .answerValue(answerOptionService.getAnswerValue(answerInput.getAnswerIndex(), question))
                    .respondent(respondent)
                    .question(question)
                    .build();

            answersList.add(answer);
        }
        return answerRepository.saveAll(answersList);
    }

    public List<Answer> getAnswers(Respondent respondent) {
        if (respondent != null) {
            return answerRepository.findByRespondent(respondent);
        }

        return new ArrayList<>();
    }

    public Date getAnswerTime(Respondent respondent) {
        if (respondent != null) {
            Answer answer = answerRepository.findOneByRespondent(respondent);
            if (answer != null) {
                return answer.getCreated();
            }
        }

        return null;
    }
}

