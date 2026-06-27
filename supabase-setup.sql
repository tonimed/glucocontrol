-- ============================================================
-- GlucoControl — Setup inicial en Supabase
-- Ejecutar en: Supabase Dashboard → SQL Editor → New query
-- ============================================================

-- 1. Tabla principal de lecturas de glucosa
-- id es BIGSERIAL: el servidor genera el valor; la app no lo envía en INSERT.
CREATE TABLE IF NOT EXISTS glucose_readings (
    id                   BIGSERIAL     PRIMARY KEY,
    user_id              UUID          NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    value_mg_dl          INT           NOT NULL CHECK (value_mg_dl BETWEEN 1 AND 600),
    date_epoch_day       BIGINT        NOT NULL,          -- LocalDate.toEpochDay()
    time_seconds_of_day  INT,                             -- LocalTime.toSecondOfDay(), NULL si no se registra
    tag                  TEXT          NOT NULL DEFAULT 'OTRO',
    notes                TEXT,
    created_at           TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- Índice para las queries por usuario + fecha (más frecuentes)
CREATE INDEX IF NOT EXISTS idx_glucose_user_date
    ON glucose_readings (user_id, date_epoch_day);

-- 2. Row Level Security: cada usuario solo accede a sus propias filas
ALTER TABLE glucose_readings ENABLE ROW LEVEL SECURITY;

-- Política única: el usuario puede SELECT / INSERT / UPDATE / DELETE sus filas
CREATE POLICY "users_own_their_readings"
    ON glucose_readings
    FOR ALL
    USING      (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- ============================================================
-- Verificación (opcional — ejecutar para comprobar que todo está bien)
-- ============================================================
-- SELECT tablename, rowsecurity FROM pg_tables WHERE tablename = 'glucose_readings';
-- SELECT policyname, cmd, qual FROM pg_policies WHERE tablename = 'glucose_readings';
