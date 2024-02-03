package org.example.Clients;

import org.example.Communication.Request;
import org.example.Connection.AuthenticatedConnection;
import org.example.Connection.Connection;
import org.example.Model.Role;
import org.example.Credentials.UserInfo;
import java.io.*;
import java.net.Socket;

public class InstructorClient {
    static Connection connection = new Connection();

    public static void main(String[] args) {
        try {
            AuthenticatedConnection authConnection = connection.establishConnection(Role.INSTRUCTOR);
            Socket socket = authConnection.socket();
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            DataInputStream input = new DataInputStream(socket.getInputStream());
            UserInfo credentials = authConnection.userInfo();
            Request request = new Request();
            request.send(input, output, credentials.getId(), Role.INSTRUCTOR);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}