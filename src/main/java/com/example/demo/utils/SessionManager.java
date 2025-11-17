package com.example.demo.utils;

public class SessionManager {
    private static String userType;
    private static int userId;
    private static boolean navigatedFromDashboard = false;

    public static void setUserType(String type) {
        userType = type;
    }

    public static String getUserType() {
        return userType;
    }

    public static void setUserId(int id) {
        userId = id;
    }

    public static int getUserId() {
        return userId;
    }

    public static void setNavigatedFromDashboard(boolean value) {
        navigatedFromDashboard = value;
    }

    public static boolean isNavigatedFromDashboard() {
        return navigatedFromDashboard;
    }
}
