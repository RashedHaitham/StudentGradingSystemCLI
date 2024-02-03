package org.example.View;


public class AdminView extends View{
    @Override
    public String getDisplayOptions() {
        return "Please choose one of the following tables:\n" + "1. students\t\t2. instructors\t\t3. courses";
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
