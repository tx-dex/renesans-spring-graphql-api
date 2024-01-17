create table if not exists survey_language (
    survey_id uuid not null,
    language varchar(2) not null,
    primary key (survey_id, language),
    constraint fk_survey
        foreign key (survey_id) references survey(id)
);