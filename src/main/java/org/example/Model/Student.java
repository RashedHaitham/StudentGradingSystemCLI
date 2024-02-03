package org.example.Model;

import org.example.Server.Database;
import java.sql.*;

public class Student extends User {
    public Student(int id, String password) {
        super(id, password, Role.STUDENT);
    }

    @Override
    public boolean isValidUser() {
        try(Connection connection = Database.getInstance().getDatabaseConnection()) {
            System.out.println("The Student have Successfully Connected to the Database.");
            String query = "SELECT student_id FROM students WHERE student_id = ? AND password = ?";
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
