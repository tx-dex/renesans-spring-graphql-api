package fi.sangre.renesans.config;

import com.coxautodev.graphql.tools.SchemaParserDictionary;
import fi.sangre.renesans.graphql.output.parameter.*;
import fi.sangre.renesans.graphql.output.question.QuestionnaireLikertQuestionOutput;
import fi.sangre.renesans.graphql.output.question.QuestionnaireOpenQuestionOutput;
import graphql.scalars.ExtendedScalars;
import graphql.scalars.alias.AliasedScalar;
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

    @Bean
    public AliasedScalar translationScalar() {
        return ExtendedScalars.newAliasedScalar("Translation")
                        .aliasedScalar(ExtendedScalars.Json)
                        .description("Scalar for keeping translation data of the app")
                        .build();
    }

    @Bean
    public AliasedScalar questionDriverWeightsScalar() {
        return ExtendedScalars.newAliasedScalar("QuestionDriverWeights")
                .aliasedScalar(ExtendedScalars.Json)
                .description("Json map for question driver weights")
                .build();
    }
}
