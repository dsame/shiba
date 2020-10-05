CREATE TABLE research
(
    application_id         VARCHAR,
    completed_at           TIMESTAMP WITHOUT TIME ZONE,
    county                 VARCHAR,
    time_to_complete       INTEGER,
    sentiment              VARCHAR,
    feedback               TEXT,
    flow                   VARCHAR,
    spoken_language        VARCHAR,
    written_language       VARCHAR,
    first_name             VARCHAR,
    last_name              VARCHAR,
    date_of_birth          DATE,
    sex                    VARCHAR,
    phone_number           VARCHAR,
    email                  VARCHAR,
    phone_opt_in           BOOLEAN,
    email_opt_in           BOOLEAN,
    zip_code               VARCHAR,
    snap                   boolean,
    cash                   boolean,
    housing                boolean,
    emergency              boolean,
    live_alone             BOOLEAN,
    money_made_last30_days NUMERIC(2),
    pay_rent_or_mortgage   BOOLEAN,    -- skip
    home_expenses_amount   NUMERIC(2),
    are_you_working        BOOLEAN,
    self_employment        BOOLEAN,    -- skip
    unearned_income        NUMERIC(2),
    household_size         INTEGER
);
