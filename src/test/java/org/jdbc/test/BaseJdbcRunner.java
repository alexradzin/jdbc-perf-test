package org.jdbc.test;

import java.sql.Connection;
import java.util.Arrays;

import static java.sql.Statement.SUCCESS_NO_INFO;

public abstract class BaseJdbcRunner implements JdbcRunner {
    protected final Connection connection;
    protected final String sql;

    public BaseJdbcRunner(Connection connection, String sql) {
        this.connection = connection;
        this.sql = sql;
    }

    protected int count(int[] counts) {
        return Arrays.stream(counts).map(c -> c == SUCCESS_NO_INFO ? 1 : c).sum();
    }

}
