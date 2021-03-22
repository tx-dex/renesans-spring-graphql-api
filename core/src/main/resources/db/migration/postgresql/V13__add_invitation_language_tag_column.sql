alter table survey_guest
    add column if not exists invitation_language_tag varchar(2) not null default 'en';

alter table survey_respondent
    add column if not exists invitation_language_tag varchar(2) not null default 'en';

alter table survey_guest alter column language_tag drop not null;
alter table survey_respondent alter column language_tag drop not null;
