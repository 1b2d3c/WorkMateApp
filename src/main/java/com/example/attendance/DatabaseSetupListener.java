package com.example.attendance;

import com.example.attendance.dao.DBConnection;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

@WebListener
public class DatabaseSetupListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Create tables if they don't exist
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (user_id SERIAL PRIMARY KEY, username VARCHAR(50) UNIQUE NOT NULL, password VARCHAR(100) NOT NULL, role VARCHAR(20) NOT NULL, enabled BOOLEAN NOT NULL)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS attendance (attendance_id SERIAL PRIMARY KEY, user_id INT NOT NULL, check_in_time TIMESTAMP NOT NULL, check_out_time TIMESTAMP, FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS messages (message_id SERIAL PRIMARY KEY, message_text TEXT NOT NULL, priority VARCHAR(20) NOT NULL, start_datetime TIMESTAMP NOT NULL, end_datetime TIMESTAMP NOT NULL)");

            // Insert initial data if users table is empty
            try (ResultSet rs = stmt.executeQuery("SELECT 1 FROM users LIMIT 1")) {
                if (!rs.next()) {
                    stmt.executeUpdate("INSERT INTO users (username, password, role, enabled) VALUES ('employee', 'password', 'employee', true)");
                    stmt.executeUpdate("INSERT INTO users (username, password, role, enabled) VALUES ('manager', 'password', 'manager', true)");
                }
            }

        } catch (Exception e) {
            // Use the servlet context logger to log errors
            sce.getServletContext().log("Database setup failed.", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Cleanup logic if needed
    }
}
