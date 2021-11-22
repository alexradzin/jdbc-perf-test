package org.jdbc.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Properties;
import java.util.Random;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

abstract class BasePerformanceTestCase {
    private final Random random = new Random(System.currentTimeMillis());

    protected Properties getProperties(String db) throws IOException {
        Properties props = new Properties();
        props.load(getClass().getResourceAsStream("/" + db + ".properties"));

        Properties sysProps = System.getProperties();
        for (String key : props.keySet().stream().map(k -> (String)k).collect(toList())) {
            String sysPropKey = db + "." + key;
            if (sysProps.containsKey(sysPropKey)) {
                props.setProperty(key, sysProps.getProperty(sysPropKey));
            }
        }

        return props;
    }

    protected int load(int nLines, String table, Connection connection, String loadCommand, File dir) throws IOException, ReflectiveOperationException, SQLException {
        Class<? extends JdbcRunner> runnerClass = StatementUpdateOneByOne.class;
        Object[][] args = data(nLines);
        File file = writeData(new String[] {"id", "label", "text"}, args, dir);
        Properties loadProperties = stringToProperties(format("TMP-TABLE=%s; FILE=%s", table, file));
        int loaded = new TimeMeasuringJdbcRunner(createRunner(runnerClass, connection, Template.replace(loadCommand, loadProperties))).execute(new Object[1][1]);
        file.delete();
        return loaded;
    }

    protected JdbcRunner createRunner(Class<? extends JdbcRunner> runnerClass, Connection connection, String sql) throws ReflectiveOperationException {
        return runnerClass.getConstructor(Connection.class, String.class).newInstance(connection, sql);
    }

    protected Properties stringToProperties(String propsStr) throws IOException {
        Properties props = null;
        if (propsStr != null) {
            props = new Properties();
            props.load(new StringReader(propsStr.replaceAll("\\s*[,;]\\s*", "\n")));
        }
        return props;
    }

    protected void dropTable(Connection connection, String dropTable, Properties tableProperties) {
        try {
            connection.createStatement().execute(Template.replace(dropTable, tableProperties));
        } catch (SQLException e) {
            // may be thrown if table does not exist
        }
    }

    protected String randomString() {
        char[] chars = new char[256];
        for (int i = 0; i < chars.length; i++) {
            random.nextInt(26);
            chars[i] = (char)('a' + random.nextInt(26));
        }
        return new String(chars);
    }

    protected Object[][] data(int nLines) {
        Object[][] result = new Object[nLines][3];
        for (int i = 0; i < nLines; i++) {
            int id = i + 1;
            result[i] = new Object[] {id, "" + id, randomString()};
        }
        return result;
    }

    protected String propertyValue(Properties props, String baseName, String test) {
        return props.getProperty(propertyName(baseName, test), props.getProperty(baseName));
    }

    protected String propertyName(String baseName, String test) {
        return test == null || "".equals(test) ? baseName : baseName + "." + test;
    }

    protected File writeData(String[] header, Object[][] data, File dir) throws IOException {
        File file = File.createTempFile("data", "csv", dir);
        file.deleteOnExit();
        return writeData(file, header, data);
    }

    protected File writeData(File file, String[] header, Object[][] data) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println(String.join(",", header));
            for (Object[] row : data) {
                pw.println(Arrays.stream(row).map(d -> "" + d).collect(Collectors.joining(",")));
                pw.flush();
            }
        }
        return file;
    }
}
