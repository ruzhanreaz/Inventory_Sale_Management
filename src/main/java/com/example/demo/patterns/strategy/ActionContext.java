package com.example.demo.patterns.strategy;

public class ActionContext {
    private ActionStrategy strategy;

    public void setStrategy(ActionStrategy strategy) {
        this.strategy = strategy;
    }

    public void executeStrategy() {
        if (strategy != null) {
            strategy.execute();
        } else {
            System.out.println("No strategy set");
        }
    }
}

