create sequence lists_id_seq
;

create sequence next_block_id_seq
;

create table workers
(
	address bytea not null
		constraint warkers_pkey
			primary key,
	balance double precision,
	shares double precision
)
;

create table shares
(
	blob bigint not null,
	mined_date bigint not null,
	block_id integer not null
)
;

create unique index shares_blob_mined_date_block_id_uindex
	on shares (blob, mined_date, block_id)
;

create index shares_block_id_index
	on shares (block_id)
;

create table tmp_wallets
(
	list_id integer,
	wallet_id integer,
	balance_change double precision
)
;

create table lists
(
	id serial not null,
	type integer,
	features integer,
	block_id integer,
	item bytea,
	receivers bytea
)
;

create table next_block
(
	id serial not null
		constraint next_block_pkey
			primary key,
	block_id integer not null,
	data bytea not null,
	hash bytea not null,
	insert_date timestamp default now() not null
)
;

