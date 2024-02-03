package org.example.Clients;

import org.example.Communication.Request;
import org.example.Connection.AuthenticatedConnection;
import org.example.Connection.Connection;
import org.example.Model.Role;
import org.example.Credentials.UserInfo;
import java.io.*;
import java.net.Socket;

public class AdminClient {
    private static final Connection connection = new Connection();
    public static void main(String[] args) {
        try {
            AuthenticatedConnection authenticatedConnection = connection.establishConnection(Role.ADMIN);
            UserInfo userCredentials = authenticatedConnection.userInfo();
            Socket socket = authenticatedConnection.socket();
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            DataInputStream input = new DataInputStream(socket.getInputStream());
            System.out.println("Requesting database tables......");
            Thread.sleep(500);
            Request request = new Request();
            request.send(input, output, userCredentials.getId(), Role.ADMIN);
        }catch (IOException | ClassNotFoundException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}