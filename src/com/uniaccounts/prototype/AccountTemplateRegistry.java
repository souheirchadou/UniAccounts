package com.uniaccounts.prototype;

import com.uniaccounts.model.StudentAccount;

import java.util.HashMap;
import java.util.Map;

/**
 * PATTERN: Prototype -> ROLE: Prototype Registry (manager of templates).
 * One ready-made template per batch (e.g. "L3-INFO-2026"); new accounts are
 * produced by copying it and changing only the student's identity.
 */
public class AccountTemplateRegistry {

    private final Map<String, StudentAccount> templates = new HashMap<>();

    /** Stores a copy so later changes to the original do not alter the template. */
    public void register(String batchKey, StudentAccount template) {
        templates.put(batchKey, template.copy());
    }

    public StudentAccount createFrom(String batchKey, String id, String fullName, String email) {
        StudentAccount template = templates.get(batchKey);
        if (template == null)
            throw new IllegalArgumentException("Unknown template: " + batchKey);
        return template.copy().withIdentity(id, fullName, email);
    }
}
