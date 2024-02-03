package org.example.Connection;

import org.example.Model.Role;
import org.example.Credentials.UserInfo;

import java.io.*;
import java.net.Socket;

public class Connection {
    public AuthenticatedConnection establishConnection(Role role) throws IOException {
        System.out.println("Welcome " + role);
        UserInfo userCredentials = new UserInfo();
        Socket socket = new Socket("127.0.0.1", 8080);
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        DataInputStream input = new DataInputStream(socket.getInputStream());
        while (true) {
            try {
                userCredentials.chooseUserCredentials(role); //who am i
                output.writeInt(userCredentials.getId());
                output.flush();
                output.writeUTF(role.toString());
                output.flush();
                if (role == Role.ADMIN) {
                    output.writeUTF(userCredentials.getUsername());
                    output.flush();
                }
                output.writeUTF(userCredentials.getPassword());
                output.flush();
                String response = input.readUTF();
                if (response.equals("access")) {
                    System.out.println(input.readUTF()); //successfully logged in message
                    return new AuthenticatedConnection(socket, userCredentials);
                } else {
                    System.out.println(input.readUTF()); //wrong credentials message
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
        throw new RuntimeException("Failed to establish connection.");
    }
}
