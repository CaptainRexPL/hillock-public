## Database (postgresql) structure

```sql
CREATE TABLE public.failed_login_attempts (
	id bigserial NOT NULL,
	ip_address varchar(25) NOT NULL,
	email varchar(50) NULL,
	"timestamp" timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	CONSTRAINT failed_login_attempts_pkey PRIMARY KEY (id)
);

CREATE TABLE public.unathorized_attempts (
	id bigserial NOT NULL,
	ip_address varchar(25) NOT NULL,
	profile_id int8 NULL,
	"timestamp" timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	CONSTRAINT unathorized_attempts_pkey PRIMARY KEY (id)
);

CREATE TABLE public.invites (
	id bigserial NOT NULL,
	invite varchar(25) NOT NULL,
	userid int8 NOT NULL,
	redeemed bool DEFAULT false NOT NULL,
	CONSTRAINT invites_pkey PRIMARY KEY (id)
);

CREATE TABLE public.users (
	id int8 NOT NULL,
	username varchar(50) NOT NULL,
	email varchar(100) NOT NULL,
	emailverified bool DEFAULT false NOT NULL,
	hashedpassword varchar(256) NOT NULL,
	"role" varchar(20) NOT NULL,
	disabled bool DEFAULT false NOT NULL,
	inviteid int8 NOT NULL,
	pokerscore int4 DEFAULT 0 NOT NULL,
	discordid int8 NULL,
	last_login_ip varchar(25) NULL,
	last_login_timestamp timestamp NULL,
	prev_login_ip varchar(25) NULL,
	prev_login_timestamp timestamp NULL,
	last_failed_login_ip varchar(25) NULL,
	last_failed_login_timestamp timestamp NULL,
	login_count int8 DEFAULT 0 NOT NULL,
	failed_login_count int8 DEFAULT 0 NOT NULL,
	CONSTRAINT users_email_key UNIQUE (email),
	CONSTRAINT users_inviteid_key UNIQUE (inviteid),
	CONSTRAINT users_pkey PRIMARY KEY (id),
	CONSTRAINT users_username_key UNIQUE (username),
	CONSTRAINT invitecode FOREIGN KEY (inviteid) REFERENCES public.invites(id)
);
```