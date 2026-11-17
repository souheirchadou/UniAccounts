package com.uniaccounts;

import com.uniaccounts.audit.AuditLogger;
import com.uniaccounts.model.StudentAccount;
import com.uniaccounts.repository.AccountRepository;

/** Reads the database only (no insert): proves that the data survives after the program stops. */
public class ShowAccounts {
    public static void main(String[] args) {
        AccountRepository repo = AccountRepository.getInstance();
        System.out.println("Accounts found in uniaccounts.db: " + repo.count());
        for (StudentAccount a : repo.findAll()) {
            System.out.println(a);
        }
        System.out.println();
        AuditLogger.getInstance().print();
    }
}
