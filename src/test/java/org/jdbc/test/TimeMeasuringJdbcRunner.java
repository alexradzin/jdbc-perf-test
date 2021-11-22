package org.jdbc.test;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TimeMeasuringJdbcRunner implements JdbcRunner {
    private static final Map<TimeUnit, Long> timeTransformers = Map.of(
            TimeUnit.NANOSECONDS, 1L,
            TimeUnit.MICROSECONDS, 1_000L,
            TimeUnit.MILLISECONDS, 1_000_000L,
            TimeUnit.SECONDS, 1_000_000_000L
    );

    private final JdbcRunner runner;
    private final TimeUnit timeUnit;

    public TimeMeasuringJdbcRunner(JdbcRunner runner) {
        this(runner, TimeUnit.MILLISECONDS);
    }

    public TimeMeasuringJdbcRunner(JdbcRunner runner, TimeUnit timeUnit) {
        this.runner = runner;
        this.timeUnit = timeUnit;
    }

    @Override
    public int execute(Object[][] args) throws SQLException {
        long before = System.nanoTime();
        runner.execute(args);
        long after = System.nanoTime();
        int time = (int)((after - before) / timeTransformers.get(timeUnit));
        return time;
    }
}
