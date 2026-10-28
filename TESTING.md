# DevFlow testing

## Automated (JUnit 5): `mvn test`
Pure-logic tests that need no database: workflow transitions, priority scoring, validation, BCrypt.

## Manual checklist
Load `database/sample_data.sql` first. Password for all demo users: `Password@123`.

| # | Scenario | Steps | Expected |
|---|---|---|---|
| 1 | Registration | `/register`, fill the form | Redirect to login with a success message |
| 2 | Duplicate email | Register the same email again | "An account with this email already exists." |
| 3 | Weak password | Register with `abc` | Validation message, form keeps your input |
| 4 | Login / logout | Sign in as admin, press Sign out, press Back | Login page, no private data visible |
| 5 | Wrong password | Sign in with a bad password | "Invalid email or password." |
| 6 | Project creation | Admin: Projects, New project, key `TEST` | Project opens, creator is Admin member |
| 7 | Duplicate key | Create another project with key `TEST` | "Project key TEST is already in use." |
| 8 | Add member | Team page, add `priya@devflow.dev` | Member appears with the chosen role |
| 9 | Issue creation | New issue in `TEST` | Key `TEST-101`, then `TEST-102` for the next |
| 10 | Assignment | Edit issue, choose an assignee | Shows on the issue, board card and "My issues" |
| 11 | Valid transition | Board: drag OPEN card to In Progress | Card moves, green toast |
| 12 | Invalid transition | Board: drag an OPEN card to Done | Card snaps back, toast "Cannot move issue from OPEN directly to DONE." |
| 13 | Direct bypass | `curl -X POST .../workflow/transition` without a CSRF token | 403 |
| 14 | Comments | Add, edit, delete a comment | Listed in order, "edited" label, delete asks to confirm |
| 15 | Script in comment | Comment `<script>alert(1)</script>` | Shown as text, no popup |
| 16 | Audit history | Change title, priority, assignee, due date, status | One timeline entry per change, with old and new values |
| 17 | No-change save | Save the edit form without changes | No new history entries |
| 18 | Priority suggestion | Open `ECOM-103` (overdue bug, blocks others) | Score, suggested priority and reasons. Accept changes priority, Ignore hides it |
| 19 | Viewer role | Sign in as viewer | No New issue form (403), board cards not draggable, no edit or comment forms |
| 20 | Unauthorized access | As a new user with no projects open `/issues/view?id=1` | 404 page |
| 21 | Filtering | Issues page: project + type + status + search + sort | Correct results, pagination keeps filters |
| 22 | Dashboard | Move an issue, wait 20 s | Cards and charts update |
| 23 | Session timeout | Wait 30 min idle, click a link | Redirected to login |
| 24 | Responsive | Resize to phone width | Sidebar becomes a menu button, tables scroll sideways |
| 25 | DB down | Stop MySQL, open any page | Friendly error page, no stack trace (details in Tomcat log) |
