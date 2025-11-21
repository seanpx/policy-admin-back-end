-- Flyway V3: initialize client maintenance master (clntPF)
-- Mapping based on the provided client profile specification.

CREATE TABLE IF NOT EXISTS clntpf (
    clntpfx   VARCHAR(2)   NOT NULL,
    clntcoy   VARCHAR(1)   NOT NULL,
    clntnum   VARCHAR(8)   NOT NULL,
    validflag VARCHAR(1),
    clttype   VARCHAR(1),
    secuityno VARCHAR(24),
    surname   VARCHAR(30),
    givname   VARCHAR(40),
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
    cltdob    NUMERIC(8,0),
    cltdod    NUMERIC(8,0),
    cltstat   VARCHAR(2),
    cltmchg   VARCHAR(1),
    middl01   VARCHAR(30),
    middl02   VARCHAR(30),
    marryd    VARCHAR(1),
    tlxno     VARCHAR(16),
    faxno     VARCHAR(16),
    tgram     VARCHAR(16),
    birthp    VARCHAR(20),
    salutl    VARCHAR(8),
    trdt      NUMERIC(6,0),
    trtm      NUMERIC(6,0),
    natlty    VARCHAR(3),
    cltind    VARCHAR(1),
    race      VARCHAR(3),
    ctryorig  VARCHAR(3),
    zgstregno VARCHAR(15),
    zpnynnam  VARCHAR(20),
    zressts   VARCHAR(1),
    zemailadd VARCHAR(50),
    datime    TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ctrydesc  VARCHAR(30),
    mailing   VARCHAR(1),
    usrprf    VARCHAR(10),
    PRIMARY KEY (clntpfx, clntcoy, clntnum),
    CONSTRAINT uq_clntpf_client_key UNIQUE (clntpfx, clntcoy, clntnum)
);

-- Maintain datime (audit timestamp) automatically
CREATE OR REPLACE FUNCTION set_clntpf_datime() RETURNS trigger AS $$
BEGIN
    NEW.datime := CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_clntpf_datime ON clntpf;
CREATE TRIGGER trg_clntpf_datime
BEFORE INSERT OR UPDATE ON clntpf
FOR EACH ROW
EXECUTE FUNCTION set_clntpf_datime();

COMMENT ON TABLE clntpf IS 'Client Profile Table. Contains the detailed personal/corporate information of clients/advisors/dealers.';
