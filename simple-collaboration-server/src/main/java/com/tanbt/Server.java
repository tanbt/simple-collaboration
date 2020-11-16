package com.tanbt;

import java.io.IOException;

public class Server {
    static String HOST = "localhost";
    static int PORT = 7070;

    public static void main(String[] args) throws IOException {
        if (args.length > 0) {
            PORT = Integer.valueOf(args[0]);
        }

        InternalServer server = new InternalServer(HOST, PORT);
        server.start();
        System.in.read();
    }


}

