package com.IoTPlatform.Utilities;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLHelper {
    private static final String USERNAME = "root";
    private static final String PASSWORD = "password";
    private Connection connection;
    private String databaseURL;

    public SQLHelper(String DBName) throws SQLException {
        DriverManager.registerDriver(new com.mysql.jdbc.Driver());
        this.databaseURL = "jdbc:mysql://localhost:3306/" + DBName;
        this.connection = DriverManager.getConnection(databaseURL, USERNAME, PASSWORD);
    }

    public void close() throws SQLException {
        connection.close();
    }

    public int executeUpdate(String query, Object... params) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }

            return statement.executeUpdate();
        }
    }

    public ResultSet executeQuery(String query, Object... params) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(query);

        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }

        return statement.executeQuery(); //get info from sql
    }
}



