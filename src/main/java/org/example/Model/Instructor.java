package org.example.Model;

import org.example.Server.Database;
import java.sql.*;

public class Instructor extends User {
    public Instructor(int id, String password) {
        super(id, password, Role.INSTRUCTOR);
    }
    @Override
    public boolean isValidUser() {
        try(Connection connection = Database.getInstance().getDatabaseConnection()) {
            System.out.println("The Instructor have Successfully Connected to the Database.");
            String query = "SELECT instructor_id FROM instructors WHERE instructor_id = ? AND password = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, id);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database: " + e.getMessage());
            return false;
        }
    }
}
