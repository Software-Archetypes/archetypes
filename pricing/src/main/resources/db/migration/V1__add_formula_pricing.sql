create table formula_pricing
(
    id              uuid primary key,
    creation_date   timestamp with time zone not null,
    name            text                     not null,
    function_logic  text                     not null,
    input_data_type text                     not null,
    input_data_json text                     not null
);