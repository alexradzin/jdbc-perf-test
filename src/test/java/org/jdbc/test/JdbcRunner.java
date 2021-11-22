package org.jdbc.test;

import java.sql.SQLException;

public interface JdbcRunner {
    int execute(Object[][] args) throws SQLException;
}
