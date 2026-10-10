\set ON_ERROR_STOP on

BEGIN;

\ir schema.sql
\ir game_type.sql
\ir piece.sql
\ir task.sql

COMMIT;