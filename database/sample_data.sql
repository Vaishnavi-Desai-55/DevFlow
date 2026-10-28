-- DevFlow demo data. Run AFTER schema.sql on an EMPTY database:  mysql -u root -p < database/sample_data.sql
-- Every demo account uses the password:  Password@123   (stored as a BCrypt hash, never as plain text)
USE devflow_db;

INSERT INTO users (id, full_name, email, password_hash, role) VALUES
 (1, 'Aisha Rahman',  'admin@devflow.dev',  '$2a$10$Ntzu0Mq9B16kdIGuH5jbYOEigxWS3NRAUwYFzhUt.q8B9A55FZ0wy', 'ADMIN'),
 (2, 'Rohan Mehta',   'rohan@devflow.dev',  '$2a$10$Ntzu0Mq9B16kdIGuH5jbYOEigxWS3NRAUwYFzhUt.q8B9A55FZ0wy', 'DEVELOPER'),
 (3, 'Priya Nair',    'priya@devflow.dev',  '$2a$10$Ntzu0Mq9B16kdIGuH5jbYOEigxWS3NRAUwYFzhUt.q8B9A55FZ0wy', 'DEVELOPER'),
 (4, 'Sam Viewer',    'viewer@devflow.dev', '$2a$10$Ntzu0Mq9B16kdIGuH5jbYOEigxWS3NRAUwYFzhUt.q8B9A55FZ0wy', 'VIEWER');

INSERT INTO projects (id, name, project_key, description, created_by, next_issue_number) VALUES
 (1, 'E-Commerce Platform', 'ECOM', 'Online store: catalogue, cart, checkout and order tracking.', 1, 111),
 (2, 'Mobile App',          'MOBL', 'Customer mobile app for iOS and Android.', 1, 106);

INSERT INTO project_members (project_id, user_id, project_role) VALUES
 (1, 1, 'ADMIN'), (1, 2, 'DEVELOPER'), (1, 3, 'DEVELOPER'), (1, 4, 'VIEWER'),
 (2, 1, 'ADMIN'), (2, 2, 'DEVELOPER'), (2, 4, 'VIEWER');

-- Default workflow for both projects: OPEN > IN_PROGRESS > IN_REVIEW > TESTING > DONE
INSERT INTO workflows (id, project_id, workflow_name) VALUES (1, 1, 'Default workflow'), (2, 2, 'Default workflow');
INSERT INTO workflow_transitions (workflow_id, from_status, to_status)
SELECT w.id, t.f, t.t FROM workflows w CROSS JOIN (
    SELECT 'OPEN' f, 'IN_PROGRESS' t UNION ALL SELECT 'IN_PROGRESS', 'IN_REVIEW' UNION ALL SELECT 'IN_REVIEW', 'IN_PROGRESS'
    UNION ALL SELECT 'IN_REVIEW', 'TESTING' UNION ALL SELECT 'TESTING', 'IN_PROGRESS' UNION ALL SELECT 'TESTING', 'DONE'
    UNION ALL SELECT 'DONE', 'OPEN') t;

INSERT INTO issues (id, project_id, issue_number, issue_key, title, description, issue_type, priority, status, reporter_id, assignee_id, parent_issue_id, due_date, created_at) VALUES
 (1, 1, 101, 'ECOM-101', 'Shopping cart page', 'As a shopper I can add, remove and update item quantities in my cart.', 'STORY', 'HIGH', 'IN_PROGRESS', 1, 2, NULL, CURDATE() + INTERVAL 5 DAY, NOW() - INTERVAL 12 DAY),
 (2, 1, 102, 'ECOM-102', 'Checkout with payment gateway', 'Integrate the payment provider and show order confirmation.', 'STORY', 'CRITICAL', 'IN_REVIEW', 1, 3, NULL, CURDATE() + INTERVAL 2 DAY, NOW() - INTERVAL 20 DAY),
 (3, 1, 103, 'ECOM-103', 'Payment fails for amounts over 10,000', 'Gateway returns a validation error when the order total is above 10,000.', 'BUG', 'CRITICAL', 'OPEN', 2, 2, NULL, CURDATE() - INTERVAL 3 DAY, NOW() - INTERVAL 10 DAY),
 (4, 1, 104, 'ECOM-104', 'Product search is slow', 'Search takes over 4 seconds on the category page.', 'BUG', 'HIGH', 'TESTING', 2, 3, NULL, CURDATE() + INTERVAL 1 DAY, NOW() - INTERVAL 8 DAY),
 (5, 1, 105, 'ECOM-105', 'Send order confirmation email', 'Email the customer after a successful payment.', 'TASK', 'MEDIUM', 'OPEN', 1, 3, NULL, CURDATE() + INTERVAL 9 DAY, NOW() - INTERVAL 6 DAY),
 (6, 1, 106, 'ECOM-106', 'Update product images to WebP', 'Convert catalogue images to reduce page weight.', 'TASK', 'LOW', 'DONE', 2, 2, NULL, CURDATE() - INTERVAL 4 DAY, NOW() - INTERVAL 25 DAY),
 (7, 1, 107, 'ECOM-107', 'Wishlist feature', 'Let customers save products for later.', 'STORY', 'LOW', 'OPEN', 1, NULL, NULL, NULL, NOW() - INTERVAL 2 DAY),
 (8, 1, 108, 'ECOM-108', 'Write unit tests for the cart service', 'Cover add, remove and quantity rules.', 'TASK', 'MEDIUM', 'IN_PROGRESS', 2, 2, NULL, CURDATE() + INTERVAL 7 DAY, NOW() - INTERVAL 4 DAY),
 (9, 1, 109, 'ECOM-109', 'Design payment form', 'Card form UI with validation messages.', 'SUBTASK', 'MEDIUM', 'DONE', 1, 3, 2, CURDATE() - INTERVAL 1 DAY, NOW() - INTERVAL 15 DAY),
 (10, 1, 110, 'ECOM-110', 'Add payment webhook handler', 'Handle success and failure callbacks.', 'SUBTASK', 'HIGH', 'IN_PROGRESS', 1, 3, 2, CURDATE() + INTERVAL 3 DAY, NOW() - INTERVAL 9 DAY),
 (11, 2, 101, 'MOBL-101', 'Login screen', 'Email and password login with biometric option.', 'STORY', 'HIGH', 'DONE', 1, 2, NULL, CURDATE() - INTERVAL 10 DAY, NOW() - INTERVAL 30 DAY),
 (12, 2, 102, 'MOBL-102', 'App crashes on rotate', 'Crash on the order screen when the device is rotated.', 'BUG', 'HIGH', 'IN_REVIEW', 2, 2, NULL, CURDATE() + INTERVAL 4 DAY, NOW() - INTERVAL 5 DAY),
 (13, 2, 103, 'MOBL-103', 'Push notifications', 'Notify users about order updates.', 'STORY', 'MEDIUM', 'OPEN', 1, NULL, NULL, CURDATE() + INTERVAL 14 DAY, NOW() - INTERVAL 3 DAY),
 (14, 2, 104, 'MOBL-104', 'Dark mode', 'Support the system dark theme.', 'TASK', 'LOWEST', 'OPEN', 2, 2, NULL, NULL, NOW() - INTERVAL 1 DAY),
 (15, 2, 105, 'MOBL-105', 'Offline cart', 'Keep the cart when the network drops.', 'TASK', 'MEDIUM', 'IN_PROGRESS', 1, 2, NULL, CURDATE() - INTERVAL 2 DAY, NOW() - INTERVAL 11 DAY);

INSERT INTO issue_links (blocker_id, blocked_id) VALUES (3, 2), (3, 5);

INSERT INTO comments (issue_id, user_id, comment_text, created_at) VALUES
 (2, 1, 'Payment sandbox credentials are in the team vault.', NOW() - INTERVAL 18 DAY),
 (2, 3, 'Gateway integration is done. Ready for review.', NOW() - INTERVAL 2 DAY),
 (3, 2, 'I can reproduce this. It looks like a currency formatting problem.', NOW() - INTERVAL 7 DAY),
 (4, 3, 'Added an index on the category column. Search is now under 400 ms.', NOW() - INTERVAL 1 DAY);

-- Audit trail: every issue has a CREATED entry, plus some real status changes
INSERT INTO issue_history (issue_id, user_id, action_type, field_name, old_value, new_value, created_at)
SELECT id, reporter_id, 'CREATED', 'issue', NULL, issue_key, created_at FROM issues;
INSERT INTO issue_history (issue_id, user_id, action_type, field_name, old_value, new_value, created_at) VALUES
 (1, 2, 'STATUS_CHANGED', 'status', 'OPEN', 'IN_PROGRESS', NOW() - INTERVAL 9 DAY),
 (2, 3, 'STATUS_CHANGED', 'status', 'OPEN', 'IN_PROGRESS', NOW() - INTERVAL 15 DAY),
 (2, 3, 'STATUS_CHANGED', 'status', 'IN_PROGRESS', 'IN_REVIEW', NOW() - INTERVAL 2 DAY),
 (3, 1, 'PRIORITY_CHANGED', 'priority', 'HIGH', 'CRITICAL', NOW() - INTERVAL 6 DAY),
 (4, 3, 'STATUS_CHANGED', 'status', 'OPEN', 'IN_PROGRESS', NOW() - INTERVAL 5 DAY),
 (4, 3, 'STATUS_CHANGED', 'status', 'IN_PROGRESS', 'IN_REVIEW', NOW() - INTERVAL 3 DAY),
 (4, 2, 'STATUS_CHANGED', 'status', 'IN_REVIEW', 'TESTING', NOW() - INTERVAL 1 DAY),
 (6, 2, 'STATUS_CHANGED', 'status', 'TESTING', 'DONE', NOW() - INTERVAL 5 DAY),
 (3, 2, 'COMMENT_ADDED', 'comment', NULL, 'I can reproduce this. It looks like a currency formatting problem.', NOW() - INTERVAL 7 DAY),
 (3, 1, 'LINK_ADDED', 'blocks', NULL, 'ECOM-102', NOW() - INTERVAL 5 DAY);
