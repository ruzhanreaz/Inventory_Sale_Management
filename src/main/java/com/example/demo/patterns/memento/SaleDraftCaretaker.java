package com.example.demo.patterns.memento;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SaleDraftCaretaker {
    private static final Map<String, SaleDraftMemento> drafts = new HashMap<>();

    public static void saveDraft(String phoneNumber, SaleDraftMemento memento) {
        drafts.put(phoneNumber, memento);
    }

    public static SaleDraftMemento getDraft(String phoneNumber) {
        return drafts.get(phoneNumber);
    }

    public static void removeDraft(String phoneNumber) {
        drafts.remove(phoneNumber);
    }

    public static void clearAllDrafts() {
        drafts.clear();
    }

    public static Set<String> getAllDraftPhones() {
        return drafts.keySet();
    }
}
