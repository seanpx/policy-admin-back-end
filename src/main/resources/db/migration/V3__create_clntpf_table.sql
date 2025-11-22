-- Flyway V3: initialize client maintenance master (clnt_main)
-- Mapping based on the provided client profile specification.

CREATE TABLE IF NOT EXISTS clnt_main (
    clntnum   BIGSERIAL PRIMARY KEY,
    validflag VARCHAR(1),
    clttype   VARCHAR(1),
    clntid_typ VARCHAR(3) NOT NULL,
    clntid_no  VARCHAR(50) NOT NULL,
    surname   VARCHAR(100),
    givname   VARCHAR(100),
    salut     VARCHAR(6),
    cltsex    VARCHAR(1),
    cltaddr01 VARCHAR(30),
    cltaddr02 VARCHAR(30),
    cltaddr03 VARCHAR(30),
    cltaddr04 VARCHAR(30),
    cltaddr05 VARCHAR(30),
    cltpcode  VARCHAR(10),
    ctrycode  VARCHAR(3),
    addrtype  VARCHAR(1),
    cltphone01 VARCHAR(16),
    cltphone02 VARCHAR(16),
    occpcode  VARCHAR(4),
    statcode  VARCHAR(2),
    cltdob    DATE,
    cltdod    DATE,
    cltstat   VARCHAR(2),
    cltmchg   VARCHAR(1),
    marryd    VARCHAR(1),
    tlxno     VARCHAR(16),
    birthp    VARCHAR(20),
    salutl    VARCHAR(8),
    trdt      DATE,
    trtm      TIME,
    natlty    VARCHAR(3),
    cltind    VARCHAR(1),
    race      VARCHAR(3),
    ctryorig  VARCHAR(3),
    zgstregno VARCHAR(15),
    zpnynnam  VARCHAR(20),
    zressts   VARCHAR(1),
    zemailadd VARCHAR(100),
    datime    TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ctrydesc  VARCHAR(30),
    mailing   VARCHAR(1),
    usrprf    VARCHAR(10),

    CONSTRAINT uq_clnt_main_id UNIQUE (clntid_typ, clntid_no)
);

-- Maintain datime (audit timestamp) automatically
CREATE OR REPLACE FUNCTION set_clnt_main_datime() RETURNS trigger AS $$
BEGIN
    NEW.datime := CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_clnt_main_datime ON clnt_main;
CREATE TRIGGER trg_clnt_main_datime
BEFORE INSERT OR UPDATE ON clnt_main
FOR EACH ROW
EXECUTE FUNCTION set_clnt_main_datime();

COMMENT ON TABLE clnt_main IS 'Client Profile Table. Contains the detailed personal/corporate information of clients/advisors/dealers.';
