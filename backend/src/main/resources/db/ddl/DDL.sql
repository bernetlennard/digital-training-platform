-- =====================================================================
-- Digitale Plattform zur Trainingsverwaltung
-- Flyway-Migration V1: Initiales Datenbankschema (PostgreSQL)
--
-- Grundlage: Diplomarbeit, Kapitel «Konzeption > Datenmodell»
--   ERM, Löschverhalten, Eindeutigkeit, Prüfregeln, Indizes,
--   Stammdatenwerte
--
-- Konventionen
--   - Namen Englisch in snake_case
--   - «user» und «set» sind reserviert -> app_user, exercise_set
--   - Constraints/Indizes: Präfix + Tabelle + Spalte
--     (pk_, fk_, uq_, ck_, ix_)
--   - Alle Spalten NOT NULL, ausser explizit NULL
--   - Zeitpunkte als TIMESTAMPTZ (Zeitzone Europe/Zurich in der App),
--     geplantes Datum als DATE
-- =====================================================================


-- ---------------------------------------------------------------------
-- Stammdaten (feste IDs, keine IDENTITY)
-- ---------------------------------------------------------------------

CREATE TABLE muscle_group (
                              id   INTEGER     NOT NULL,
                              name VARCHAR(50) NOT NULL,

                              CONSTRAINT pk_muscle_group_id   PRIMARY KEY (id),
                              CONSTRAINT uq_muscle_group_name UNIQUE (name)
);

CREATE TABLE session_status (
                                id   INTEGER     NOT NULL,
                                name VARCHAR(20) NOT NULL,

                                CONSTRAINT pk_session_status_id   PRIMARY KEY (id),
                                CONSTRAINT uq_session_status_name UNIQUE (name)
);


-- ---------------------------------------------------------------------
-- Benutzerverwaltung
-- ---------------------------------------------------------------------

CREATE TABLE app_user (
                          id            BIGINT       GENERATED ALWAYS AS IDENTITY,
                          email         VARCHAR(254) NOT NULL,
                          password_hash VARCHAR(255) NOT NULL,
                          first_name    VARCHAR(100) NULL,
                          last_name     VARCHAR(100) NULL,

                          CONSTRAINT pk_app_user_id    PRIMARY KEY (id),
                          CONSTRAINT uq_app_user_email UNIQUE (email),
                          CONSTRAINT ck_app_user_email CHECK (email = lower(email))
);

-- Bewusst ohne Beziehung zu app_user: Die Sperre gilt pro E-Mail-Adresse,
-- auch wenn kein Konto existiert (#NFA-1.3).
CREATE TABLE login_lock (
                            id              BIGINT       GENERATED ALWAYS AS IDENTITY,
                            email           VARCHAR(254) NOT NULL,
                            failed_attempts INTEGER      NOT NULL DEFAULT 0,
                            locked_until    TIMESTAMPTZ  NULL,

                            CONSTRAINT pk_login_lock_id              PRIMARY KEY (id),
                            CONSTRAINT uq_login_lock_email           UNIQUE (email),
                            CONSTRAINT ck_login_lock_email           CHECK (email = lower(email)),
                            CONSTRAINT ck_login_lock_failed_attempts CHECK (failed_attempts >= 0)
);


-- ---------------------------------------------------------------------
-- Übungskatalog
-- ---------------------------------------------------------------------

CREATE TABLE exercise (
                          id              BIGINT        GENERATED ALWAYS AS IDENTITY,
                          user_id         BIGINT        NOT NULL,
                          name            VARCHAR(100)  NOT NULL,
                          muscle_group_id INTEGER       NOT NULL,
                          description     VARCHAR(1000) NULL,
                          deleted         BOOLEAN       NOT NULL DEFAULT FALSE,

                          CONSTRAINT pk_exercise_id PRIMARY KEY (id),

                          CONSTRAINT fk_exercise_user_id
                              FOREIGN KEY (user_id) REFERENCES app_user (id)
                                  ON DELETE CASCADE,

                          CONSTRAINT fk_exercise_muscle_group_id
                              FOREIGN KEY (muscle_group_id) REFERENCES muscle_group (id)
                                  ON DELETE RESTRICT
);

-- Übungsname je Sportler eindeutig, ohne Gross-/Kleinschreibung,
-- nur nicht gelöschte Zeilen (Name nach Soft Delete wieder verwendbar).
CREATE UNIQUE INDEX uq_exercise_user_id_name
    ON exercise (user_id, lower(name))
    WHERE deleted = FALSE;


-- ---------------------------------------------------------------------
-- Trainingsroutinen
-- ---------------------------------------------------------------------

CREATE TABLE routine (
                         id      BIGINT       GENERATED ALWAYS AS IDENTITY,
                         user_id BIGINT       NOT NULL,
                         name    VARCHAR(100) NOT NULL,
                         deleted BOOLEAN      NOT NULL DEFAULT FALSE,

                         CONSTRAINT pk_routine_id PRIMARY KEY (id),

    -- Ziel des zusammengesetzten Fremdschlüssels aus training_session (FK1)
                         CONSTRAINT uq_routine_id_user_id UNIQUE (id, user_id),

                         CONSTRAINT fk_routine_user_id
                             FOREIGN KEY (user_id) REFERENCES app_user (id)
                                 ON DELETE CASCADE
);

-- Routinenname je Sportler eindeutig, ohne Gross-/Kleinschreibung,
-- nur nicht gelöschte Zeilen.
CREATE UNIQUE INDEX uq_routine_user_id_name
    ON routine (user_id, lower(name))
    WHERE deleted = FALSE;

CREATE TABLE routine_exercise (
                                  id          BIGINT  GENERATED ALWAYS AS IDENTITY,
                                  routine_id  BIGINT  NOT NULL,
                                  exercise_id BIGINT  NOT NULL,
                                  position    INTEGER NOT NULL,

                                  CONSTRAINT pk_routine_exercise_id PRIMARY KEY (id),

                                  CONSTRAINT uq_routine_exercise_routine_id_exercise_id
                                      UNIQUE (routine_id, exercise_id)
                                          DEFERRABLE INITIALLY DEFERRED,

                                  CONSTRAINT uq_routine_exercise_routine_id_position
                                      UNIQUE (routine_id, position)
                                          DEFERRABLE INITIALLY DEFERRED,

                                  CONSTRAINT ck_routine_exercise_position CHECK (position >= 1),

                                  CONSTRAINT fk_routine_exercise_routine_id
                                      FOREIGN KEY (routine_id) REFERENCES routine (id)
                                          ON DELETE CASCADE,

    -- Prüfung erst beim COMMIT, damit die Kontolöschung über beide
    -- Kaskaden (exercise und routine) durchläuft.
                                  CONSTRAINT fk_routine_exercise_exercise_id
                                      FOREIGN KEY (exercise_id) REFERENCES exercise (id)
                                          ON DELETE NO ACTION
                                          DEFERRABLE INITIALLY DEFERRED
);


-- ---------------------------------------------------------------------
-- Trainingsplanung und Protokollierung
-- ---------------------------------------------------------------------

-- user_id ist bewusst redundant zur Routine: Eine Routine wechselt nie
-- den Besitzer, der Besitz der Einheit wird direkt über user_id geprüft.
-- Der zusammengesetzte Fremdschlüssel stellt sicher, dass user_id immer
-- zum Besitzer der Routine passt.
CREATE TABLE training_session (
                                  id           BIGINT        GENERATED ALWAYS AS IDENTITY,
                                  routine_id   BIGINT        NOT NULL,
                                  user_id      BIGINT        NOT NULL,
                                  status_id    INTEGER       NOT NULL,
                                  planned_date DATE          NULL,
                                  started_at   TIMESTAMPTZ   NULL,
                                  finished_at  TIMESTAMPTZ   NULL,
                                  note         VARCHAR(1000) NULL,
                                  deleted      BOOLEAN       NOT NULL DEFAULT FALSE,

                                  CONSTRAINT pk_training_session_id PRIMARY KEY (id),

                                  CONSTRAINT fk_training_session_routine_id_user_id
                                      FOREIGN KEY (routine_id, user_id) REFERENCES routine (id, user_id)
                                          ON DELETE CASCADE,

                                  CONSTRAINT fk_training_session_status_id
                                      FOREIGN KEY (status_id) REFERENCES session_status (id)
                                          ON DELETE RESTRICT,

    -- Soft Delete nur bei Status «abgeschlossen» (3)
                                  CONSTRAINT ck_training_session_deleted
                                      CHECK (deleted = FALSE OR status_id = 3)
);

-- Pro Sportler höchstens eine Einheit im Status «aktiv» (2) (#FA-5.1)
CREATE UNIQUE INDEX uq_training_session_user_id
    ON training_session (user_id)
    WHERE status_id = 2;

CREATE TABLE session_exercise (
                                  id                  BIGINT  GENERATED ALWAYS AS IDENTITY,
                                  training_session_id BIGINT  NOT NULL,
                                  exercise_id         BIGINT  NOT NULL,
                                  position            INTEGER NOT NULL,

                                  CONSTRAINT pk_session_exercise_id PRIMARY KEY (id),

                                  CONSTRAINT uq_session_exercise_training_session_id_exercise_id
                                      UNIQUE (training_session_id, exercise_id)
                                          DEFERRABLE INITIALLY DEFERRED,

                                  CONSTRAINT uq_session_exercise_training_session_id_position
                                      UNIQUE (training_session_id, position)
                                          DEFERRABLE INITIALLY DEFERRED,

                                  CONSTRAINT ck_session_exercise_position CHECK (position >= 1),

                                  CONSTRAINT fk_session_exercise_training_session_id
                                      FOREIGN KEY (training_session_id) REFERENCES training_session (id)
                                          ON DELETE CASCADE,

                                  CONSTRAINT fk_session_exercise_exercise_id
                                      FOREIGN KEY (exercise_id) REFERENCES exercise (id)
                                          ON DELETE NO ACTION
                                          DEFERRABLE INITIALLY DEFERRED
);

CREATE TABLE exercise_set (
                              id                  BIGINT       GENERATED ALWAYS AS IDENTITY,
                              session_exercise_id BIGINT       NOT NULL,
                              set_number          INTEGER      NOT NULL,
                              weight_kg           NUMERIC(6,2) NOT NULL,
                              repetitions         INTEGER      NOT NULL,
                              rest_seconds        INTEGER      NULL,

                              CONSTRAINT pk_exercise_set_id PRIMARY KEY (id),

                              CONSTRAINT uq_exercise_set_session_exercise_id_set_number
                                  UNIQUE (session_exercise_id, set_number)
                                      DEFERRABLE INITIALLY DEFERRED,

                              CONSTRAINT ck_exercise_set_set_number   CHECK (set_number >= 1),
                              CONSTRAINT ck_exercise_set_weight_kg    CHECK (weight_kg BETWEEN 0 AND 1000),
                              CONSTRAINT ck_exercise_set_repetitions  CHECK (repetitions BETWEEN 1 AND 100),
                              CONSTRAINT ck_exercise_set_rest_seconds CHECK (rest_seconds IS NULL
                                  OR rest_seconds BETWEEN 0 AND 3600),

                              CONSTRAINT fk_exercise_set_session_exercise_id
                                  FOREIGN KEY (session_exercise_id) REFERENCES session_exercise (id)
                                      ON DELETE CASCADE
);


-- ---------------------------------------------------------------------
-- Indizes auf Fremdschlüssel
-- (PK und UNIQUE haben automatisch einen Index. Nicht indexiert werden
--  FK, die bereits vorne in einem UNIQUE stehen, sowie Verweise auf
--  Stammdaten.)
-- ---------------------------------------------------------------------

CREATE INDEX ix_exercise_user_id             ON exercise (user_id);
CREATE INDEX ix_routine_user_id              ON routine (user_id);
CREATE INDEX ix_routine_exercise_exercise_id ON routine_exercise (exercise_id);
CREATE INDEX ix_training_session_routine_id  ON training_session (routine_id);
CREATE INDEX ix_session_exercise_exercise_id ON session_exercise (exercise_id);


-- ---------------------------------------------------------------------
-- Stammdatenwerte (feste IDs, werden im Code verwendet)
-- ---------------------------------------------------------------------

INSERT INTO muscle_group (id, name) VALUES
                                        (1, 'Brust'),
                                        (2, 'Rücken'),
                                        (3, 'Schultern'),
                                        (4, 'Bizeps'),
                                        (5, 'Trizeps'),
                                        (6, 'Bauch'),
                                        (7, 'Beine'),
                                        (8, 'Gesäss');

INSERT INTO session_status (id, name) VALUES
                                          (1, 'geplant'),
                                          (2, 'aktiv'),
                                          (3, 'abgeschlossen');