package fi.sangre.renesans.dto;

import graphql.GraphQLException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocaleDto {
    private String validation_email;
    private String validation_age;
    private String validation_age_min;
    private String validation_length;
    private String validation_experience;
    private String background_position_options_freelancer;
    private String questionnaire_title;
    private String questionnaire_start_linkText;
    private String results_contactDescription;
    private String background_industry_options_retail;
    private String results_startOverLink;
    private String privacy_policy_accept;
    private String html_title_default;
    private String statistics_totalGrowthIndex;
    private String background_experience;
    private String background_industry_options_financeInsurance;
    private String background_segment_options_other;
    private String background_industry;
    private String html_title_finish;
    private String validation_phoneNumber;
    private String validation_number;
    private String html_title_template;
    private String question_answerOption_always;
    private String background_gender;
    private String background_segment_options_businessToBusiness;
    private String validation_required;
    private String background_segment;
    private String background_industry_options_manufacturing;
    private String questionnaire_start_title;
    private String language_prompt;
    private String background_location;
    private String question_answerOption_rarely;
    private String questionnaire_submitDescription;
    private String questionnaire_submitLinkText;
    private String background_email;
    private String background_industry_options_manufacturingService;
    private String background_age;
    private String background_gender_options_male;
    private String background_industry_options_publicServices;
    private String background_name;
    private String html_title_questionnaire;
    private String ui_loading;
    private String ui_next;
    private String ui_back;
    private String background_position_options_management;
    private String background_position_options_employee;
    private String statistics_result_all;
    private String background_segment_options_businessToConsumer;
    private String background_industry_options_consulting;
    private String background_phone;
    private String questionnaire_submitError;
    private String background_required;
    private String questionnaire_background_title;
    private String background_industry_options_service;
    private String background_industry_options_other;
    private String html_title_background;
    private String background_position;
    private String background_gender_options_female;
    private String privacy_policy_full;
    private String statistics_result_filtered;
    private String privacy_policy_consent_label;
    private String background_industry_options_trading;
    private String results_contactLink;
    private String question_answerOption_never;
    private String privacy_policy_read_more;
    private String questionnaire_results_title;
    private String questionnaire_submitting;
    private String questionnaire_background_description;
    private String questionnaire_description;
    private String question_answerOption_sometimes;
    private String question_answerOption_often;
    private String privacy_policy_title;
    private String background_position_options_other;
    private String html_title_start;
    private String questionnaire_results_description;
    private String questionnaire_start_description;

    public LocaleDto(Map<String, String> data) {
        for (Map.Entry<String, String> entry : data.entrySet()) {
            try {
                Field field = this.getClass().getDeclaredField(entry.getKey()); //get the field by name
                field.set(this, entry.getValue()); // set the field's value for your object
            } catch (Exception e) {
                throw new GraphQLException("Invalid Field");
            }
        }
    }
}
