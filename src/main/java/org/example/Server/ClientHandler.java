package org.example.Server;

import org.example.Communication.Response;
import org.example.Model.*;
import org.example.Model.Role;
import java.io.*;
import java.net.Socket;

public class ClientHandler {
    private final Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void handleClient() {
        try (DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
             DataInputStream input = new DataInputStream(clientSocket.getInputStream())) {
            Role role;
            while (true) {
                int id = input.readInt();
                role = Role.valueOf(input.readUTF());
                String username = (role == Role.ADMIN) ? input.readUTF() : null;
                String password = input.readUTF();
                User user = new UserFactory().createUser(role, id, password);
                user.setUsername(username);
                if (user.isValidUser()) {
                    output.writeUTF("access");
                    output.writeUTF("Successfully logged in");
                    break;
                } else {
                    output.writeUTF("denied");
                    output.writeUTF("Wrong credentials. Please try again.");
                }
            }
            Response response = new Response();
            response.receive(input, output, role);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}