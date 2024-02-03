package org.example.View;

public class InstructorView extends View{
    @Override
    public String getDisplayOptions() {
        return "Please choose one of the following options:\n" +
        "1. View Courses\t\t2. Add Student Grade\t\t 3.Update Student Grade\t\t 4. Get All Course Students\t\t5. enroll in course";
    }

    @Override
    public void displayErrorMessage(String error){
        System.err.println(error);
    }

    @Override
    public void displayEndingMessage(String message) {
        System.out.println(message);
    }
}
