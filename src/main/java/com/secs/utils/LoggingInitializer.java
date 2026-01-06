package com.secs.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class LoggingInitializer {
    private static final Logger logger = LoggerFactory.getLogger(LoggingInitializer.class);

    public static void initialize() {
        try {
            // Set up MDC context
            MDC.put("application", "SECS");
            MDC.put("version", "2.0");
            MDC.put("developer", "Kuei Poch Kuei");
            MDC.put("university", "Dilla University");
            MDC.put("studentId", "CS/0032/14");

            // Get machine info
            String hostname = InetAddress.getLocalHost().getHostName();
            String ipAddress = getLocalIpAddress();

            MDC.put("hostname", hostname);
            MDC.put("ip", ipAddress);

            // Log initialization
            logger.info("╔══════════════════════════════════════════════════════╗");
            logger.info("║          SECS APPLICATION INITIALIZATION          ║");
            logger.info("╠══════════════════════════════════════════════════════╣");
            logger.info("║  Application: Secure Exam Control System (SECS)   ║");
            logger.info("║  Version: 2.0 Enterprise Edition                  ║");
            logger.info("║  Developer: Kuei Poch Kuei                        ║");
            logger.info("║  University: Dilla University                     ║");
            logger.info("║  Student ID: CS/0032/14                           ║");
            logger.info("║  Hostname: {}                                     ", hostname);
            logger.info("║  IP Address: {}                                   ", ipAddress);
            logger.info("║  Java Version: {}                                 ", System.getProperty("java.version"));
            logger.info("║  JavaFX Version: 21                               ║");
            logger.info("╚══════════════════════════════════════════════════════╝");

            logger.info("Logging system initialized successfully");

        } catch (Exception e) {
            System.err.println("CRITICAL: Failed to initialize logging: " + e.getMessage());
            e.printStackTrace();
            // Don't throw - we want to at least try to start
        }
    }

    private static String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (!addr.isLoopbackAddress() && addr.getHostAddress().contains(".")) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Could not determine IP address: {}", e.getMessage());
        }
        return "127.0.0.1";
    }

    public static void shutdown() {
        logger.info("Logging system shutting down...");
        MDC.clear();
    }
}