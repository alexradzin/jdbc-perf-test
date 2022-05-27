package org.jdbc.test;

import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

public class PostgreSqlStreamInputStreamJdbcRunner extends InputStreamJdbcRunner {
    public PostgreSqlStreamInputStreamJdbcRunner(Connection connection, String sql, String[] header) {
        super(connection, sql, header);
    }

    @Override
    protected int execute(InputStream in) throws SQLException {
        try {
            BaseConnection pgConnection = connection.unwrap(BaseConnection.class);
            CopyManager copyManager = new CopyManager(pgConnection);
            return (int)copyManager.copyIn(sql, in);
        } catch (IOException e) {
            throw new SQLException(e);
        }
    }
}
