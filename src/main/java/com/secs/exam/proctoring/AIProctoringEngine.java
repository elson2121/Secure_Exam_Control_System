package com.secs.exam.proctoring;

import com.secs.security.core.SecurityCore;
import java.time.LocalDateTime;
import java.util.*;

public class AIProctoringEngine {
    private static AIProctoringEngine instance;
    private SecurityCore securityCore;
    private Timer proctoringTimer;
    private List<ProctoringEvent> events = new ArrayList<>();
    
    private long lastFaceDetectionTime = 0;
    private long lastEyeDetectionTime = 0;
    
    private static final String DEVELOPER_TAG = 
        "KueiPochKuei-AI-Proctoring-DillaUniversity-CS";
    
    private AIProctoringEngine() {
        this.securityCore = SecurityCore.getInstance();
        initializeProctoringEngine();
    }
    
    public static AIProctoringEngine getInstance() {
        if (instance == null) {
            synchronized (AIProctoringEngine.class) {
                if (instance == null) {
                    instance = new AIProctoringEngine();
                }
            }
        }
        return instance;
    }
    
    private void initializeProctoringEngine() {
        System.out.println("🤖 Initializing AI Proctoring Engine...");
        System.out.println("🧠 " + DEVELOPER_TAG);
        
        lastFaceDetectionTime = System.currentTimeMillis();
        lastEyeDetectionTime = System.currentTimeMillis();
        
        startProctoring();
        
        securityCore.logSecurityEvent("PROCTORING_INIT", 
            "AI Proctoring Engine initialized", "HIGH");
    }
    
    private void startProctoring() {
        proctoringTimer = new Timer(true);
        
        proctoringTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                monitorFacePresence();
            }
        }, 0, 3000);
        
        proctoringTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                monitorEyeMovement();
            }
        }, 0, 2000);
        
        proctoringTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                analyzeBehavior();
            }
        }, 0, 10000);
    }
    
    private void monitorFacePresence() {
        boolean faceDetected = simulateFaceDetection();
        
        if (faceDetected) {
            lastFaceDetectionTime = System.currentTimeMillis();
        } else {
            long timeAway = System.currentTimeMillis() - lastFaceDetectionTime;
            if (timeAway > 10000) {
                logProctoringEvent("FACE_AWAY", 
                    "Candidate face not detected for " + (timeAway/1000) + " seconds", 
                    "HIGH");
            }
        }
    }
    
    private void monitorEyeMovement() {
        boolean eyesOnScreen = simulateEyeTracking();
        
        if (eyesOnScreen) {
            lastEyeDetectionTime = System.currentTimeMillis();
        } else {
            long timeAway = System.currentTimeMillis() - lastEyeDetectionTime;
            if (timeAway > 5000) {
                logProctoringEvent("EYES_AWAY", 
                    "Eyes away from screen for " + (timeAway/1000) + " seconds", 
                    "MEDIUM");
            }
        }
    }
    
    private void analyzeBehavior() {
        boolean suspiciousMovement = simulateMovementAnalysis();
        boolean multipleFaces = simulateMultipleFaceDetection();
        
        if (suspiciousMovement) {
            logProctoringEvent("SUSPICIOUS_MOVEMENT", 
                "Unusual movement pattern detected", "HIGH");
        }
        
        if (multipleFaces) {
            logProctoringEvent("MULTIPLE_FACES", 
                "Multiple faces detected in frame", "CRITICAL");
        }
    }
    
    private boolean simulateFaceDetection() {
        return Math.random() > 0.05;
    }
    
    private boolean simulateEyeTracking() {
        return Math.random() > 0.1;
    }
    
    private boolean simulateMovementAnalysis() {
        return Math.random() < 0.05;
    }
    
    private boolean simulateMultipleFaceDetection() {
        return Math.random() < 0.02;
    }
    
    private void logProctoringEvent(String eventType, String description, String severity) {
        ProctoringEvent event = new ProctoringEvent(
            UUID.randomUUID().toString().substring(0, 8),
            eventType,
            description,
            severity,
            LocalDateTime.now()
        );
        
        events.add(event);
        securityCore.logSecurityEvent("PROCTORING_" + eventType, description, severity);
        
        if ("CRITICAL".equals(severity)) {
            System.out.println("🚨 PROCTORING ALERT: " + eventType + " - " + description);
        }
    }
    
    public void generateProctoringReport() {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║              AI PROCTORING REPORT                   ║");
        System.out.println("╠══════════════════════════════════════════════════════╣");
        System.out.println("║  Developer: Kuei Poch Kuei - Dilla University CS    ║");
        System.out.println("║  Engine: AI-Powered Proctoring v1.0                 ║");
        System.out.println("║  Total Events: " + events.size() + "                               ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
    }
    
    public void shutdown() {
        if (proctoringTimer != null) {
            proctoringTimer.cancel();
        }
        
        generateProctoringReport();
        securityCore.logSecurityEvent("PROCTORING_SHUTDOWN", 
            "AI Proctoring Engine terminated", "HIGH");
        
        System.out.println("AI Proctoring Engine Shutdown Complete");
    }
    
    public static class ProctoringEvent {
        private String id;
        private String eventType;
        private String description;
        private String severity;
        private LocalDateTime timestamp;
        
        public ProctoringEvent(String id, String eventType, String description, 
                              String severity, LocalDateTime timestamp) {
            this.id = id;
            this.eventType = eventType;
            this.description = description;
            this.severity = severity;
            this.timestamp = timestamp;
        }
    }
}