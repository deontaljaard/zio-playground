create table if not exists country (
    code character(3) not null primary key,
    name varchar not null,
    continent varchar not null,
    region varchar not null
);
