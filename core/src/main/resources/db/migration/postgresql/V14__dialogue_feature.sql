create table if not exists dialogue_topic
(
    id uuid not null
        constraint dialogue_topic_pk
            primary key,
    title text,
    survey_id uuid
        constraint dialogue_topic_survey
            references survey,
    active boolean default true not null,
    sort_order integer not null
);

create table if not exists dialogue_topic_question
(
    id uuid not null
        constraint dialogue_topic_question_pk
            primary key,
    dialogue_topic_id uuid
        constraint dialogue_topic_question_topic
            references dialogue_topic,
    active boolean default true not null,
    sort_order integer not null
);

create table if not exists dialogue_tip
(
    id uuid not null
        constraint dialogue_tip_pk
            primary key,
    dialogue_topic_id uuid
        constraint dialogue_tip_topic
            references dialogue_topic,
    text text
);

create table if not exists dialogue_comment
(
    id uuid not null
        constraint dialogue_comment_pk
            primary key,
    dialogue_topic_question_id uuid
        constraint dialogue_comment_topic_question
            references dialogue_topic_question,
    parent_id uuid
        constraint dialogue_comment_parent
            references dialogue_comment,
    color text not null,
    text text
);

create table if not exists dialogue_like
(
    id uuid not null
        constraint dialogue_like_pk
            primary key,
    dialogue_comment_id uuid
        constraint dialogue_like_comment
            references dialogue_comment,
    survey_respondent_id uuid
        constraint dialogue_like_survey_respondent
            references survey_respondent
);

