package org.jdbc.test;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import static java.lang.String.format;

class PerformanceTest extends BasePerformanceTestCase{
    private static final String PS1_1 = "ps.update";
    private static final String PS_BATCH = "ps.batch";
    private static final String ST1_1 = "st.update";
    private static final String ST_BATCH = "batch.update";
    private static final String ST_SCRIPT = "script.update";

    private static final Map<String, Class<? extends JdbcRunner>> updaters = Map.of(
            PS1_1, PreparedStatementUpdateOneByOne.class,
            PS_BATCH, PreparedStatementBatchUpdate.class,
            ST1_1, StatementUpdateOneByOne.class,
            ST_BATCH, StatementBatchUpdate.class,
            ST_SCRIPT, ScriptUpdate.class
    );

    @BeforeAll
    static void beforeAll() {
        System.out.println("db,table-type, statement, rows count, insert, update, upsert, cond-upsert");
    }

    @ParameterizedTest(name = "{0}.{1}.{2}")
    @CsvSource(value = {
            // PostgreSQL
            "postgresql,," + PS1_1 + ",",
            "postgresql,pk," + PS1_1 + ",id",
            "postgresql,int.index," + PS1_1 + ",id",
            "postgresql,str.index," + PS1_1 + ",label",

            "postgresql,," + PS_BATCH + ",",
            "postgresql,pk," + PS_BATCH + ",id",
            "postgresql,int.index," + PS_BATCH + ",id",
            "postgresql,str.index," + PS_BATCH + ",label",

            "postgresql,," + ST1_1 + ",",
            "postgresql,pk," + ST1_1 + ",id",
            "postgresql,int.index," + ST1_1 + ",id",
            "postgresql,str.index," + ST1_1 + ",label",

            "postgresql,," + ST_BATCH + ",",
            "postgresql,pk," + ST_BATCH + ",id",
            "postgresql,int.index," + ST_BATCH + ",id",
            "postgresql,str.index," + ST_BATCH + ",label",

            "postgresql,," + ST_SCRIPT + ",",
            "postgresql,pk," + ST_SCRIPT + ",id",
            "postgresql,int.index," + ST_SCRIPT + ",id",
            "postgresql,str.index," + ST_SCRIPT + ",label",

            // MySQL
            "mysql,," + PS1_1 + ",",
            "mysql,pk," + PS1_1 + ",id",
            "mysql,int.index," + PS1_1 + ",id",
            "mysql,str.index," + PS1_1 + ",label",

            "mysql,," + PS_BATCH + ",",
            "mysql,pk," + PS_BATCH + ",id",
            "mysql,int.index," + PS_BATCH + ",id",
            "mysql,str.index," + PS_BATCH + ",label",

            "mysql,," + ST1_1 + ",",
            "mysql,pk," + ST1_1 + ",id",
            "mysql,int.index," + ST1_1 + ",id",
            "mysql,str.index," + ST1_1 + ",label",

            "mysql,," + ST_BATCH + ",",
            "mysql,pk," + ST_BATCH + ",id",
            "mysql,int.index," + ST_BATCH + ",id",
            "mysql,str.index," + ST_BATCH + ",label"
    })
    void byStatement(String db, String test, String updater, String constraintField) throws IOException, ReflectiveOperationException, SQLException {
        Properties props = getProperties(db);
        String jdbcUrl = props.getProperty("jdbc.url");
        Properties jdbcProps = stringToProperties(props.getProperty("jdbc.properties"));
        Class<? extends JdbcRunner> updaterClass = updaters.get(updater);

        String table = propertyValue(props, "table", test);
        String createTable = propertyValue(props, "create.table", test);
        String insert = propertyValue(props, "insert", test);
        String update = propertyValue(props, "update", test);
        String upsert = constraintField == null ? null : propertyValue(props, "upsert", constraintField);
        String upsertScript = props.getProperty("upsert");
        String dropTable = props.getProperty("drop.table");

        int nLines = Integer.parseInt(props.getProperty("n"));

        byStatement(format("%s,%s,%s", db, test, updater), jdbcUrl, jdbcProps, updaterClass, table, createTable, insert, update, upsert, upsertScript, dropTable, data(nLines));
    }

    private void byStatement(String testName, String jdbcUrl, Properties jdbcProps, Class<? extends JdbcRunner> runnerClass, String table,
                             String createTable,
                             String insert, String update, String upsert, String upsertByCondition,
                             String dropTable,
                             Object[][] args) throws SQLException, ReflectiveOperationException, IOException {
        Connection connection = DriverManager.getConnection(jdbcUrl, jdbcProps);
        Properties tableProperties = stringToProperties("TABLE=" + table);
        dropTable(connection, dropTable, tableProperties);
        connection.createStatement().execute(Template.replace(createTable, tableProperties));
        int insertDuration = new TimeMeasuringJdbcRunner(createRunner(runnerClass, connection, Template.replace(insert, tableProperties))).execute(args);

        Object[][] updateArgs = Arrays.stream(args).map(row -> new Object[] {randomString(), row[0]}).toArray(Object[][]::new);
        int updateDuration = new TimeMeasuringJdbcRunner(createRunner(runnerClass, connection, Template.replace(update, tableProperties))).execute(updateArgs);

        int upsertDuration = 0;
        if (upsert != null) {
            Object[][] upsertArgs = Arrays.stream(args).map(row -> new Object[] {row[0], row[1], row[2], randomString()}).toArray(Object[][]::new);
            upsertDuration = new TimeMeasuringJdbcRunner(createRunner(runnerClass, connection, Template.replace(upsert, tableProperties))).execute(upsertArgs);
        }

        int upsertByConditionDuration = 0;
        if (upsertByCondition != null) {
            Object[][] upsertArgs = Arrays.stream(args).map(row -> new Object[] {randomString(), row[0], row[0], row[1], row[2], row[0]}).toArray(Object[][]::new);
            upsertByConditionDuration = new TimeMeasuringJdbcRunner(createRunner(runnerClass, connection, Template.replace(upsertByCondition, tableProperties))).execute(upsertArgs);
        }

        System.out.printf("%s, %d, %d, %d, %d, %d%n",
                testName, args.length,
                insertDuration, updateDuration, upsertDuration, upsertByConditionDuration);
        dropTable(connection, dropTable, tableProperties);
    }
}
