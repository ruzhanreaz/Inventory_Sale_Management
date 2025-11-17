package com.example.demo.patterns.strategy;

public class PrintStrategy implements ActionStrategy {
    @Override
    public void execute() {
        System.out.println("Print successful");
    }
}

