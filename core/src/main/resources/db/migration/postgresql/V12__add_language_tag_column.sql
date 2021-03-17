alter table survey_guest
    add column if not exists language_tag varchar(2) not null default 'en';

alter table survey_respondent
    add column if not exists language_tag varchar(2) not null default 'en';