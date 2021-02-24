create sequence if not exists multilingual_phrase_key_seq;

create sequence if not exists  hibernate_sequence;

CREATE TABLE revinfo
(
    rev bigint primary key,
    revtstmp bigint
);

create table if not exists multilingual_key
(
	id bigserial primary key not null,
	key text default ('key'::text || nextval('multilingual_phrase_key_seq'::regclass)) not null
		constraint multilingual_key_key
			unique,
	ctm timestamp default CURRENT_TIMESTAMP not null,
	mtm timestamp default CURRENT_TIMESTAMP not null,
	archived boolean default false
);

create table if not exists multilingual_phrase
(
	id bigserial primary key not null,
	key_id bigint not null
		constraint fk_phrase_key_id
			references multilingual_key,
	message text not null,
	locale text not null,
	ctm timestamp default CURRENT_TIMESTAMP not null,
	mtm timestamp default CURRENT_TIMESTAMP not null,
	archived boolean default false,
	constraint multilingual_phrase_key_locale_key
		unique (key_id, locale)
);

create table if not exists respondent_option
(
	id bigserial primary key not null,
	index bigint,
	option_type integer,
	title_id bigint
		constraint fkj4yayumfg4diqbbyqb6y9y36p
			references multilingual_key,
	ctm timestamp default CURRENT_TIMESTAMP not null,
	mtm timestamp default CURRENT_TIMESTAMP not null,
	archived boolean default false
);

create table if not exists survey
(
	id uuid primary key not null,
	version bigint not null,
	state text not null,
	is_default boolean,
	description_id bigint
		constraint fk6ud7ylb9rxnffuearyax4xv26
			references multilingual_key,
	title_id bigint
		constraint fkeg83pbj5j3pqidoyji4hqh1tq
			references multilingual_key,
	metadata jsonb null,
	archived boolean default false,
    cuser bigint not null,
    muser bigint not null,
	ctm timestamp default CURRENT_TIMESTAMP not null,
	mtm timestamp default CURRENT_TIMESTAMP not null
);

create table if not exists survey_history
(
    rev bigint not null,
    rev_type smallint not null,
    id uuid not null,
    version bigint null,
    state text null,
    metadata jsonb null,
    muser bigint null,
    mtm timestamp null,
    constraint survey_history_pk
        primary key (rev, id)
);

create table if not exists survey_respondent
(
    id uuid primary key not null,
    survey_id uuid not null
        constraint respondent_survey_id_fkey
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

create unique index if not exists survey_respondent_survey_id_id_uidx
    on survey_respondent(survey_id, id);

create unique index if not exists survey_respondent_survey_id_email_uidx
    on survey_respondent(survey_id, email) where archived is false;

create index if not exists survey_respondent_id_archived_idx
    on survey_respondent(id, archived);

create index if not exists survey_respondent_survey_id_archived_idx
    on survey_respondent(survey_id, archived);

create index if not exists survey_respondent_invitation_hash_archived_idx
    on survey_respondent(invitation_hash, archived);

create table if not exists question_group
(
	id bigserial primary key not null,
	uuid uuid default gen_random_uuid() not null
        constraint catalyst_id_uidx
            unique,
	pdfname varchar(255),
	seq bigint,
	weight double precision,
	description_id bigint
		constraint fkiwjpq3ihnjsyhlaot7jw7sig8
			references multilingual_key,
	parent_id bigint
		constraint fk4rlkxunwygfke8j7f2vuokpqm
			references question_group,
	prescription_id bigint
		constraint fknsavroo6wa03hokgu7mcarras
			references multilingual_key,
	survey_id uuid
		constraint fk547jj0p7m6ay6ydytylmf1fxa
			references survey,
	title_id bigint
		constraint fkgo8gu89pwsh6086bdvg47h3aw
			references multilingual_key,
	archived boolean default false,
	ctm timestamp default CURRENT_TIMESTAMP not null,
	mtm timestamp default CURRENT_TIMESTAMP not null
);

create table if not exists likert_question_answer
(
    survey_id uuid not null
        constraint question_answer_survey_id_fkey
            references survey,
    respondent_id uuid not null
        constraint question_answer_respondent_id_fkey
            references survey_respondent,
    catalyst_id uuid not null,
    question_id uuid not null,
    rate integer null,
    status integer not null,
    likert_response integer null,
    answer_time timestamp default CURRENT_TIMESTAMP not null
);

create unique index if not exists question_answer__survey_id_respondent_id_question_id_uidx
    on likert_question_answer(survey_id, respondent_id, question_id);

create index if not exists question_answer__survey_id_respondent_id_idx
    on likert_question_answer(survey_id, respondent_id);


create table if not exists open_question_answer
(
    survey_id uuid not null
        constraint catalyst_answer_survey_id_fkey
            references survey,
    respondent_id uuid not null
        constraint catalyst_answer_respondent_id_fkey
            references survey_respondent,
    catalyst_id uuid not null,
    question_id uuid not null,
    status integer not null,
    is_public bool not null default false,
    open_response text null,
    answer_time timestamp default CURRENT_TIMESTAMP not null
);

create unique index if not exists catalyst_answer_survey_id_respondent_id_catalyst_id_uidx
    on open_question_answer(survey_id, respondent_id, catalyst_id);

create index if not exists catalyst_answer_survey_id_respondent_id_idx
    on open_question_answer(survey_id, respondent_id);

create table if not exists parameter_answer
(
    survey_id uuid not null
        constraint question_answer_survey_id_fkey
            references survey,
    respondent_id uuid not null
        constraint question_answer_respondent_id_fkey
            references survey_respondent,
    parameter_id uuid not null,
    type VARCHAR(10) not null,
    parent_id uuid null,
    root_id uuid null,
    answer_time timestamp default CURRENT_TIMESTAMP not null
);

create unique index if not exists parameter_answer_survey_id_respondent_id_parameter_id_uidx
    on parameter_answer(survey_id, respondent_id, parameter_id);

create index if not exists parameter_answer_survey_id_respondent_id_idx
    on parameter_answer(survey_id, respondent_id);

create index if not exists parameter_answer_survey_id_respondent_id_type_idx
    on parameter_answer(survey_id, respondent_id, type);


create table if not exists "user"
(
	id bigserial primary key not null,
	username text not null
		constraint user_username_key
			unique,
	email text not null
		constraint user_email_key
			unique,
	enabled boolean not null,
	first_name text,
	last_name text,
	password text,
	token_expired boolean not null
);

create index if not exists user_username_index
	on "user" (username);

create table if not exists role
(
	id bigserial primary key not null,
	name text,
	title text
);

create table if not exists privilege
(
	id bigserial primary key not null,
	name text
);

create table if not exists users_roles
(
	user_id bigint not null
		constraint users_roles_user_id_fkey
			references "user",
	role_id bigint not null
		constraint users_roles_role_id_fkey
			references role
);

create table if not exists roles_privileges
(
	role_id bigint not null
		constraint roles_privileges_role_id_fkey
			references role,
	privilege_id bigint not null
		constraint roles_privileges_privilege_id_fkey
			references privilege
);

create unique index if not exists  roles_privileges_role_id_privilege_id_uindex
    on data.roles_privileges (role_id, privilege_id);

create table if not exists segment
(
	id bigserial primary key not null,
	name text not null,
	archived boolean default false,
	ctm timestamp default CURRENT_TIMESTAMP not null,
	mtm timestamp default CURRENT_TIMESTAMP not null
);

-- TODO: rename to organization
create table if not exists customer
(
	id uuid primary key not null,
	name text not null,
	description text not null,
	created_by bigint not null,
	archived boolean default false,
	is_default boolean default false not null,
	segment_id bigint
		constraint customer_segment_id_fk
			references segment,
	ctm timestamp default CURRENT_TIMESTAMP not null,
	mtm timestamp default CURRENT_TIMESTAMP not null
);

create table if not exists organizations_surveys
(
    organization_id uuid not null
        constraint organization_surveys_mapping_organization_id_fk
            references customer(id),
    survey_id uuid not null
        constraint organization_surveys_mapping_survey_id_fk
            references survey(id)
);

create table if not exists customers_users
(
    customer_id uuid
      constraint customers_users_customer_id_fkey
          references customer,
    user_id bigint
      constraint customers_users_user_id_fkey
          references "user"
);

create index if not exists customer_name_index
    on customer (name);

create sequence if not exists question_seq_seq;

create table if not exists question
(
	id bigserial primary key not null,
	questiontype integer,
	seq bigint default nextval('question_seq_seq'::regclass) not null,
	questiongroup_id bigint
		constraint fkjv147vbjicsp0hyc3cgwbj65y
			references question_group,
	title_id bigint
		constraint fkm5lhvy4xso4fudysbrwdj9rju
			references multilingual_key,
	archived boolean default false,
	segment_id bigint
		constraint question_segment_id_fk
			references segment,
	customer_id uuid
		constraint question_customer_id_fk
			references customer,
	source_type smallint not null,
	ctm timestamp default CURRENT_TIMESTAMP not null,
	mtm timestamp default CURRENT_TIMESTAMP not null
);

create table if not exists weight
(
	id bigserial primary key not null,
	question_group_id bigint,
	question_id bigint
		constraint fklvvw0hhan61xo8997u79pdyio
			references question,
	weight double precision,
	archived boolean default false,
	ctm timestamp default CURRENT_TIMESTAMP not null,
	mtm timestamp default CURRENT_TIMESTAMP not null
);

create unique index if not exists weight_questionid_questiongroupid_uindex
	on weight (question_id, question_group_id);

create table if not exists respondent_group
(
	id char(36) primary key not null,
	description text,
	is_default boolean,
	title varchar(255),
	survey_id uuid
		constraint fkex0gg1swqq1dpee2go656yqxl
			references survey,
	customer_id uuid
		constraint fk_respondent_customer
			references customer,
	archived boolean default false,
	default_locale text default 'en'::text,
	ctm timestamp default CURRENT_TIMESTAMP not null,
	mtm timestamp default CURRENT_TIMESTAMP not null
);

create index if not exists respondentgroup_title_index
	on respondent_group (title);

create table if not exists respondent
(
	id char(36) primary key not null,
	age integer,
	consent boolean,
	country varchar(255),
	email varchar(255),
	experience integer,
	gender varchar(255),
	name varchar(255),
	phone varchar(255),
	industry_id bigint
		constraint fkgaq6jdyorlkddbjep6wuxvx8n
			references respondent_option,
	position_id bigint
		constraint fkid16unbm18ftck6uex4h1weev
			references respondent_option,
	respondentgroup_id char(36)
		constraint fkhs4hl3gybergvu3wfrnm5yfod
			references respondent_group,
	segment_id bigint
		constraint fkt4qxbo0q5m7650b5jvcoyys9a
			references respondent_option,
	archived boolean default false,
	invitation_hash varchar(255) default NULL::character varying,
	state text default 'FINISHED',
	locale text,
	answer_time timestamp,
	original_id char(36),
	ctm timestamp default CURRENT_TIMESTAMP not null,
	mtm timestamp default CURRENT_TIMESTAMP not null
);

create index if not exists age_idx
	on respondent (age);

create index if not exists country_idx
	on respondent (country);

create index if not exists gender_idx
	on respondent (gender);

create index if not exists industry_idx
	on respondent (industry_id);

create index if not exists position_idx
	on respondent (position_id);

create index if not exists respondentgroup_idx
	on respondent (respondentgroup_id);

create index if not exists segment_idx
	on respondent (segment_id);

create index if not exists respondent_name_index
	on respondent (name);

create table if not exists discussion_actor
(
    id bigserial primary key not null,
    survey_id uuid not null,
    respondent_id uuid not null
);

create index if not exists discussion_actor__survey_id
    on discussion_actor(survey_id);

create unique index if not exists discussion_actor__survey_id_respondent_id
    on discussion_actor(survey_id, respondent_id);

create table if not exists discussion_comment
(
    id uuid primary key not null,
    survey_id uuid not null,
    question_id uuid not null,
    actor_id bigint not null references discussion_actor,
    text text not null,
    ctm timestamp default CURRENT_TIMESTAMP not null,
    mtm timestamp default CURRENT_TIMESTAMP not null
);

create index if not exists discussion_comment__survey_id
    on discussion_comment(survey_id);

create index if not exists discussion_actor__survey_id_question_id
    on discussion_comment(survey_id, question_id);

create index if not exists discussion_actor__survey_id_question_id_ctm
    on discussion_comment(survey_id, question_id, ctm);

create table if not exists discussion_like
(
    id uuid primary key not null,
    survey_id uuid not null,
    comment_id uuid not null references discussion_comment,
    actor_id bigint not null references discussion_actor
);

create index if not exists discussion_like__survey_id
    on discussion_like(survey_id);

create index if not exists discussion_like__survey_id_comment_id
    on discussion_like(survey_id, comment_id);

create unique index if not exists discussion_like__survey_id_comment_id_actor_id
    on discussion_like(survey_id, comment_id, actor_id);


create table if not exists answer
(
	id bigserial primary key not null,
	answer_index integer,
	answer_value integer,
	question_id bigint
		constraint fk8frr4bcabmmeyyu60qt7iiblo
			references question,
	respondent_id char(36)
		constraint fk6dq0hyl3n6exx70ypckm4x44r
			references respondent,
	archived boolean default false,
	ctm timestamp default CURRENT_TIMESTAMP not null,
	mtm timestamp default CURRENT_TIMESTAMP not null
);

create index if not exists respondent_idx
	on answer (respondent_id);

create index if not exists question_idx
	on answer (question_id);

create table if not exists respondent_group_question_groups
(
	respondent_group_id char(36) not null
		constraint respondent_group_question_groups_respondent_group_id_fkey
			references respondent_group,
	question_group_id bigserial not null
		constraint respondent_group_question_groups_question_group_id_fkey
			references question_group,
	constraint respondent_group_question_groups_pkey
		primary key (respondent_group_id, question_group_id)
);

create table if not exists segment_question_group_phrase
(
	id bigserial primary key not null,
	segment_id bigint
		constraint segment_question_group_phrase_segment_id_fkey
			references segment,
	question_group_id bigint not null
		constraint segment_question_group_phrase_question_group_id_fkey
			references question_group,
	title_id bigint not null
		constraint segment_question_group_phrase_title_id_fkey
			references multilingual_key,
	ctm timestamp default CURRENT_TIMESTAMP not null,
	mtm timestamp default CURRENT_TIMESTAMP not null
);

create table if not exists customer_driver_weights
(
	customer_id uuid not null,
	driver_id bigint not null,
	weight numeric default 0.5 not null,
	constraint customer_driver_weights_customer_id_driver_id_key
		unique (customer_id, driver_id)
);

