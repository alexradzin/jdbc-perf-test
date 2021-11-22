package org.jdbc.test;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;

public class DataLoadMetricsTest extends BasePerformanceTestCase {
    @BeforeAll
    static void beforeAll() {
        System.out.println("db,n,load,merge");
    }

    @ParameterizedTest
    @CsvSource(value = {
            "postgresql,1000", "postgresql,10000", "postgresql,100000",
            "postgresql,1000000", "postgresql,2000000", "postgresql,3000000", "postgresql,4000000", "postgresql,5000000",

            "mysql,1000", "mysql,10000", "mysql,100000", "mysql,1000000",
            "mysql,2000000", "mysql,3000000", "mysql,4000000", "mysql,5000000"
    })
    void upsertByNumberOfRows(String db, int nLines) throws IOException, SQLException, ReflectiveOperationException {
        Properties props = getProperties(db);
        String jdbcUrl = props.getProperty("jdbc.url");
        Properties jdbcProps = stringToProperties(props.getProperty("jdbc.properties"));
        Class<? extends JdbcRunner> runnerClass = StatementUpdateOneByOne.class;

        String test = "pk";
        String table = propertyValue(props, "table", test);
        String createTable = propertyValue(props, "create.table", test);
        String load = props.getProperty("load");
        String merge = props.getProperty("merge");
        String dropTable = props.getProperty("drop.table");
        File loadDir = Optional.ofNullable(props.getProperty("load.dir")).map(File::new).orElse(null);

        try (Connection connection = DriverManager.getConnection(jdbcUrl, jdbcProps)) {
            Properties tableProperties = stringToProperties("TABLE=" + table);
            dropTable(connection, dropTable, tableProperties);
            connection.createStatement().execute(Template.replace(createTable, tableProperties));

            String tempTable = table + 1;
            Properties tempTableProperties = stringToProperties("TABLE=" + tempTable);
            dropTable(connection, dropTable, tempTableProperties);
            connection.createStatement().execute(Template.replace(createTable, tempTableProperties));

            int loaded = load(nLines, tempTable, connection, load, loadDir);

            Properties mergeProps = new Properties();
            mergeProps.putAll(tableProperties);
            mergeProps.setProperty("TMP-TABLE", tempTable);
            int merged = 0;
            if (merge != null) {
                merged = new TimeMeasuringJdbcRunner(createRunner(runnerClass, connection, Template.replace(merge, mergeProps))).execute(new Object[1][1]);
            }
            System.out.printf("%s, %d, %d, %d%n", db, nLines, loaded, merged);
            dropTable(connection, dropTable, tableProperties);
        }
    }
}
