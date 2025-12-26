package com.secs.client;

import com.secs.shared.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIClient {
    private ExamService examService;
    private boolean connected = false;

    public RMIClient(String host, int port) throws Exception {
        System.out.println("Connecting to server at " + host + ":" + port + "...");
        try {
            Registry registry = LocateRegistry.getRegistry(host, port);
            examService = (ExamService) registry.lookup("ExamService");

            // Test connection
            if (examService.isAlive()) {
                connected = true;
                System.out.println("✓ Connected to SECS Server successfully");
            } else {
                throw new Exception("Server is not alive");
            }

        } catch (Exception e) {
            connected = false;
            throw new Exception("Failed to connect to server: " + e.getMessage());
        }
    }

    public User login(String username, String password) throws Exception {
        if (!connected) throw new Exception("Not connected to server");
        return examService.login(username, password);
    }

    public ExamService getExamService() {
        return examService;
    }

    public boolean isConnected() {
        return connected;
    }
}