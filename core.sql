create sequence wallets_wallet_id_seq
;

create table blocks
(
	id integer not null
		constraint blocks_pkey
			primary key,
	mined_date bigint not null,
	difficulty bytea not null,
	core_version bytea not null,
	miner_id integer not null,
	previous_block_hash bytea not null,
	job_uuid bytea not null,
	current_block_hash bytea not null,
	blob bigint not null
)
;

create unique index blocks_current_block_hash_uindex
	on blocks (current_block_hash)
;

create table wallets
(
	wallet_id serial not null
		constraint wallets_pkey
			primary key,
	balance double precision not null,
	public_key bytea not null
)
;

