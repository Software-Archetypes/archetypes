DO $$
BEGIN
  CREATE ROLE availability_user NOSUPERUSER NOCREATEDB CREATEROLE INHERIT LOGIN PASSWORD '<TO_BE_FILLED>';
  EXCEPTION WHEN OTHERS THEN
  RAISE NOTICE 'not creating role availability_user -- it already exists';
END
$$;

CREATE SCHEMA IF NOT EXISTS availability;

GRANT USAGE ON SCHEMA availability TO availability_user