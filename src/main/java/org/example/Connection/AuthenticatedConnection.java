package org.example.Connection;

import org.example.Credentials.UserInfo;

import java.net.Socket;

public record AuthenticatedConnection(Socket socket, UserInfo userInfo) {
}