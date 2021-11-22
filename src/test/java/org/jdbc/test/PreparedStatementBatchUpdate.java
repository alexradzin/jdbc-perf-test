package org.jdbc.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;

public class PreparedStatementBatchUpdate extends BaseJdbcRunner {
    public PreparedStatementBatchUpdate(Connection connection, String sql) {
        super(connection, sql);
    }

    @Override
    public int execute(Object[][] args) throws SQLException {
        int count = 0;
        int start = 0;
        for (String sql : super.sql.split(";")) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                int argsCount = (int)sql.chars().filter(ch -> ch == '?').count();
                int end = start + argsCount;
                for (Object[] row : args) {
                    Object[] statementRowArgs = Arrays.copyOfRange(row, start, end);
                    for (int i = 0; i < statementRowArgs.length; i++) {
                        ps.setObject(i + 1, statementRowArgs[i]);
                    }
                    ps.addBatch();
                }
                count += count(ps.executeBatch());
                start += end;
            }
        }
        return count;
    }
}
