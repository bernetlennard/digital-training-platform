package ch.bernet.trainingplatform;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

// Gemeinsame Testdaten gemäss Testanleitung
public final class TestData {

    public static final ZoneId ZONE = ZoneId.of("Europe/Zurich");
    public static final Instant NOW = Instant.parse("2026-10-15T08:00:00Z");
    public static final LocalDate TODAY = LocalDate.of(2026, 10, 15);
    public static final Clock CLOCK = Clock.fixed(NOW, ZONE);

    public static final Long USER_A = 1L;
    public static final Long USER_B = 2L;

    public static final Long BENCH_PRESS = 10L;
    public static final Long SQUAT = 11L;
    public static final Long PULL_UP = 12L;
    public static final Long ROUTINE_ID = 20L;

    private TestData() {
    }

}
