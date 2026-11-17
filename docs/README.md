# UniAccounts - Travail 1 (Builder, Singleton, Prototype + SQLite)

## Run (Windows)
1. Install JDK 17+ (`java -version` and `javac -version` must work).
2. Unzip the project to `C:\UniAccounts2` (a path without spaces).
3. Double-click / run `run.bat` in that folder.
   - It downloads the SQLite driver (lib\sqlite-jdbc.jar) the first time (needs internet).
   - It compiles and runs the demo.
4. Run `show.bat` afterwards: it only READS uniaccounts.db and shows that the data is still there.

Manual commands:
    dir /s /b src\*.java > sources.txt
    javac -d out -cp lib\sqlite-jdbc.jar @sources.txt
    java -cp "out;lib\sqlite-jdbc.jar" com.uniaccounts.Main

## Structure
| Package | Class | Role |
|---|---|---|
| model | StudentAccount (+ nested Builder) | Builder (Product + ConcreteBuilder), Prototype (Concrete Prototype) |
| prototype | Prototype, AccountTemplateRegistry | Prototype (interface + registry) |
| repository | AccountRepository | Singleton - stores accounts in table `students` |
| audit | AuditLogger | Singleton - stores entries in table `audit_log` |
| db | Database | JDBC connection to SQLite + CREATE TABLE |
| (root) | Main, ShowAccounts | demo / read-only display |

## Database (SQLite, file uniaccounts.db)
    students(id TEXT PRIMARY KEY, full_name TEXT NOT NULL, email TEXT NOT NULL, phone TEXT,
             level TEXT NOT NULL, major TEXT, scholarship INTEGER NOT NULL DEFAULT 0,
             courses TEXT NOT NULL DEFAULT '')      -- courses joined with "|"
    audit_log(id INTEGER PRIMARY KEY AUTOINCREMENT, ts TEXT, action TEXT, details TEXT)

## UML (Mermaid - paste into https://mermaid.live to export an image)
```mermaid
classDiagram
  class Prototype~T~ { <<interface>> +copy() T }
  class StudentAccount {
    -id; -fullName; -email; -phone; -level; -major; -scholarship; -courses
    -StudentAccount(Builder)
    +copy() StudentAccount
    +withIdentity(id, fullName, email) StudentAccount
  }
  class Builder {
    +Builder(id, fullName, email)
    +phone() +level() +major() +scholarship() +addCourse() +courses()
    +build() StudentAccount
  }
  class AccountTemplateRegistry {
    -templates : Map
    +register(key, template)
    +createFrom(key, id, fullName, email) StudentAccount
  }
  class AccountRepository { <<Singleton>> +getInstance() +add() +findById() +findAll() +count() }
  class AuditLogger { <<Singleton>> +getInstance() +log() +print() }
  class Database { +getConnection() Connection }
  Prototype <|.. StudentAccount
  StudentAccount +-- Builder
  Builder ..> StudentAccount : creates
  AccountTemplateRegistry o-- StudentAccount
  AccountRepository ..> StudentAccount
  AccountRepository ..> AuditLogger : logs
  AccountRepository ..> Database
  AuditLogger ..> Database
```
