CREATE TABLE admin (
    id          BIGSERIAL PRIMARY KEY,
    login_id    VARCHAR   NOT NULL UNIQUE,
    password    VARCHAR   NOT NULL,
    name        VARCHAR   NOT NULL,
    role        VARCHAR   NOT NULL,
    created_at  TIMESTAMP,
    modified_at TIMESTAMP
);

CREATE TABLE member (
    id                BIGSERIAL PRIMARY KEY,
    name              VARCHAR   NOT NULL,
    student_num       VARCHAR   NOT NULL UNIQUE,
    profile_photo     VARCHAR,
    total_stamp_count INTEGER   NOT NULL DEFAULT 0,
    joined_year       INTEGER   NOT NULL,
    joined_semester   INTEGER   NOT NULL,
    withdrawn_at      DATE,
    created_at        TIMESTAMP,
    modified_at       TIMESTAMP
);

CREATE TABLE stamp (
    id             BIGSERIAL PRIMARY KEY,
    member_id      BIGINT REFERENCES member (id),
    stamp_year     INTEGER,
    stamp_semester INTEGER,
    feed_stamp_num INTEGER,
    ex_stamp_num   INTEGER,
    present_count  INTEGER,
    created_at     TIMESTAMP,
    modified_at    TIMESTAMP
);

CREATE TABLE give_stamp (
    id          BIGSERIAL PRIMARY KEY,
    member_id   BIGINT REFERENCES member (id),
    admin_id    BIGINT REFERENCES admin (id),
    stamp_kind  VARCHAR,
    delta       INTEGER,
    created_at  TIMESTAMP,
    modified_at TIMESTAMP
);
