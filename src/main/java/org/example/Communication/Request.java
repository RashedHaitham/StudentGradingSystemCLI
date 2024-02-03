package org.example.Communication;

import org.example.Model.Role;
import org.example.View.*;
import java.io.*;
import java.util.Scanner;

public class Request {
    AdminView adminView = new AdminView();
    private final Scanner inputScanner = new Scanner(System.in);

    public void send(DataInputStream input, DataOutputStream output, int id, Role role) throws IOException, ClassNotFoundException {
        switch (role) {
            case ADMIN -> handleAdminOptions(input, output);
            case STUDENT -> handleStudentOption(input, output, id);
            case INSTRUCTOR -> handleInstructorOption(input, output, id);
            default -> throw new IllegalStateException("Unsupported role");
        }
    }

    private void handleAdminOptions(DataInputStream input, DataOutputStream output) throws IOException {
        // Display initial options to the admin user.
        System.out.println(input.readUTF()); // Display options
        int option = adminView.getOptionValue(1, 3); // Get the option selected by the admin.
        output.writeInt(option); // Send the selected option to the server.

        boolean continueSession = true; // Control the loop based on user's decision to continue.

        while (continueSession) {
            // Read and display table content and CRUD options to the admin.
            System.out.println(input.readUTF()); // Read and display table content.
            System.out.println(input.readUTF()); // Read and display CRUD options.

            // Get the CRUD operation choice from the admin.
            int crudOption = adminView.getOptionValue(1, 5);
            // Execute the CRUD operation.
            CRUD(input, output, crudOption);

            // Ask the admin if they wish to continue after the CRUD operation is complete.
            System.out.println(input.readUTF()); // Prompt "Do you wish to continue.."
            String check = inputScanner.next(); // Read the admin's decision.
            output.writeUTF(check); // Send the admin's decision to the server.

            // Update the loop control variable based on the admin's decision.
            continueSession = check.equalsIgnoreCase("yes");
        }
        adminView.displayEndingMessage(input.readUTF());
    }
    private void addUser(DataInputStream input, DataOutputStream output) throws IOException {

         while (true) {
             System.out.println(input.readUTF()); // Read input message (7-digit number)
             String id = inputScanner.next();
             output.writeUTF(id); // Send entered user's id to server
             if (!input.readBoolean()) { //check if id is equal to 7
                 adminView.displayErrorMessage(input.readUTF());
                 continue;
             }
             boolean checkUserExistCondition = input.readBoolean();
             if (!checkUserExistCondition) {
                 adminView.displayErrorMessage(input.readUTF());
                 continue;
             }
             break;
         }
         int fieldSize = input.readInt();
         for (int i = 1; i < fieldSize; i++) {
             System.out.println(input.readUTF()); // Read fieldName print message
             output.writeUTF(inputScanner.next()); // Send value
         }
         if (!input.readUTF().equals("courses")) {
             System.out.println(input.readUTF()); // Enter password message
             String userPassword = inputScanner.next();
             output.writeUTF(userPassword); // Send user
         }
         System.out.println(input.readUTF()); // Record added successfully


     }
    private boolean IsValidUserId(DataInputStream input, DataOutputStream output, View view) throws IOException {
            System.out.println(input.readUTF()); // Read id msg
            String id = inputScanner.next();
            output.writeUTF(id); // Send entered user's id to server
            if (!input.readBoolean()) { //check if id is equal to 7
                view.displayErrorMessage(input.readUTF());
                return false;
            }
           return true;
    }
    private boolean IsValidCourseId(DataInputStream input, DataOutputStream output,View view) throws IOException {
        System.out.println(input.readUTF()); // Read id msg
        int id = inputScanner.nextInt();
        output.writeInt(id); // Send entered user's id to server
        if (!input.readBoolean()) { //check if id is equal to 7
            view.displayErrorMessage(input.readUTF());
            return false;
        }
        return true;
    }
    private void updateUserRecord(DataInputStream input,DataOutputStream output) throws IOException {
        System.out.println(input.readUTF()); //read select the column message
         int columnSize = input.readInt(); //read column size
         for (int i = 0; i < columnSize; i++) {
             System.out.println(input.readUTF()); //read columns
         }
         int columnChoice = adminView.getOptionValue(1, 2);
         output.writeInt(columnChoice); //send chosen column
         while (true){
            if (IsValidUserId(input,output,adminView))break;
         }
         while (true){
             if (IsValidUserId(input,output,adminView))break;
         }
         System.out.println(input.readUTF()); //update status
     }
    private void CRUD(DataInputStream input, DataOutputStream output, int crudOption) throws IOException {
        output.writeInt(crudOption); //send crud option selected by user
        switch (crudOption) {
            case 1 -> addUser(input,output);
            case 2 -> System.out.println(input.readUTF()); //get table content
            case 3 -> updateUserRecord(input,output);
            case 4 -> {
                while (true){
                    if (IsValidUserId(input,output,adminView))break;
                }
                System.out.println(input.readUTF()); //successfully deleted message
            }
            case 5 -> handleAdminOptions(input, output);
        }
    }
    private void handleStudentOption(DataInputStream input, DataOutputStream output, int id) throws IOException {
        StudentView studentView = new StudentView();
        output.writeInt(id);
        String check = "";
        while (!check.equalsIgnoreCase("no")) {
            System.out.println(input.readUTF()); //read display options
            int option = studentView.getOptionValue(1, 3);
            output.writeInt(option); //send option selected by user to server
            switch (option) {
                case 1 -> System.out.println(input.readUTF()); //read view grades
                case 2 -> System.out.println(input.readUTF()); //read courses taken
                case 3 -> {
                    while (true) {
                        System.out.println(input.readUTF()); //get available courses
                        if (IsValidUserId(input,output,studentView))break;
                    }
                    System.out.println(input.readUTF()); //read successfully enrolled message
                }
            }
            System.out.println(input.readUTF()); //do you wish to continue..
            check = inputScanner.next();
            output.writeUTF(check); //send entered value if no stop
        }
        studentView.displayEndingMessage(input.readUTF()); //thank you for your visit...
    }

    private void handleInstructorOption(DataInputStream input, DataOutputStream output, int id) throws IOException {
        InstructorView instructorView = new InstructorView();
        output.writeInt(id);
        String check = "";
        while(!check.equalsIgnoreCase("no")){
            System.out.println(input.readUTF()); //read display options
            int option = instructorView.getOptionValue(1, 5);
            output.writeInt(option); //send option selected by user
            switch (option) {
                case 1 -> System.out.println(input.readUTF()); //read courses taken
                case 2, 3 -> {
                    while (true) {
                        System.out.println(input.readUTF()); //read courses taken
                        if (IsValidCourseId(input,output,instructorView))break;
                    }
                    while (true) {
                        boolean anyStudentExist = input.readBoolean();
                        if (!anyStudentExist) {
                            instructorView.displayErrorMessage(input.readUTF());
                            return;
                        }
                        System.out.println(input.readUTF()); //read students
                        if (IsValidCourseId(input,output,instructorView))break;
                    }
                    System.out.println(input.readUTF()); //read enter grade message
                    float grade = inputScanner.nextFloat();
                    output.writeFloat(grade);
                    boolean isStudentGradeAdded = input.readBoolean();
                    if (!isStudentGradeAdded) {
                        instructorView.displayErrorMessage(input.readUTF());
                        continue;
                    }
                    System.out.println(input.readUTF()); //grade added successfully
                }
                case 4 -> {
                    while (true) {
                        System.out.println(input.readUTF()); //read courses
                        if (IsValidCourseId(input,output,instructorView))break;
                    }
                    System.out.println(input.readUTF()); //read grades
                }
                case 5 -> {
                    while (true) {
                        System.out.println(input.readUTF()); //get available courses
                        if (IsValidCourseId(input,output,instructorView))break;
                    }
                    System.out.println(input.readUTF()); //read successfully enrolled message
                }
            }
            System.out.println(input.readUTF()); //do you wish to continue..
            check = inputScanner.next();
            output.writeUTF(check); //send entered value if no stop
        }
        instructorView.displayEndingMessage(input.readUTF()); //thank you for your visit instructor
    }

}