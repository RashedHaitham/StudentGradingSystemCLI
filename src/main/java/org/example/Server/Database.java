package org.example.Server;

import org.example.Model.CourseGrade;
import org.example.Model.StudentGrades;
import org.example.Model.Role;
import java.sql.*;
import java.sql.Connection;
import java.util.*;

public class Database {
    private static final String HOST = "jdbc:mysql://localhost:3306/";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "2002";
    private static final String DB_DATABASE = "directory";
    private static Database instance;
    private final Map<String, List<String>> tableColumns = new HashMap<>();

    private Database() {
        setConnection();
        initTableColumns();
    }

    private void initTableColumns(){
        tableColumns.put("students", Arrays.asList("student_id", "username"));
        tableColumns.put("instructors", Arrays.asList("instructor_id", "username"));
        tableColumns.put("courses", Arrays.asList("course_id", "course_name"));
    }

    public static Database getInstance() {
        if (instance == null)
            instance = new Database();
        return instance;
    }

    private void setConnection() {
        try (Connection ignored = getDatabaseConnection()) {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized Connection  getDatabaseConnection() throws SQLException {
        return DriverManager.getConnection(HOST + DB_DATABASE, DB_USERNAME, DB_PASSWORD);
    }

    public List<String> getTableColumns(String tableName) {
        return tableColumns.getOrDefault(tableName,null);
    }

    private int executeUpdate(String query, Object... params) throws SQLException {
        try (Connection connection = getDatabaseConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }
            return preparedStatement.executeUpdate();
        }
    }

    private ResultSet executeQuery(String query, Object... params) throws SQLException {
        Connection connection = getDatabaseConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        for (int i = 0; i < params.length; i++) {
            preparedStatement.setObject(i + 1, params[i]);
        }
        return preparedStatement.executeQuery();
    }

    public Map<Integer, String> viewCourses(int id, Role role) {
        Map<Integer, String> courses = new HashMap<>();
        String query = "";
        if (role.equals(Role.INSTRUCTOR)) {
            query = "SELECT courses.course_id, courses.course_name " +
                    "FROM instructors " +
                    "JOIN instructor_course ON instructors.instructor_id = instructor_course.instructor_id " +
                    "JOIN courses ON courses.course_id = instructor_course.course_id " +
                    "WHERE instructors.instructor_id = ?";
        } else if (role.equals(Role.STUDENT)) {
            query = "SELECT courses.course_id, courses.course_name " +
                    "FROM students " +
                    "JOIN student_course ON students.student_id = student_course.student_id " +
                    "JOIN courses ON courses.course_id = student_course.course_id " +
                    "WHERE students.student_id = ?";
        }
        try (Connection connection = getDatabaseConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    int courseId = resultSet.getInt("course_id");
                    String courseName = resultSet.getString("course_name");
                    courses.put(courseId, courseName);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }

    public StudentGrades viewGrades(int studentIdInput) {
        List<CourseGrade> courseGrades = new ArrayList<>();
        String studentName = "";
        try {
            String query = "SELECT students.username AS student_name, courses.course_id, courses.course_name, grades.grade " +
                    "FROM students " +
                    "JOIN grades ON students.student_id = grades.student_id " +
                    "JOIN courses ON courses.course_id = grades.course_id " +
                    "WHERE students.student_id = ?";
            try (Connection connection = getDatabaseConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, studentIdInput);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        studentName = resultSet.getString("student_name");
                        int courseId = resultSet.getInt("course_id");
                        String courseName = resultSet.getString("course_name");
                        double grade = resultSet.getDouble("grade");
                        courseGrades.add(new CourseGrade(courseId, courseName, grade));
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return new StudentGrades(studentIdInput, studentName, courseGrades);
    }

    public boolean addRecord(String tableName, Map<String, String> inputData) {
        return insertRecord(tableName, inputData);
    }

    private boolean insertRecord(String tableName, Map<String, String> inputData) {
        StringJoiner columns = new StringJoiner(", ");
        StringJoiner values = new StringJoiner(", ");
        for (String field : inputData.keySet()) {
            columns.add(field);
            values.add("?");
        }
        String sql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + values + ")";
        try {
            executeUpdate(sql, inputData.values().toArray());
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<String[]> getTableContent(String tableName) {
        String query = "SELECT * FROM " + tableName;
        List<String[]> tableContent = new ArrayList<>();
        List<String> columns = tableColumns.get(tableName);
        try (Connection connection = getDatabaseConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String[] row = new String[columns.size()];
                for (int i = 0; i < columns.size(); i++) {
                    row[i] = resultSet.getString(columns.get(i));
                }
                tableContent.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tableContent;
    }

    public String getCrudOptions(String table) {
        return "Please choose the CRUD operation you would like to perform on " + table + " table: \n" +
                "1. Create\t\t2. Read\t\t3. Update\t\t4. Delete\t\t5. Choose another table";
    }

    public boolean updateRecord(String tableName, String columnToUpdate,
                                String primaryKeyColumn, String idToUpdate, String newValue) {
        String query = "UPDATE " + tableName + " SET " + columnToUpdate + " = ? WHERE " + primaryKeyColumn + " = ?";
        try {
            int rowsUpdated = executeUpdate(query, newValue, idToUpdate);
            if (rowsUpdated > 0) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteRecord(String tableName, String idToDelete) {
        List<String> columns = tableColumns.get(tableName);
        String primaryKeyColumn = columns.get(0);
        String deleteSQL = "DELETE FROM " + tableName + " WHERE " + primaryKeyColumn + " = ?";
        try {
            int rowsDeleted = executeUpdate(deleteSQL, idToDelete);
            if (rowsDeleted > 0) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean enrollCourse(int userId, Role role, String tableName, String courseId) {
        String sql = String.format("INSERT INTO %s (%s_id, course_id) VALUES (?, ?)", tableName, role.toString().toLowerCase());
        try {
            int rowsInserted = executeUpdate(sql, userId, courseId);
            if (rowsInserted < 1) {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
       return true;
    }

    public Map<Integer, String> getAvailableCourses(int userId, Role role) {
        Map<Integer, String> availableCourses = new HashMap<>();
        try (Connection connection = getDatabaseConnection()) {
            String query = "";
            if (role == Role.STUDENT) {
                query = "SELECT course_id, course_name FROM courses WHERE course_id NOT IN (SELECT course_id FROM student_course WHERE student_id = ?)";
            } else if (role == Role.INSTRUCTOR) {
                query = "SELECT course_id, course_name FROM courses WHERE course_id NOT IN (SELECT course_id FROM instructor_course WHERE instructor_id = ?)";
            }
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, userId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                int courseId = rs.getInt("course_id");
                String courseName = rs.getString("course_name");
                availableCourses.put(courseId, courseName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return availableCourses;
    }

    public boolean addOrUpdateStudentGrade(int courseId, int studentId, float grade, String gradeOrUpdate) {
        try {
            String query;
            int rowsInserted;
            if (gradeOrUpdate.equals("grade")) {
                query = "INSERT INTO grades (student_id, course_id, grade) VALUES (?, ?, ?)";
                rowsInserted = executeUpdate(query, studentId, courseId, grade);
            } else {
                query = "UPDATE grades SET grade = ? WHERE student_id = ? AND course_id = ?";
                rowsInserted = executeUpdate(query, grade, studentId, courseId);  // Note the order
            }
            if (rowsInserted < 1)
                return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Map<Integer, String> fetchStudentsWithGrades(int courseId, boolean hasGrades) {
        String query = "SELECT s.student_id, s.username FROM student_course sc " +
                "JOIN students s ON s.student_id = sc.student_id " +
                "WHERE sc.course_id = ? AND s.student_id " + (hasGrades ? "IN" : "NOT IN") +
                " (SELECT student_id FROM grades WHERE course_id = ?)";
        return fetchRecords(query, courseId, courseId);
    }

    private Map<Integer, String> fetchRecords(String query, Object... parameters) {
        Map<Integer, String> records = new HashMap<>();
        try (ResultSet rs = executeQuery(query, parameters)){
            while (rs.next()) {
                records.put(rs.getInt("student_id"), rs.getString("username"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    public List<StudentGrades> getAllCourseStudents(int courseId) {
        return fetchStudentGradesForCourse(courseId);
    }

    private List<StudentGrades> fetchStudentGradesForCourse(int courseId) {
        List<StudentGrades> studentGradesList = new ArrayList<>();
        String fetchGradesSql = "SELECT s.student_id, s.username, c.course_name, g.grade " +
                "FROM grades g " +
                "JOIN students s ON g.student_id = s.student_id " +
                "JOIN courses c ON g.course_id = c.course_id " +
                "WHERE g.course_id = ?";
        try(ResultSet rs = executeQuery(fetchGradesSql, courseId)) {
            while (rs.next()) {
                int studentId = rs.getInt("student_id");
                String studentName = rs.getString("username");
                String courseName = rs.getString("course_name");
                float grade = rs.getFloat("grade");
                CourseGrade courseGrade = new CourseGrade(courseId, courseName, grade);
                studentGradesList.add(new StudentGrades(studentId, studentName, Collections.singletonList(courseGrade)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return studentGradesList;
    }

    public String getDatabaseTable(int choice) {
        String tableName = "";
        switch (choice) {
            case 1 -> tableName = "students";
            case 2 -> tableName = "instructors";
            case 3 -> tableName = "courses";
        }
        return tableName;
    }

    public String getDbUsername(Role role, int id) {
        String query = "";
        String res = "";
        if (role.equals(Role.INSTRUCTOR) || role.equals(Role.STUDENT)) {
            query = "SELECT username FROM " + (role.equals(Role.INSTRUCTOR) ? "instructors" : "students") + " WHERE " +
                    (role.equals(Role.INSTRUCTOR) ? "instructor_id" : "student_id") + " = ?";
        }
        try (Connection connection = getDatabaseConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next())
                res = resultSet.getString("username");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    public boolean isUserIdExists(String id, String tableName) {
        String query = checkTableAccess(tableName);
        try (Connection connection = getDatabaseConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, id);
            ResultSet rs = statement.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String checkTableAccess(String tableName) {
        return switch (tableName) {
            case "students" -> "SELECT student_id FROM " + tableName + " WHERE student_id = ?";
            case "instructors" -> "SELECT instructor_id FROM " + tableName + " WHERE instructor_id = ?";
            case "courses" -> "SELECT course_id FROM " + tableName + " WHERE course_id = ?";
            default -> throw new IllegalArgumentException();
        };
    }
}