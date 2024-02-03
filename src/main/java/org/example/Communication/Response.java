package org.example.Communication;

import org.example.Model.StudentGrades;
import org.example.Util.PasswordHashing;
import org.example.Util.Printer;
import org.example.Model.Role;
import org.example.Server.Database;
import org.example.View.*;
import java.io.*;
import java.util.*;

public class Response {
    static Database database = Database.getInstance();

    public void receive(DataInputStream input, DataOutputStream output, Role role) throws IOException, ClassNotFoundException {
        switch (role) {
            case ADMIN -> handleAdminOptions(input, output);
            case STUDENT -> handleStudentOptions(input, output);
            case INSTRUCTOR -> handleInstructorOptions(input, output);
            default -> throw new IllegalStateException("Unsupported role");
        }
    }

    /*******************************ADMIN*******************************************/
    private void handleAdminOptions(DataInputStream input, DataOutputStream output) throws IOException {
        AdminView adminView = new AdminView();
        output.writeUTF(adminView.getDisplayOptions());
        boolean continueSession = true;
        int option = input.readInt(); // Read option selected by user
        String table = database.getDatabaseTable(option);
        while (continueSession) {

            List<String> columns = database.getTableColumns(table);
            List<String[]> tableContent = database.getTableContent(table);
            String content = Printer.displayTableContent(columns, tableContent);
            output.writeUTF(content); // Send table content
            output.writeUTF(database.getCrudOptions(table)); // Send CRUD options

            int chosenCRUD = input.readInt(); // Read CRUD option selected by user
            handleCrudOperation(chosenCRUD, input, output, table, columns, content);

            continueSession = promptForContinuation(input, output);
        }
        output.writeUTF("ADMIN logged out of the system.");
    }
    private void handleCrudOperation(int chosenCRUD, DataInputStream input, DataOutputStream output, String table, List<String> columns, String content) throws IOException {
        switch (chosenCRUD) {
            case 1 -> createUser(input, output, table, columns);
            case 2 -> output.writeUTF(content);
            case 3 -> updateRecord(input, output, table, columns);
            case 4 -> deleteRecord(input, output, table);
            case 5 -> handleAdminOptions(input, output);
            default -> output.writeUTF("Invalid CRUD option. Please select a valid option."); // Inform the user and loop for a valid option.
        }
    }
    private void deleteRecord(DataInputStream input, DataOutputStream output, String table) throws IOException {
        while (true) {
            output.writeUTF("Enter the ID of the record you want to delete:");
            String idToDelete = input.readUTF();
            boolean isDeleted = database.deleteRecord(table, idToDelete);
            if (!isDeleted) {
                output.writeBoolean(false);
                output.writeUTF("No record was found with the specified ID.");
                continue;
            }
            output.writeBoolean(true);
            break;
        }
        output.writeUTF("Successfully deleted the record.");
    }
    private void updateRecord(DataInputStream input, DataOutputStream output, String table, List<String> columns) throws IOException {
        output.writeUTF("Select the column you want to update:");
        output.writeInt(columns.size());
        for (int i = 0; i < columns.size(); i++) {
            output.writeUTF((i + 1) + ". " + columns.get(i));
        }
        int columnChoice = input.readInt();
        String columnToUpdate = columns.get(columnChoice - 1);
        String primaryKeyColumn = columns.get(0);
        String idToUpdate, newValue;
        while (true) {
            output.writeUTF("Enter the ID of the record you want to update: ");
            idToUpdate = input.readUTF();
            boolean isIdExists = database.isUserIdExists(idToUpdate, table); //check if it exists or not (true:exists:re-enter id)
            if (!isIdExists) {
                output.writeBoolean(false);
                output.writeUTF("Error: " + columns.get(0) + " ID doesn't exist. Please enter one of the displayed id's!");
                continue;
            }
            output.writeBoolean(true);
            break;
        }
        while (true) {
            output.writeUTF("\nEnter the new value for " + columnToUpdate + ": ");
            newValue = input.readUTF();
            boolean isUserExists = database.isUserIdExists(newValue, table); //check if it exists or not (true:exists:re-enter id)
            if (isUserExists) {
                output.writeBoolean(false);
                output.writeUTF("Error: " + columns.get(0) + " ID already exists. Please enter a different ID.");
                continue;
            }
            output.writeBoolean(true);
            break;
        }
        boolean isUpdated = database.updateRecord(table, columnToUpdate,
                primaryKeyColumn, idToUpdate, newValue);
        if (isUpdated) {
            output.writeUTF("Successfully updated the record.");
        } else {
            output.writeUTF("No record was found with the specified ID.");
        }
    }
    private void createUser(DataInputStream input, DataOutputStream output, String table, List<String> columns) throws IOException {
        String userId;
        while (true) {
            output.writeUTF("Enter " + columns.get(0) + " (7-digit numeric ID): ");
            userId = input.readUTF(); //read user's id from client
            if (!userId.matches("\\d{7}")) {
                output.writeBoolean(false);
                output.writeUTF("Invalid ID format. ID must be a 7-digit numeric value.");
                continue;
            }
            output.writeBoolean(true); //if id was exactly 7 (true)
            boolean isUserExists = database.isUserIdExists(userId, table); //check if it exists or not (true:exists:re-enter id)
            if (isUserExists) {
                output.writeBoolean(false);
                output.writeUTF("Error: " + columns.get(0) + " already exists. Please enter a different ID.");
                continue;
            }
            output.writeBoolean(true);
            break;
        }
        Map<String, String> inputData = new HashMap<>();
        inputData.put(columns.get(0), userId); //add username
        output.writeInt(columns.size()); //send field size to client
        for (String fieldName : columns) {
            if (!fieldName.equals(columns.get(0))) { //since it is already printed and entered
                output.writeUTF("Enter " + fieldName + ": ");
                String value = input.readUTF(); //read field value from user
                inputData.put(fieldName, value);
            }
        }
        output.writeUTF(table); //send table name to user
        if (!Objects.equals(table, "courses")) {
            output.writeUTF("Enter user's password: ");
            String userPassword = input.readUTF(); //read user's password and hash it
            String hashedPassword = PasswordHashing.hashPassword(userPassword);
            inputData.put("password", hashedPassword);
            System.out.println();
        }
        boolean isAdded = database.addRecord(table, inputData); //execute the add record
        if (isAdded) {
            output.writeUTF("Record added successfully");
        } else {
            output.writeUTF("Record wasn't added an error occurred");
        }
    }

    /*******************************STUDENT*******************************************/
    private void handleStudentOptions(DataInputStream input, DataOutputStream output) throws IOException {
        StudentView studentView = new StudentView();
        int studentId = input.readInt();
        boolean continueSession = true;
        while (continueSession) {
            output.writeUTF(studentView.getDisplayOptions());
            int option = input.readInt(); //read option selected by user
            switch (option) {
                case 1 -> sendStudentGrades(output,studentId);
                case 2 -> sendStudentCourses(output,studentId);
                case 3 -> enrollInCourse(input,output,studentId,Role.STUDENT);
            }
            continueSession = promptForContinuation(input, output);
        }
        output.writeUTF(getLogoutMessage(Role.STUDENT,studentId));

    }
    private void sendStudentGrades(DataOutputStream output, int studentId) throws IOException {
        StudentGrades studentGrades = database.viewGrades(studentId);
        String grades = Printer.displayStudentGradesInfo(studentGrades.getStudentId(), studentGrades.getStudentName(), studentGrades.getCourseGrades());
        output.writeUTF(grades); // Send view grades to user
    }
    private void sendStudentCourses(DataOutputStream output, int studentId) throws IOException {
        Map<Integer, String> courseList = database.viewCourses(studentId, Role.STUDENT);
        String courses = Printer.displayCourses(studentId, database.getDbUsername(Role.STUDENT, studentId), courseList);
        output.writeUTF(courses);
    }
    /*******************************INSTRUCTOR*******************************************/
    private void handleInstructorOptions(DataInputStream input, DataOutputStream output) throws IOException {
        InstructorView instructorView = new InstructorView();
        int instructorId = input.readInt();
        boolean continueSession = true;
        while (continueSession) {
            output.writeUTF(instructorView.getDisplayOptions());
            int option = input.readInt(); //read option selected by user
            switch (option) {
                case 1 -> sendInstructorCourses(output,instructorId);
                case 2, 3 -> gradeOrUpdateStudents(input,output,instructorId,option);
                case 4 -> viewStudentsAndGrades(input,output,instructorId);
                case 5 -> enrollInCourse(input,output,instructorId,Role.INSTRUCTOR);
            }
            continueSession = promptForContinuation(input, output);
        }
        output.writeUTF(getLogoutMessage(Role.INSTRUCTOR,instructorId));
    }
    private void sendInstructorCourses(DataOutputStream output, int instructorId) throws IOException {
        Map<Integer, String> courseList = database.viewCourses(instructorId, Role.INSTRUCTOR);
        String courses = Printer.displayCourses(instructorId, database.getDbUsername(Role.INSTRUCTOR, instructorId), courseList);
        output.writeUTF(courses);
    }
    private void gradeOrUpdateStudents(DataInputStream input, DataOutputStream output, int instructorId, int option) throws IOException {
        String gradeOrUpdate = (option == 2  ? "grade" : "update");
        int courseId;
        while (true) {
            Map<Integer, String> courseList = database.viewCourses(instructorId, Role.INSTRUCTOR);
            String courses = Printer.displayCourses(instructorId, database.getDbUsername(Role.INSTRUCTOR, instructorId), courseList);
            output.writeUTF(courses);
            output.writeUTF("Enter the Course ID to " + gradeOrUpdate + ": ");
            courseId = input.readInt();
            if (!courseList.containsKey(courseId)) {
                output.writeBoolean(false);
                output.writeUTF("Invalid course ID. Please try again.");
                continue;
            }
            output.writeBoolean(true);
            break;
        }
        int studentId;
        while(true) {
            Map<Integer, String> students = (option == 2)
                    ? database.fetchStudentsWithGrades(courseId, false)
                    : database.fetchStudentsWithGrades(courseId, true);
            String studentsToGrade = Printer.displayStudents(students);
            if(studentsToGrade.equals("No students found")){
                output.writeBoolean(false);
                output.writeUTF("No students found");
                return;
            }
            output.writeBoolean(true);
            output.writeUTF(studentsToGrade);
            output.writeUTF("Enter the Student ID to " + gradeOrUpdate + ": ");
            studentId = input.readInt();
            if(!students.containsKey(studentId)){
                output.writeBoolean(false);
                output.writeUTF("Enter student ID to " + gradeOrUpdate + ": ");
                continue;
            }
            output.writeBoolean(true);
            break;
        }
        output.writeUTF("Enter grade for the student: ");
        float grade = input.readFloat();
        boolean isStudentGradeAdded = database.addOrUpdateStudentGrade(courseId, studentId, grade, gradeOrUpdate);
        if(!isStudentGradeAdded) {
            output.writeBoolean(false);
            output.writeUTF("Failed to perform operation on grade.");
        }
        output.writeBoolean(true);
        output.writeUTF("Grade added successfully!");
    }
    private void viewStudentsAndGrades(DataInputStream input, DataOutputStream output, int instructorId) throws IOException {
        int courseId;
        while (true){
            Map<Integer, String> courseList = database.viewCourses(instructorId, Role.INSTRUCTOR);
            String courses = Printer.displayCourses(instructorId, database.getDbUsername(Role.INSTRUCTOR, instructorId), courseList);
            output.writeUTF(courses);
            output.writeUTF("Enter the course ID to view students and their grades:");
            courseId = input.readInt();
            if (!courseList.containsKey(courseId)) {
                output.writeBoolean(false);
                output.writeUTF("Invalid course ID. Please try again.");
                continue;
            }
            output.writeBoolean(true);
            break;
        }
        List<StudentGrades> studentGrades = database.getAllCourseStudents(courseId);
        String grades = Printer.displayStudentsWithGrades(studentGrades);
        output.writeUTF(grades);
    }
    /*******************************SHARED*******************************************/
    private String getLogoutMessage(Role role,int id) {
        return database.getDbUsername(role, id) + " logged out of the system";
    }
    private void enrollInCourse(DataInputStream input, DataOutputStream output, int userId, Role role) throws IOException {
        while (true){
            Map<Integer, String> availableCourses = database.getAvailableCourses(userId, role);
            String courses = Printer.displayAvailableCourses(availableCourses);
            output.writeUTF(courses);
            output.writeUTF("Enter the course ID to enroll/assign: ");
            String courseId = input.readUTF();

            // Determine the appropriate table based on the user's role
            String table = role == Role.STUDENT ? "student_course" : "instructor_course";

            boolean isEnrolled = database.enrollCourse(userId, role, table, courseId);
            if (!isEnrolled) {
                output.writeBoolean(false);
                output.writeUTF("Failed to enroll/assign the course. Make sure the course exists and you are eligible.");
            } else {
                output.writeBoolean(true);
                output.writeUTF("Successfully enrolled in the course!");
                break;
            }
        }
    }
    private boolean promptForContinuation(DataInputStream input, DataOutputStream output) throws IOException {
        String check;
        do {
            output.writeUTF("Do you wish to continue (yes/no)?: ");
            check = input.readUTF();
            if (check.equalsIgnoreCase("yes")) {
                return true;
            } else if (check.equalsIgnoreCase("no")) {
                return false;
            } else {
                output.writeUTF("Invalid response. Please enter 'yes' or 'no'.");
            }
        } while (!check.equalsIgnoreCase("yes") && !check.equalsIgnoreCase("no"));
        return false;
    }



}