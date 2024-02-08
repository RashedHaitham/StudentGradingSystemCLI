package org.example.View;

import java.util.InputMismatchException;
import java.util.Scanner;

public abstract class View {
    protected Scanner input = new Scanner(System.in);
    public abstract String getDisplayOptions();
    public abstract void displayErrorMessage(String error);
    public abstract void displayEndingMessage(String message);
    public int getOptionValue(int min, int max) {
        int option = 0;
        boolean valid = false;

        while (!valid) {
            try {
                System.out.print("Please enter your option: ");
                option = input.nextInt();

                // Check if the option is within the specified range
                if (option >= min && option <= max) {
                    valid = true; // Break out of the loop if valid
                } else {
                    // Inform the user the option is out of bounds and continue prompting
                    displayErrorMessage("Please enter a number between " + min + " - " + max + " only!");
                }
            } catch (InputMismatchException e) {
                // Handle non-integer inputs by displaying an error message
                displayErrorMessage("Invalid input. Please enter a number.");
                // Clear the scanner's buffer
                input.next();
            }
        }
        return option;
    }

}
