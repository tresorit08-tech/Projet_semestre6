package com.example.projet_semestre6;

import java.sql.*;

public class DB {
    private static DB instance;
    private Connection conn;
    private PreparedStatement pstm;

    private final String URL = "jdbc:mysql://127.0.0.1:3306/GESTION_ETABLISSEMENT";
    private final String USER = "root";
    private final String PASSWORD = "";

    private DB() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Connexion réussie !");
        } catch (Exception e) {
            System.out.println("❌ Erreur de connexion : " + e.getMessage());
        }
    }

    public static synchronized DB getInstance() {
        if (instance == null) {
            instance = new DB();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (conn == null || conn.isClosed()) {
                conn = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur de reconnexion : " + e.getMessage());
        }
        return conn;
    }

    public void initPrepare(String sql) throws SQLException {
        if (pstm != null) {
            pstm.close();
        }
        if (getConnection() != null) {
            pstm = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        }
    }

    public void setParameters(Object... params) throws SQLException {
        if (pstm != null) {
            for (int i = 0; i < params.length; i++) {
                pstm.setObject(i + 1, params[i]);
            }
        }
    }

    public ResultSet executeSelect() throws SQLException {
        if (pstm == null) {
            throw new SQLException("PreparedStatement is null. initPrepare() must be called first.");
        }
        return pstm.executeQuery();
    }

    public int executeMaj() throws SQLException {
        if (pstm == null) {
            throw new SQLException("PreparedStatement is null. initPrepare() must be called first.");
        }
        return pstm.executeUpdate();
    }

    public void closeConnection() {
        try {
            if (pstm != null) {
                pstm.close();
            }
            if (conn != null) {
                conn.close();
                System.out.println("✅ Connexion fermée.");
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur closeConnection : " + e.getMessage());
        }
    }

    public PreparedStatement getPstmt() {
        return pstm;
    }

    public int executePrepare() throws SQLException {
        if (pstm == null) {
            throw new SQLException("PreparedStatement is null. initPrepare() must be called first.");
        }
        return pstm.executeUpdate();
    }
}