package org.jdbc.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.Collectors;

abstract class InputStreamJdbcRunner extends BaseJdbcRunner {
    private final String[] header;

    public InputStreamJdbcRunner(Connection connection, String sql, String[] header) {
        super(connection, sql);
        this.header = header;
    }

    @Override
    public int execute(Object[][] args) throws SQLException {
        try {
            return execute(toInputStream(args));
        } catch (IOException e) {
            throw new SQLException(e);
        }
    }

    private InputStream toInputStream(Object[][] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(baos))) {
            pw.println(String.join(",", header));
            for (Object[] row : data) {
                pw.println(Arrays.stream(row).map(d -> "" + d).collect(Collectors.joining(",")));
                pw.flush();
            }
        }
        baos.close();
        return new ByteArrayInputStream(baos.toByteArray());
    }

    protected abstract int execute(InputStream in) throws SQLException;
}
