create table if not exists survey_guest
(
    id uuid primary key not null,
    survey_id uuid not null
        constraint guest_survey_id_fkey
            references survey,
    email text not null,
    invitation_hash text not null,
    invitation_error text null,
    consent boolean default false not null,
    state varchar(20) default 'INVITING' not null,
    archived boolean default false not null,
    ctm timestamp default CURRENT_TIMESTAMP not null,
    mtm timestamp default CURRENT_TIMESTAMP not null
);

create unique index if not exists survey_guest_survey_id_id_uidx
    on survey_guest(survey_id, id);

create unique index if not exists survey_guest_survey_id_email_uidx
    on survey_guest(survey_id, email) where archived is false;

create index if not exists survey_guest_id_archived_idx
    on survey_guest(id, archived);

create index if not exists survey_guest_survey_id_archived_idx
    on survey_guest(survey_id, archived);

create index if not exists survey_guest_invitation_hash_archived_idx
    on survey_guest(invitation_hash, archived);

create table if not exists guest_parameter_answer
(
    survey_id uuid not null
        constraint guest_parameter_answer_survey_id_fkey
            references survey,
    guest_id uuid not null
        constraint guest_parameter_answer_guest_id_fkey
            references survey_guest,
    parameter_id uuid not null,
    type VARCHAR(10) not null,
    parent_id uuid null,
    root_id uuid null,
    answer_time timestamp default CURRENT_TIMESTAMP not null
);

create unique index if not exists parameter_guest_answer_survey_id_guest_id_parameter_id_uidx
    on guest_parameter_answer(survey_id, guest_id, parameter_id);

create index if not exists parameter_guest_answer_survey_id_guest_id_idx
    on guest_parameter_answer(survey_id, guest_id);

create index if not exists parameter_guest_answer_survey_id_guest_id_type_idx
    on guest_parameter_answer(survey_id, guest_id, type);

