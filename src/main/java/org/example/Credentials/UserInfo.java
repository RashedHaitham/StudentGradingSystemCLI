package org.example.Credentials;

import org.example.Model.Role;
import org.example.Util.PasswordHashing;

import java.util.Scanner;

public class UserInfo {
    private int id;
    private String password;
    private String username;

    public void chooseUserCredentials(Role role) {
        Scanner input = new Scanner(System.in);
        if (role.equals(Role.ADMIN)) {
            System.out.print("\nHello " + role + ", please enter your username: ");
            username = input.nextLine();
        } else {
            while (true) {
                System.out.print("\nHello " + role + ", please enter your ID: ");
                String userInput = input.nextLine();
                if (userInput.matches("\\d{7}")) {
                    id = Integer.parseInt(userInput);
                    break;
                } else {
                    System.out.println("Please enter an ID number that consists of 7 digits.");
                }
            }
        }
        System.out.print("Enter your password: ");
        String pass = input.nextLine();
        if(role.equals(Role.ADMIN)) {
            password = pass;
            return;
        }
        password = PasswordHashing.hashPassword(pass);
        System.out.println();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getId() {
        return id;
    }
}
