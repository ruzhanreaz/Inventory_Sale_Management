package com.example.demo.patterns.strategy;

public class EmailStrategy implements ActionStrategy {
    @Override
    public void execute() {
        System.out.println("Email successful");
    }
}
