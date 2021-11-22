package org.jdbc.test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class ScriptUpdate extends BaseJdbcRunner {
    public ScriptUpdate(Connection connection, String sql) {
        super(connection, sql);
    }

    @Override
    public int execute(Object[][] args) throws SQLException {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        for (Object[] row : args) {
            pw.println(Template.replaceParameters(sql, row) + ";");
        }

        try (Statement statement = connection.createStatement()) {
            return statement.executeUpdate(sw.toString());
        }
    }
}
