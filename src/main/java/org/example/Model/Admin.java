package org.example.Model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Admin extends User {
    public Admin(int id, String password) {
        super(id, password, Role.ADMIN);
    }

    @Override
    public boolean isValidUser() {
        boolean isValid = false;
        try(Connection ignored = DriverManager.getConnection
                ("jdbc:mysql://localhost:3306/" + "directory", username, password)) {
            System.out.println("The Admin have Successfully Connected to the Database.");
            isValid = true;
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database: " + e.getMessage());
        }
        return isValid;
    }
}
