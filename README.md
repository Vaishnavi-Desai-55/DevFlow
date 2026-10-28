# DevFlow: Project & Issue Tracking Platform

A Jira-inspired issue tracker built with **Java Servlets, JSP, JDBC and MySQL**. Teams create projects, add members, raise issues (stories, tasks, bugs, subtasks), move them across a validated workflow on a Kanban board, discuss them in comments, and see every change in an audit history.

> Portfolio project. Built with plain Servlets/JSP/JDBC on purpose, so every layer (security, transactions, SQL) is written by hand and easy to explain.

## Features

| Area | What it does |
|---|---|
| **Authentication** | Register, login, logout, BCrypt password hashing, session timeout (30 min), session-id rotation on login |
| **Roles** | Admin, Developer, Viewer, enforced in the **service layer** (not just hidden buttons) |
| **Projects** | Create/edit projects with a unique key (e.g. `ECOM`), add/remove members, change project roles |
| **Issues** | Story / Task / Bug / Subtask, priorities, assignee, due date, parent issue, blocking links |
| **Issue keys** | `ECOM-101`, `ECOM-102`... generated with a row lock inside a transaction, so concurrent users can never get the same key |
| **Workflow engine** | `WorkflowService` is the only place that validates status changes. Transitions are stored per project in MySQL |
| **Kanban board** | Drag and drop. The browser asks the server, and the card moves only after the server approves |
| **Comments** | Add, edit (author), delete (author or project admin), chronological |
| **Audit log** | Every create, status, priority, assignee, title, description, due-date change and comment action is recorded in the same transaction as the change |
| **Smart priority** | Rule-based score (type, overdue, due soon, age, blocking) with an explanation. Never changes priority silently: you accept or ignore |
| **Dashboard + analytics** | Chart.js charts fed by live SQL, refreshed every 20 s |
| **Search** | Key/title/description search, filters (project, type, status, priority, assignee), sorting, pagination |

## Technology

Java 17, Jakarta Servlet 6 / JSP 3.1 (Tomcat 10.1), JSTL, JDBC (`PreparedStatement` everywhere), MySQL 8, Maven (WAR), jBCrypt, Gson, Chart.js, plain CSS and JavaScript, JUnit 5.

## Architecture

```mermaid
flowchart LR
    B["Browser"] --> F["Filters<br/>AuthFilter, CsrfFilter"]
    F --> C["Servlets<br/>(controllers)"]
    C --> S["Services<br/>rules, permissions, transactions"]
    S --> D["DAOs<br/>PreparedStatement SQL"]
    D --> DB[("MySQL 8")]
    C --> V["JSP views<br/>WEB-INF/views, c:out escaping"]
    V --> B
```

* **controller/** reads the request, calls a service, picks a view. No SQL, no rules.
* **service/** holds business rules and permission checks. `Tx.run(...)` wraps work in a transaction.
* **dao/** is the only place with SQL. All queries use `PreparedStatement`.
* **filter/** blocks anonymous users and checks the CSRF token on every POST.
* **util/** `DBConnection`, `PasswordUtil` (BCrypt), `ValidationUtil`, `CsrfUtil`, `Tx`.

Why it is built this way, in one line each:
* *Transactions:* creating an issue (number + insert + counter + audit row) and changing status (check + update + audit row) each commit or roll back as one unit.
* *Authorization:* a user who guesses `/issues/view?id=7` for a project they are not in gets a 404, the same as a missing issue.
* *Workflow:* the board JavaScript is only a convenience. Calling the endpoint directly cannot bypass the rules.

### Connection pooling
`DBConnection.getConnection()` opens a new connection per call (simple and correct for v1). To use a pool such as HikariCP later, change only that method to `return dataSource.getConnection();`. Nothing else changes.

## Database

`database/schema.sql` creates `devflow_db` (InnoDB, keys, unique constraints, indexes):

```mermaid
erDiagram
    USERS ||--o{ PROJECT_MEMBERS : "joins"
    PROJECTS ||--o{ PROJECT_MEMBERS : "has"
    USERS ||--o{ PROJECTS : "creates"
    PROJECTS ||--o{ ISSUES : "contains"
    USERS ||--o{ ISSUES : "reports"
    USERS ||--o{ ISSUES : "assigned to"
    ISSUES ||--o{ ISSUES : "parent of (subtasks)"
    ISSUES ||--o{ ISSUE_LINKS : "blocker"
    ISSUES ||--o{ ISSUE_LINKS : "blocked"
    ISSUES ||--o{ COMMENTS : "has"
    USERS ||--o{ COMMENTS : "writes"
    ISSUES ||--o{ ISSUE_HISTORY : "audited by"
    USERS ||--o{ ISSUE_HISTORY : "performs"
    PROJECTS ||--o{ WORKFLOWS : "defines"
    WORKFLOWS ||--o{ WORKFLOW_TRANSITIONS : "allows"

    USERS {
        int id PK
        string full_name
        string email UK
        string password_hash
        string role
        timestamp created_at
    }
    PROJECTS {
        int id PK
        string name
        string project_key UK
        string status
        int next_issue_number
        int created_by FK
    }
    PROJECT_MEMBERS {
        int id PK
        int project_id FK
        int user_id FK
        string project_role
    }
    ISSUES {
        int id PK
        int project_id FK
        int issue_number
        string issue_key UK
        string title
        string issue_type
        string priority
        string status
        int reporter_id FK
        int assignee_id FK
        int parent_issue_id FK
        date due_date
    }
    ISSUE_LINKS {
        int id PK
        int blocker_id FK
        int blocked_id FK
    }
    COMMENTS {
        int id PK
        int issue_id FK
        int user_id FK
        text comment_text
    }
    ISSUE_HISTORY {
        int id PK
        int issue_id FK
        int user_id FK
        string action_type
        string field_name
        text old_value
        text new_value
    }
    WORKFLOWS {
        int id PK
        int project_id FK
        string workflow_name
    }
    WORKFLOW_TRANSITIONS {
        int id PK
        int workflow_id FK
        string from_status
        string to_status
    }
```

Two small additions to the brief: `projects.next_issue_number` (the key counter) and `issue_links` (for the "blocks another issue" rule).

### Workflow (status transitions)

Only these moves are allowed. Anything else is rejected by `WorkflowService` with a message such as "Cannot move issue from OPEN directly to DONE."

```mermaid
stateDiagram-v2
    [*] --> OPEN
    OPEN --> IN_PROGRESS
    IN_PROGRESS --> IN_REVIEW
    IN_REVIEW --> IN_PROGRESS
    IN_REVIEW --> TESTING
    TESTING --> IN_PROGRESS
    TESTING --> DONE
    DONE --> OPEN : reopen
```

### What happens when you drag a card on the Kanban board

```mermaid
sequenceDiagram
    actor U as Developer
    participant J as board.js
    participant W as WorkflowServlet
    participant S as WorkflowService
    participant DB as MySQL
    U->>J: Drag card to another column
    J->>W: POST /workflow/transition with CSRF token
    W->>S: transition(user, issueId, toStatus)
    S->>DB: BEGIN and lock the issue row
    S->>S: Check role and allowed transition
    alt Move allowed
        S->>DB: UPDATE status, INSERT audit row, COMMIT
        W-->>J: 200 OK
        J-->>U: Card moves and a success toast appears
    else Move not allowed
        S->>DB: ROLLBACK
        W-->>J: 409 with the reason
        J-->>U: Card stays and an error toast appears
    end
```

## Getting started

### 1. Prerequisites
JDK 17+, Maven 3.9+, MySQL 8, and **Apache Tomcat 10.1** (not 9: Tomcat 9 uses `javax.*`, this project uses `jakarta.*`).

### 2. Create the database
PowerShell does not support the `<` redirect, so use `Get-Content` (or open the files in MySQL Workbench and run them):
```powershell
Get-Content database\schema.sql | mysql -u root -p
Get-Content database\sample_data.sql | mysql -u root -p     # optional demo data (run on an empty database)
```

### 3. Configure
Copy `src/main/resources/db.properties.example` to `src/main/resources/db.properties` and set your MySQL password. This file is git-ignored.

### 4. Build and deploy
Quick way: `.\deploy.ps1` (edit the `$Tomcat` path inside first). Or step by step:
```powershell
mvn clean package
C:\tomcat10\bin\shutdown.bat
Remove-Item C:\tomcat10\webapps\devflow.war -ErrorAction SilentlyContinue
Remove-Item C:\tomcat10\webapps\devflow -Recurse -ErrorAction SilentlyContinue
Copy-Item target\devflow.war C:\tomcat10\webapps\
C:\tomcat10\bin\startup.bat
```
Open **http://localhost:8080/devflow/** (health check: `/devflow/health`).

### Demo accounts (after `sample_data.sql`)
All use the password `Password@123`.

| Email | Role |
|---|---|
| admin@devflow.dev | Admin (member of both demo projects) |
| rohan@devflow.dev | Developer |
| priya@devflow.dev | Developer (ECOM only) |
| viewer@devflow.dev | Viewer (read-only) |

Without sample data: the **first account you register becomes the Admin**. Everyone else registers as Developer or Viewer, and an admin adds them to projects.

### Run the tests
```powershell
mvn test
```
See `TESTING.md` for the manual checklist.

## Screenshots

### Dashboard
![DevFlow Dashboard](docs/screenshots/dashboard.png)

### All Issues
![DevFlow All Issues](docs/screenshots/allissues.png)

### Projects
![DevFlow Projects](docs/screenshots/projects.png)

### My Issues
![DevFlow My Issues](docs/screenshots/Myissues.png)


## Project structure
```
src/main/java/com/devflow/{controller,service,dao,model,filter,util}
src/main/webapp/WEB-INF/views/*.jsp     (pages, hidden from direct URL access)
src/main/webapp/assets/{css,js}
src/test/java/com/devflow/...            (JUnit 5)
database/{schema.sql,sample_data.sql}
```

## Future enhancements
Pagination for the board, email notifications, file attachments, per-project custom workflows editor, HikariCP pooling, login rate limiting, REST API with token auth, Docker compose, migration to Spring Boot.