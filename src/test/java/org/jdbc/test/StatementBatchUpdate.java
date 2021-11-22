package org.jdbc.test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

public class StatementBatchUpdate extends BaseJdbcRunner {
    public StatementBatchUpdate(Connection connection, String sql) {
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
                    statement.addBatch(Template.replaceParameters(sql, statementRowArgs));
                }
                count += count(statement.executeBatch());start += end;
            }
        }
        return count;
    }
}
