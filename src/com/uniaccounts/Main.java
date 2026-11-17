package com.uniaccounts;

import com.uniaccounts.audit.AuditLogger;
import com.uniaccounts.model.StudentAccount;
import com.uniaccounts.prototype.AccountTemplateRegistry;
import com.uniaccounts.repository.AccountRepository;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        AccountRepository repo = AccountRepository.getInstance();
        AuditLogger audit = AuditLogger.getInstance();

        // start the demo from a clean database
        repo.deleteAll();
        audit.clearHistory();

        // ---------- 1) BUILDER ----------
        title("1) Builder: readable construction with optional fields");
        StudentAccount s1 = new StudentAccount.Builder("20240001", "Sara Benali", "sara@univ.dz")
                .phone("0550000000")
                .level("L2")
                .major("Informatique")
                .scholarship(true)
                .addCourse("Algorithmique")
                .addCourse("Bases de donnees")
                .build();
        repo.add(s1);
        System.out.println(s1);

        StudentAccount s2 = new StudentAccount.Builder("20240002", "Amine Kaci", "amine@univ.dz").build();
        repo.add(s2);
        System.out.println("Defaults -> " + s2);

        tryRun("empty id", () -> new StudentAccount.Builder("", "X Y", "x@univ.dz").build());
        tryRun("email without @", () -> new StudentAccount.Builder("1", "X Y", "xunivdz").build());
        tryRun("invalid level", () -> new StudentAccount.Builder("2", "X Y", "x@univ.dz").level("L9").build());
        tryRun("duplicate id", () -> repo.add(
                new StudentAccount.Builder("20240001", "Another One", "a@univ.dz").build()));

        // ---------- 2) SINGLETON ----------
        title("2) Singleton: one repository and one audit log");
        System.out.println("Same repository instance? " + (repo == AccountRepository.getInstance()));
        System.out.println("Same audit instance?      " + (audit == AuditLogger.getInstance()));

        // ---------- 3) PROTOTYPE ----------
        title("3) Prototype: batch template, copy, change only the identity");
        StudentAccount template = new StudentAccount.Builder("TEMPLATE", "TEMPLATE", "template@univ.dz")
                .level("L3")
                .major("Informatique")
                .courses(List.of("Design Patterns", "Genie logiciel", "Reseaux"))
                .build();
        AccountTemplateRegistry registry = new AccountTemplateRegistry();
        registry.register("L3-INFO-2026", template);

        String[][] batch = {
                {"20260101", "Yacine Bensalem", "yacine@univ.dz"},
                {"20260102", "Nadia Ouali", "nadia@univ.dz"},
                {"20260103", "Rida Benbordi", "rida@univ.dz"}
        };
        for (String[] b : batch) {
            StudentAccount acc = registry.createFrom("L3-INFO-2026", b[0], b[1], b[2]);
            repo.add(acc);
            System.out.println(acc);
        }

        StudentAccount c1 = registry.createFrom("L3-INFO-2026", "X1", "Copy One", "c1@univ.dz");
        StudentAccount c2 = registry.createFrom("L3-INFO-2026", "X2", "Copy Two", "c2@univ.dz");
        System.out.println("Deep copy check -> same object? " + (c1 == c2)
                + " | same courses list? " + (c1.getCourses() == c2.getCourses()));

        // ---------- 4) DATABASE ----------
        title("4) Database: data read back from SQLite");
        System.out.println("Accounts stored in the database: " + repo.count());
        repo.findById("20260101").ifPresent(a -> System.out.println("Read from DB -> " + a));

        title("Audit log");
        audit.print();

        System.out.println("\nThe data is saved in uniaccounts.db. Run show.bat to read it again");
        System.out.println("after the program has stopped.");
    }

    private static void title(String t) {
        System.out.println("\n=== " + t + " ===");
    }

    private static void tryRun(String label, Runnable r) {
        try {
            r.run();
            System.out.println("[" + label + "] NO ERROR (unexpected)");
        } catch (RuntimeException e) {
            System.out.println("[" + label + "] rejected -> " + e.getMessage());
        }
    }
}
