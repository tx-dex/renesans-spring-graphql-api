package fi.sangre.renesans.repository;

import fi.sangre.renesans.model.Answer;
import fi.sangre.renesans.model.Respondent;
import fi.sangre.renesans.statistics.StatisticsAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long>, QuerydslPredicateExecutor<Answer> {
    @PostFilter("hasPermission(filterObject.respondent.respondentGroup.customer, 'READ')")
    List<Answer> findByRespondent(Respondent respondent);

    @PreAuthorize("hasPermission(#respondent.respondentGroup.customer, 'READ')")
    Answer findOneByRespondent(@P("respondent") Respondent respondent);

    @PreAuthorize("isAuthenticated()")
    Long countByQuestionId(Long questionId);


    @Query("SELECT new fi.sangre.renesans.statistics.StatisticsAnswer(questionId, AVG(answerValue), MIN(answerValue), MAX(answerValue), COUNT(id)) FROM Answer WHERE questionId = :questionId AND respondent in :respondents GROUP BY questionId")
    StatisticsAnswer findAnswerStatisticsByQuestionAndRespondentsIn(@Param("questionId") Long questionId, @Param("respondents") List<Respondent> respondents);
}
