package org.jdbc.test;

import com.mysql.cj.jdbc.StatementImpl;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class MySqlStreamInputStreamJdbcRunner extends InputStreamJdbcRunner {
    public MySqlStreamInputStreamJdbcRunner(Connection connection, String sql, String[] header) {
        super(connection, sql, header);
    }

    @Override
    protected int execute(InputStream in) throws SQLException {
        Statement statement = connection.createStatement();
        StatementImpl statementImpl = statement.unwrap(StatementImpl.class);
        statementImpl.setLocalInfileInputStream(in);
        return statement.executeUpdate(sql);
    }
}
