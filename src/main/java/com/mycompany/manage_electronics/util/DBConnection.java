package com.mycompany.manage_electronics.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Có thể ghi đè bằng biến môi trường DB_URL, DB_USER, DB_PASSWORD (không cần sửa code)
    private static final String URL = env("DB_URL",
            "jdbc:sqlserver://localhost:1433;databaseName=Manage_Electronics;encrypt=false;trustServerCertificate=true");
    private static final String USER = env("DB_USER", "sa");
    private static final String PASSWORD = env("DB_PASSWORD", "123456"); // Thay đổi password của bạn

    private static String env(String key, String defaultValue) {
        String v = System.getenv(key);
        return (v == null || v.isEmpty()) ? defaultValue : v;
    }
    
    static {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
