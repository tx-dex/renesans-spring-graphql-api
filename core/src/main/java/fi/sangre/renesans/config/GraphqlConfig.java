package fi.sangre.renesans.config;

import com.coxautodev.graphql.tools.SchemaParserDictionary;
import fi.sangre.renesans.graphql.output.parameter.*;
import fi.sangre.renesans.graphql.output.question.QuestionnaireLikertQuestionOutput;
import fi.sangre.renesans.graphql.output.question.QuestionnaireOpenQuestionOutput;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GraphqlConfig {
    @Bean
    public SchemaParserDictionary getSchemaParser() {
        final SchemaParserDictionary dictionary = new SchemaParserDictionary();

        // Unions
        dictionary.add("SurveyParameterItem", SurveyParameterItemOutput.class);

        dictionary.add("SurveyListParameter", SurveyListParameterOutput.class);
        dictionary.add("SurveyTreeParameter", SurveyTreeParameterOutput.class);

        dictionary.add("QuestionnaireLikertQuestion", QuestionnaireLikertQuestionOutput.class);
        dictionary.add("QuestionnaireOpenQuestion", QuestionnaireOpenQuestionOutput.class);

        dictionary.add("QuestionnaireListParameter", QuestionnaireListParameterOutput.class);
        dictionary.add("QuestionnaireTreeParameter", QuestionnaireTreeParameterOutput.class);

        return dictionary;
    }
}
