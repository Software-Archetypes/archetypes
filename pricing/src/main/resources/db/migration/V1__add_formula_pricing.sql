create table formula_pricing
(
    id              uuid primary key,
    create_date     timestamp with time zone not null,
    function_logic  text                     not null,
    input_data_type text                     not null
);