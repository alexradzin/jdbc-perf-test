package org.jdbc.test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

public class StatementUpdateOneByOne extends BaseJdbcRunner {
    public StatementUpdateOneByOne(Connection connection, String sql) {
        super(connection, sql);
    }

    @Override
    public int execute(Object[][] args) throws SQLException {
        int count = 0;
        int start = 0;
        for (String sql : super.sql.split(";")) {
            try (Statement statement = connection.createStatement()) {
                int argsCount = (int)sql.chars().filter(ch -> ch == '?').count();
                int end = start + argsCount;
                for (Object[] row : args) {
                    Object[] statementRowArgs = Arrays.copyOfRange(row, start, end);
                    count += statement.executeUpdate(Template.replaceParameters(sql, statementRowArgs));
                }
                start += end;
            }
        }
        return count;
    }
}
