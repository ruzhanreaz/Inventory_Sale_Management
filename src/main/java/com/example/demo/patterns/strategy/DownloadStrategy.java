package com.example.demo.patterns.strategy;

public class DownloadStrategy implements ActionStrategy {
    @Override
    public void execute() {
        System.out.println("Download successful");
    }
}

