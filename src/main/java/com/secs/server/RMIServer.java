package com.secs.server;

import com.secs.shared.ExamService;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIServer {
    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("   SECS Server Starting...");
        System.out.println("==========================================");
        
        try {
            // Check if registry already exists
            Registry registry;
            try {
                registry = LocateRegistry.getRegistry(1099);
                registry.list(); // Test if registry is alive
                System.out.println("✓ Connected to existing RMI Registry on port 1099");
            } catch (Exception e) {
                // Create new registry if none exists
                registry = LocateRegistry.createRegistry(1099);
                System.out.println("✓ Created new RMI Registry on port 1099");
            }
            
            // Create and bind service
            ExamServiceImpl examService = new ExamServiceImpl();
            
            // Unbind if already bound
            try {
                registry.unbind("ExamService");
                System.out.println("✓ Removed previous ExamService binding");
            } catch (Exception e) {
                // Service not bound yet, that's fine
            }
            
            // Bind the service
            registry.rebind("ExamService", examService);
            System.out.println("✓ ExamService bound successfully");
            
            System.out.println("\n==========================================");
            System.out.println("   SECS SERVER IS READY!");
            System.out.println("==========================================");
            
            System.out.println("\n📡 Server Information:");
            System.out.println("   Port: 1099");
            System.out.println("   Service: ExamService");
            System.out.println("   Status: Running");
            
            System.out.println("\n👤 Test Users:");
            System.out.println("   Students:");
            System.out.println("     • student1 / pass123");
            System.out.println("     • student2 / pass123");
            System.out.println("     • student3 / pass123");
            System.out.println("   Teachers:");
            System.out.println("     • teacher1 / admin123");
            System.out.println("     • teacher2 / admin123");
            
            System.out.println("\n📚 Available Exams (3 total)");
            System.out.println("\n==========================================");
            System.out.println("Server running. Press Ctrl+C to stop.");
            System.out.println("==========================================");
            
            // Keep server running
            Thread.sleep(Long.MAX_VALUE);
            
        } catch (Exception e) {
            System.err.println("\n❌ Server error: " + e.getMessage());
            e.printStackTrace();
            System.out.println("\n💡 Quick fix: Kill port 1099 and restart:");
            System.out.println("   1. Open CMD as Administrator");
            System.out.println("   2. Run: netstat -ano | findstr :1099");
            System.out.println("   3. Run: taskkill /PID [PID_NUMBER] /F");
        }
    }
}