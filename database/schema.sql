-- DevFlow database schema (MySQL 8, InnoDB)
CREATE DATABASE IF NOT EXISTS devflow_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE devflow_db;

CREATE TABLE users (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL,
    password_hash VARCHAR(60)  NOT NULL,            -- BCrypt hashes are 60 chars
    role          ENUM('ADMIN','DEVELOPER','VIEWER') NOT NULL DEFAULT 'DEVELOPER',
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_users_email UNIQUE (email)
) ENGINE=InnoDB;

CREATE TABLE projects (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    name              VARCHAR(120) NOT NULL,
    project_key       VARCHAR(10)  NOT NULL,
    description       TEXT,
    status            ENUM('ACTIVE','ARCHIVED') NOT NULL DEFAULT 'ACTIVE',
    next_issue_number INT NOT NULL DEFAULT 101,     -- counter used to build ECOM-101, ECOM-102 ...
    created_by        INT NOT NULL,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_projects_key UNIQUE (project_key),
    CONSTRAINT fk_projects_creator FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE TABLE project_members (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    project_id   INT NOT NULL,
    user_id      INT NOT NULL,
    project_role ENUM('ADMIN','DEVELOPER','VIEWER') NOT NULL DEFAULT 'DEVELOPER',
    joined_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_member UNIQUE (project_id, user_id),
    CONSTRAINT fk_pm_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_pm_user    FOREIGN KEY (user_id)    REFERENCES users(id)    ON DELETE CASCADE,
    INDEX idx_pm_user (user_id)
) ENGINE=InnoDB;

CREATE TABLE issues (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    project_id      INT NOT NULL,
    issue_number    INT NOT NULL,
    issue_key       VARCHAR(20) NOT NULL,           -- e.g. ECOM-101
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    issue_type      ENUM('STORY','TASK','BUG','SUBTASK') NOT NULL DEFAULT 'TASK',
    priority        ENUM('LOWEST','LOW','MEDIUM','HIGH','CRITICAL') NOT NULL DEFAULT 'MEDIUM',
    status          ENUM('OPEN','IN_PROGRESS','IN_REVIEW','TESTING','DONE') NOT NULL DEFAULT 'OPEN',
    reporter_id     INT NOT NULL,
    assignee_id     INT NULL,
    parent_issue_id INT NULL,
    due_date        DATE NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_issue_key     UNIQUE (issue_key),
    CONSTRAINT uq_issue_number  UNIQUE (project_id, issue_number),
    CONSTRAINT fk_issue_project  FOREIGN KEY (project_id)      REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_issue_reporter FOREIGN KEY (reporter_id)     REFERENCES users(id),
    CONSTRAINT fk_issue_assignee FOREIGN KEY (assignee_id)     REFERENCES users(id)  ON DELETE SET NULL,
    CONSTRAINT fk_issue_parent   FOREIGN KEY (parent_issue_id) REFERENCES issues(id) ON DELETE CASCADE,
    INDEX idx_issue_project_status (project_id, status),
    INDEX idx_issue_assignee (assignee_id),
    INDEX idx_issue_due (due_date),
    INDEX idx_issue_priority (priority)
) ENGINE=InnoDB;

CREATE TABLE issue_links (                           -- "blocker blocks blocked"
    id         INT AUTO_INCREMENT PRIMARY KEY,
    blocker_id INT NOT NULL,
    blocked_id INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_link UNIQUE (blocker_id, blocked_id),
    CONSTRAINT fk_link_blocker FOREIGN KEY (blocker_id) REFERENCES issues(id) ON DELETE CASCADE,
    CONSTRAINT fk_link_blocked FOREIGN KEY (blocked_id) REFERENCES issues(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE comments (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    issue_id     INT NOT NULL,
    user_id      INT NOT NULL,
    comment_text TEXT NOT NULL,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_comment_issue FOREIGN KEY (issue_id) REFERENCES issues(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_user  FOREIGN KEY (user_id)  REFERENCES users(id),
    INDEX idx_comment_issue (issue_id, created_at)
) ENGINE=InnoDB;

CREATE TABLE issue_history (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    issue_id    INT NOT NULL,
    user_id     INT NOT NULL,
    action_type VARCHAR(30) NOT NULL,               -- CREATED, STATUS_CHANGED, ...
    field_name  VARCHAR(50) NULL,
    old_value   TEXT NULL,
    new_value   TEXT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_hist_issue FOREIGN KEY (issue_id) REFERENCES issues(id) ON DELETE CASCADE,
    CONSTRAINT fk_hist_user  FOREIGN KEY (user_id)  REFERENCES users(id),
    INDEX idx_hist_issue (issue_id, created_at)
) ENGINE=InnoDB;

CREATE TABLE workflows (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    project_id    INT NOT NULL,
    workflow_name VARCHAR(100) NOT NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_wf_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE workflow_transitions (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    workflow_id INT NOT NULL,
    from_status VARCHAR(20) NOT NULL,
    to_status   VARCHAR(20) NOT NULL,
    CONSTRAINT uq_transition UNIQUE (workflow_id, from_status, to_status),
    CONSTRAINT fk_wt_workflow FOREIGN KEY (workflow_id) REFERENCES workflows(id) ON DELETE CASCADE
) ENGINE=InnoDB;
