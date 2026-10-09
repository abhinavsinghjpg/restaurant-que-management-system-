RESTAURANT QUEUE MANAGEMENT SYSTEM
PRODUCT REQUIREMENTS DOCUMENT (PRD)

Version: 1.0
Project Type: Java Web Application
Database: PostgreSQL
Architecture: Modular Monolith
Primary Backend: Java + Spring Boot
Frontend: HTML, CSS, JavaScript, Thymeleaf, Bootstrap

============================================================
1. PROJECT OVERVIEW
============================================================

The Restaurant Queue Management System is a web-based application
designed to digitally manage customers waiting for tables in a
restaurant.

The system replaces the traditional physical waiting line with a
digital queue. Customers can join the queue, receive a token, and
track their queue position and estimated waiting time. Restaurant
staff can log in to an admin/staff dashboard and manage customers,
queue status, and restaurant tables.

The application will be developed as a modular monolith. Each major
feature will have its own frontend and backend components so that
future changes and debugging can be performed at the module level.

============================================================
2. PROJECT OBJECTIVES
============================================================

Primary objectives:

1. Allow customers to digitally join a restaurant waiting queue.
2. Generate a unique queue token for every active queue entry.
3. Allow customers to view their queue position and estimated wait.
4. Allow customers to cancel their queue entry.
5. Provide staff/admin with a queue management dashboard.
6. Allow staff to call, seat, skip, and manage customers.
7. Provide basic restaurant table management.
8. Provide basic daily queue statistics.
9. Maintain queue activity/history in PostgreSQL.
10. Keep the codebase modular and easy to maintain.

============================================================
3. TARGET USERS
============================================================

3.1 CUSTOMER

Customers do not need to create an account.

Customer capabilities:

- Enter name.
- Enter mobile number.
- Enter number of people.
- Join the restaurant queue.
- Receive a queue token.
- View queue status.
- View people ahead.
- View estimated waiting time.
- Cancel their queue entry.

3.2 STAFF / ADMIN

Staff members use authenticated access.

Staff capabilities:

- Login.
- View the current queue.
- View current serving token.
- Call the next customer.
- Mark customer as seated.
- Skip a customer.
- View customer details.
- Manage restaurant tables.
- Assign/release tables.
- View basic daily statistics.

============================================================
4. PROJECT SCOPE
============================================================

IN SCOPE:

- Customer queue registration.
- Token generation.
- Queue position calculation.
- Estimated waiting time.
- Queue status management.
- Customer queue cancellation.
- Staff/admin authentication.
- Staff queue dashboard.
- Call Next functionality.
- Seat customer functionality.
- Skip customer functionality.
- Restaurant table status.
- Table assignment/release.
- Queue history.
- Basic dashboard statistics.
- PostgreSQL persistence.
- Modular frontend/backend structure.
- Error handling and validation.

OUT OF SCOPE FOR VERSION 1:

- Online food ordering.
- Online payment.
- Restaurant billing/POS.
- Delivery management.
- Customer accounts.
- SMS gateway.
- WhatsApp notifications.
- Email notifications.
- Multi-restaurant support.
- Advanced analytics.
- Microservices architecture.
- Redis/WebSocket infrastructure.
- AI-based waiting-time prediction.

These features can be considered future enhancements.

============================================================
5. CORE FEATURES
============================================================

5.1 CUSTOMER QUEUE

The customer opens the restaurant queue page and provides:

- Customer name
- Mobile number
- Party size / number of people

After submitting the form, the system:

1. Validates the information.
2. Creates a queue entry.
3. Generates a queue token.
4. Stores the entry in PostgreSQL.
5. Displays the customer's token.
6. Displays current queue information.

Example:

Token: A-24
People Ahead: 5
Estimated Wait: 25 minutes
Status: WAITING

------------------------------------------------------------
5.2 TOKEN GENERATION
------------------------------------------------------------

Each queue entry receives a token.

Example:

A-01
A-02
A-03
A-04

The token must be unique for the relevant queue/session/day.

The token generation logic will be handled by the Customer Queue
backend service and not directly inside the HTML/JavaScript.

------------------------------------------------------------
5.3 QUEUE POSITION
------------------------------------------------------------

The system calculates the number of active customers ahead of a
customer.

Only appropriate active statuses should be considered when
calculating position.

Primary active waiting status:

WAITING

Customers with the following statuses should not remain in the
waiting position:

SEATED
SKIPPED
CANCELLED

------------------------------------------------------------
5.4 ESTIMATED WAITING TIME
------------------------------------------------------------

Version 1 will use a simple configurable average service time.

Example:

Average service time = 5 minutes

Customers ahead = 4

Estimated waiting time = 4 x 5 = 20 minutes

The average service time should be stored in configuration rather
than hard-coded throughout the application.

Future versions may calculate the estimate dynamically using actual
historical service times.

------------------------------------------------------------
5.5 CUSTOMER QUEUE STATUS
------------------------------------------------------------

A queue entry can have the following states:

WAITING
CALLED
SEATED
SKIPPED
CANCELLED

Normal flow:

WAITING -> CALLED -> SEATED

Alternative flows:

WAITING -> SKIPPED
WAITING -> CANCELLED
CALLED -> SKIPPED
CALLED -> SEATED

Invalid status transitions should be rejected by the backend.

------------------------------------------------------------
5.6 CUSTOMER CANCELLATION
------------------------------------------------------------

A customer can cancel their active queue entry.

The system should:

1. Verify the queue entry exists.
2. Verify it is eligible for cancellation.
3. Change its status to CANCELLED.
4. Record the action in queue history.
5. Remove the customer from active queue calculations.

------------------------------------------------------------
6. ADMIN / STAFF FEATURES
============================================================

6.1 LOGIN

Staff/admin users can log into the management system.

Required fields:

- Username
- Password

Authentication will be handled by Spring Security.

Customers do not need to log in.

------------------------------------------------------------
6.2 QUEUE DASHBOARD
------------------------------------------------------------

The staff dashboard should display:

- Currently serving token.
- Number of waiting customers.
- Queue list.
- Customer name.
- Party size.
- Token.
- Queue status.
- Join time.
- Available actions.

Example:

TOKEN   NAME       PEOPLE   STATUS
A-21    Rahul      2        WAITING
A-22    Aman       4        WAITING
A-23    Priya      3        WAITING

Available actions:

CALL NEXT
SEAT
SKIP

------------------------------------------------------------
6.3 CALL NEXT
------------------------------------------------------------

The system selects the next eligible WAITING customer according to
queue order.

The customer's status changes:

WAITING -> CALLED

The called time is recorded.

A queue history entry is created.

------------------------------------------------------------
6.4 SEAT CUSTOMER
------------------------------------------------------------

When a customer is seated:

CALLED -> SEATED

The seated time is recorded.

If a table is assigned, the table status changes to OCCUPIED.

------------------------------------------------------------
6.5 SKIP CUSTOMER
------------------------------------------------------------

If the customer does not respond:

WAITING/CALLED -> SKIPPED

The action is recorded in queue history.

------------------------------------------------------------
7. TABLE MANAGEMENT
============================================================

Restaurant tables will be maintained in the table management module.

Each table contains:

- Table ID
- Table number
- Capacity
- Status

Table statuses:

AVAILABLE
OCCUPIED

Example:

T1 - Capacity 2 - AVAILABLE
T2 - Capacity 4 - OCCUPIED
T3 - Capacity 4 - AVAILABLE
T4 - Capacity 6 - AVAILABLE

------------------------------------------------------------
7.1 TABLE ASSIGNMENT
------------------------------------------------------------

When seating a customer, the staff can select a suitable available
table.

The system should prefer tables whose capacity is sufficient for the
party size.

Example:

Party size: 4

Suitable tables:

T3 - capacity 4 - AVAILABLE
T4 - capacity 6 - AVAILABLE

The staff can assign one of these tables.

------------------------------------------------------------
7.2 TABLE RELEASE
------------------------------------------------------------

When a table becomes free:

OCCUPIED -> AVAILABLE

The table becomes available for another customer.

============================================================
8. DASHBOARD / STATISTICS
============================================================

The dashboard provides basic daily statistics.

Required statistics:

- Customers served today.
- Currently waiting customers.
- Currently called customers.
- Skipped customers.
- Cancelled customers.
- Average waiting time.
- Available tables.
- Occupied tables.

Version 1 does not require advanced charts.

============================================================
9. DATABASE REQUIREMENTS
============================================================

Database:

PostgreSQL

Database name:

restaurant_queue

Main tables:

1. users
2. queue_entries
3. restaurant_tables
4. queue_history

------------------------------------------------------------
9.1 USERS
------------------------------------------------------------

Purpose:
Stores staff/admin authentication information.

Fields:

id
username
password
role
created_at

Role examples:

ADMIN
STAFF

------------------------------------------------------------
9.2 QUEUE_ENTRIES
------------------------------------------------------------

Purpose:
Stores customer queue information.

Fields:

id
token_number
customer_name
mobile
party_size
status
join_time
called_time
seated_time

Possible status values:

WAITING
CALLED
SEATED
SKIPPED
CANCELLED

------------------------------------------------------------
9.3 RESTAURANT_TABLES
------------------------------------------------------------

Purpose:
Stores restaurant table information.

Fields:

id
table_number
capacity
status

Possible status values:

AVAILABLE
OCCUPIED

------------------------------------------------------------
9.4 QUEUE_HISTORY
------------------------------------------------------------

Purpose:
Stores important queue actions for traceability and statistics.

Fields:

id
queue_entry_id
action
timestamp

Example actions:

JOINED
CALLED
SEATED
SKIPPED
CANCELLED

============================================================
10. SOFTWARE ARCHITECTURE
============================================================

Architecture style:

MODULAR MONOLITH

The application will be one deployable Spring Boot application,
but functionality will be separated into independent feature
modules.

High-level architecture:

Frontend
    |
    v
Controller
    |
    v
Service
    |
    v
Repository
    |
    v
PostgreSQL

Each feature/module owns its relevant controller, service,
repository, model, DTO, frontend pages, JavaScript and CSS.

============================================================
11. MODULE STRUCTURE
============================================================

The main modules are:

1. Authentication
2. Customer Queue
3. Queue Management
4. Table Management
5. Dashboard
6. Common / Shared

------------------------------------------------------------
11.1 AUTHENTICATION MODULE
------------------------------------------------------------

Responsibilities:

- Staff login.
- Authentication.
- Authorization.
- User information.

Suggested structure:

authentication/
    backend/
        controller/
        service/
        repository/
        model/
        dto/
    frontend/
        pages/
        css/
        js/

------------------------------------------------------------
11.2 CUSTOMER QUEUE MODULE
------------------------------------------------------------

Responsibilities:

- Join queue.
- Generate token.
- Queue status.
- Position calculation.
- Estimated waiting time.
- Customer cancellation.

Suggested structure:

customer_queue/
    backend/
        controller/
        service/
        repository/
        model/
        dto/
    frontend/
        pages/
        css/
        js/

------------------------------------------------------------
11.3 QUEUE MANAGEMENT MODULE
------------------------------------------------------------

Responsibilities:

- Staff queue view.
- Call next.
- Seat customer.
- Skip customer.
- Queue history.

Suggested structure:

queue_management/
    backend/
        controller/
        service/
        repository/
        model/
        dto/
    frontend/
        pages/
        css/
        js/

------------------------------------------------------------
11.4 TABLE MANAGEMENT MODULE
------------------------------------------------------------

Responsibilities:

- View tables.
- Table status.
- Assign table.
- Release table.

Suggested structure:

table_management/
    backend/
        controller/
        service/
        repository/
        model/
        dto/
    frontend/
        pages/
        css/
        js/

------------------------------------------------------------
11.5 DASHBOARD MODULE
------------------------------------------------------------

Responsibilities:

- Daily statistics.
- Queue summary.
- Table summary.

Suggested structure:

dashboard/
    backend/
        controller/
        service/
        dto/
    frontend/
        pages/
        css/
        js/

------------------------------------------------------------
11.6 COMMON / SHARED
------------------------------------------------------------

Contains functionality shared by multiple modules.

Examples:

common/
    exception/
    response/
    constants/
    util/

Examples of shared classes:

GlobalExceptionHandler
ApiResponse
QueueStatus
TableStatus
Validation utilities

============================================================
12. RECOMMENDED PROJECT STRUCTURE
============================================================

restaurant-queue-system/
|
|-- src/
|   |
|   |-- main/
|       |
|       |-- java/
|       |   |
|       |   |-- com.restaurant.queue/
|       |       |
|       |       |-- RestaurantQueueApplication.java
|       |       |
|       |       |-- config/
|       |       |   |-- SecurityConfig.java
|       |       |   |-- DatabaseConfig.java
|       |       |
|       |       |-- common/
|       |       |   |-- exception/
|       |       |   |-- response/
|       |       |   |-- constants/
|       |       |   |-- util/
|       |       |
|       |       |-- modules/
|       |           |
|       |           |-- authentication/
|       |           |   |-- backend/
|       |           |   |-- frontend/
|       |           |
|       |           |-- customer_queue/
|       |           |   |-- backend/
|       |           |   |-- frontend/
|       |           |
|       |           |-- queue_management/
|       |           |   |-- backend/
|       |           |   |-- frontend/
|       |           |
|       |           |-- table_management/
|       |           |   |-- backend/
|       |           |   |-- frontend/
|       |           |
|       |           |-- dashboard/
|       |               |-- backend/
|       |               |-- frontend/
|       |
|       |-- resources/
|           |-- templates/
|           |-- static/
|           |-- application.properties
|
|-- src/test/
|
|-- pom.xml
|-- README.md
|-- .gitignore

NOTE:
The exact Thymeleaf/static-resource arrangement can be adjusted during
implementation. The architectural rule is that feature ownership
must remain modular.

============================================================
13. API / ROUTE REQUIREMENTS
============================================================

Customer Queue:

POST   /queue/join
GET    /queue/{token}
DELETE /queue/{token}

Authentication:

GET    /login
POST   /login
POST   /logout

Queue Management:

GET    /admin/queue
POST   /admin/queue/next
POST   /admin/queue/{id}/seat
POST   /admin/queue/{id}/skip

Table Management:

GET    /admin/tables
POST   /admin/tables/{id}/assign
POST   /admin/tables/{id}/release

Dashboard:

GET    /admin/dashboard

The final URL structure may be adjusted during implementation while
keeping module boundaries intact.

============================================================
14. VALIDATION REQUIREMENTS
============================================================

Customer name:

- Required.
- Must contain a valid reasonable length.

Mobile:

- Required.
- Must follow the selected phone-number validation rules.

Party size:

- Required.
- Must be a positive integer.
- Must be within the restaurant's configured maximum.

Username:

- Required.

Password:

- Required.

Table capacity:

- Must be a positive integer.

Invalid input must produce a clear user-facing error and must not
create invalid database records.

============================================================
15. BUSINESS RULES
============================================================

1. A queue token must be unique within the applicable queue period.
2. Only WAITING customers are eligible for normal queue position.
3. CALL NEXT selects the next eligible customer according to queue
   order.
4. A SEATED customer is removed from the active waiting queue.
5. A SKIPPED customer is removed from the active waiting queue.
6. A CANCELLED customer is removed from the active waiting queue.
7. An OCCUPIED table cannot be assigned to another customer.
8. A table's capacity should be sufficient for the party size.
9. Invalid queue status transitions must be rejected.
10. Important queue actions should be recorded in queue_history.
11. Customer-facing functionality should not expose staff/admin
    controls.
12. Staff/admin routes must require authentication.

============================================================
16. ERROR HANDLING
============================================================

The system should use centralized exception handling.

Common errors:

- Queue entry not found.
- Invalid queue status.
- Customer cannot cancel queue.
- Table not found.
- Table already occupied.
- Table capacity insufficient.
- Authentication failure.
- Invalid request data.
- Database failure.

The application should return understandable messages rather than
raw Java stack traces to users.

Development logs may contain technical details.

============================================================
17. FRONTEND REQUIREMENTS
============================================================

Customer pages:

1. Home / Queue landing page.
2. Join Queue page.
3. Queue Token / Status page.

Admin pages:

1. Login page.
2. Admin Dashboard.
3. Queue Management section.
4. Table Management section.

UI requirements:

- Responsive layout.
- Clean restaurant-oriented design.
- Clear queue status.
- Clear action buttons.
- Mobile-friendly customer queue page.
- Appropriate success/error messages.
- Consistent navigation for admin pages.

Bootstrap may be used for basic responsive UI components.

============================================================
18. SECURITY REQUIREMENTS
============================================================

1. Staff/admin pages must require authentication.
2. Passwords must not be stored as plain text in the final system.
3. Use Spring Security for authentication/authorization.
4. Customer endpoints must validate input.
5. Admin-only actions must not be accessible to unauthenticated
   users.
6. Database credentials must not be committed to Git.
7. Sensitive configuration should use environment variables in
   deployment.

============================================================
19. DEVELOPMENT STRATEGY
============================================================

Development will follow a vertical-slice approach.

Do NOT build the entire backend first and the frontend afterward.

Each module should be developed from database/model through UI.

Recommended order:

PHASE 1:
Project setup + PostgreSQL connection.

PHASE 2:
Customer Queue module.

Flow:

Database
    ->
Entity
    ->
Repository
    ->
Service
    ->
Controller
    ->
Frontend
    ->
Testing

PHASE 3:
Authentication module.

PHASE 4:
Queue Management module.

PHASE 5:
Table Management module.

PHASE 6:
Dashboard module.

PHASE 7:
Integration testing.

PHASE 8:
UI polish.

PHASE 9:
Deployment.

============================================================
20. MODULE DEVELOPMENT RULE
============================================================

Each module must contain or clearly own:

- Frontend pages.
- Frontend CSS.
- Frontend JavaScript.
- Controller.
- Service.
- Repository.
- Model/entity.
- DTOs where required.
- Module-specific tests.
- Module documentation/README where useful.

A change to one module should require minimum changes to unrelated
modules.

Example:

If token generation changes:
    customer_queue/backend/service/

If customer queue UI changes:
    customer_queue/frontend/

If table assignment breaks:
    table_management/backend/

If dashboard statistics are incorrect:
    dashboard/backend/

============================================================
21. TESTING REQUIREMENTS
============================================================

Unit testing:

- Token generation.
- Queue position calculation.
- Waiting-time calculation.
- Queue state transitions.
- Table assignment rules.

Integration testing:

- Spring Boot + PostgreSQL.
- Repository operations.
- Queue workflow.
- Authentication.

Manual testing:

Customer:

- Join queue.
- Receive token.
- Check status.
- Cancel queue.

Staff:

- Login.
- View queue.
- Call next.
- Seat.
- Skip.
- Assign table.
- Release table.

============================================================
22. DEFINITION OF DONE
============================================================

The project is considered functionally complete when:

1. PostgreSQL database is connected successfully.
2. Customer can join the queue.
3. A token is generated and stored.
4. Customer can view queue status.
5. Queue position is calculated correctly.
6. Estimated waiting time is displayed.
7. Customer can cancel an eligible queue entry.
8. Staff can log in.
9. Staff can see the active queue.
10. Staff can call the next customer.
11. Staff can seat a customer.
12. Staff can skip a customer.
13. Staff can view/manage restaurant tables.
14. Table assignment works.
15. Basic dashboard statistics work.
16. Important actions are recorded in queue history.
17. Invalid operations return appropriate errors.
18. The application can be run from a clean environment using the
    documented setup instructions.

============================================================
23. FUTURE ENHANCEMENTS
============================================================

Possible future versions:

- SMS notification.
- WhatsApp notification.
- Email notification.
- QR-code based queue joining.
- Customer estimated wait notifications.
- Real-time updates using WebSocket.
- Dynamic waiting-time prediction.
- Multiple restaurants/branches.
- Customer accounts.
- Reservation system.
- Advanced analytics.
- Restaurant-specific configuration.
- Digital display screen for the waiting area.

These are NOT required for Version 1.

============================================================
24. SUCCESS CRITERIA
============================================================

The project succeeds if a real user can complete the following
workflow without manual database intervention:

CUSTOMER:

Open website
    ->
Enter name, mobile, party size
    ->
Join queue
    ->
Receive token
    ->
View position
    ->
View estimated waiting time
    ->
Cancel if required

STAFF:

Login
    ->
Open dashboard
    ->
See customer queue
    ->
Call next
    ->
Assign suitable table
    ->
Seat customer
    ->
Table becomes occupied
    ->
Release table later

All important data must persist correctly in PostgreSQL.

============================================================
25. FINAL ARCHITECTURAL PRINCIPLE
============================================================

The project should remain a MODULAR MONOLITH.

It is one Spring Boot application and one PostgreSQL database, but
features are separated by business responsibility.

Core principle:

MODULE
=
FRONTEND
+
BACKEND
+
DATA ACCESS
+
BUSINESS LOGIC
+
TESTS

The purpose of this architecture is maintainability.

Developers should be able to identify the responsible module quickly
when adding a feature, changing behavior, or debugging an error.

END OF PRD
