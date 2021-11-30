alter table dialogue_topic_question
	add title text;

alter table dialogue_comment_like
    add survey_id uuid;

alter table dialogue_comment_like
    add constraint dialogue_comment_like_survey
        foreign key (survey_id) references survey;

alter table dialogue_question_like
    add survey_id uuid;

alter table dialogue_question_like
    add constraint dialogue_comment_like_survey
        foreign key (survey_id) references survey;

alter table dialogue_comment
    add survey_id uuid;

alter table dialogue_comment
    add constraint dialogue_comment_survey
        foreign key (survey_id) references survey;