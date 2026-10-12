# Restaurant Queue Management System
## Weekly Project Report Submissions (PBL - 5th Semester)

**Project Title:** Restaurant Queue Management System  
**Academic Year:** 2026–27  
**Department:** Computer Science & Engineering / AI & Data Science  
**Faculty Mentor:** Er. Ram Babu Buri  
**Faculty Coordinator:** Prof. Angothu Muni  
**Live Deployed URL:** https://restaurant-que-management-system.onrender.com/  
**Tech Stack:** Java 17, Spring Boot 3.2.5, SQLite, Spring Security 6, Spring Data JPA / Hibernate, Thymeleaf, ZXing  
**Timeline:** 06-07-2026 to 12-10-2026 (15 Weeks)  

---

### Quick Form Field Mapping Reference for Portal
* **Week Dropdown:** Select `Week 1`, `Week 2`, etc.
* **GitHub / Report Link:** `https://restaurant-que-management-system.onrender.com/`
* **Work Completed This Week:** Text from below
* **Problems Faced:** Text from below
* **Plan for Next Week:** Text from below

---

### Week 1 (06-07-2026 to 10-07-2026): Orientation, Problem Identification & Project Setup
* **Week:** Week 1
* **GitHub / Report Link:** https://restaurant-que-management-system.onrender.com/
* **Work Completed This Week:**
  Formed the project team under faculty coordinator Prof. Angothu Muni and assigned engineering responsibilities across team members. Conducted domain research into physical queue friction, paper token limitations, and high customer abandonment rates in busy dining restaurants. Defined the core objective: building a digital, automated Restaurant Queue Management System. Evaluated alternative technical stacks and finalized Java 17, Spring Boot 3.2.5, SQLite persistence via Hibernate community dialect, and Thymeleaf for responsive server-side templating. Initialized the project repository on version control, established Git branching guidelines, and configured the initial Maven project directory layout.
* **Problems Faced:**
  Evaluating database tradeoffs between client-server RDBMS (PostgreSQL/MySQL) and embedded file-based storage (SQLite). Needed a lightweight, zero-configuration setup for local academic evaluation and low-overhead cloud hosting without high database provisioning costs.
* **Plan for Next Week:**
  Conduct mathematical queuing theory modeling using Kendall's notation and Little's Law, draft the Software Requirements Specification (SRS), and define functional and non-functional requirements.

---

### Week 2 (13-07-2026 to 17-07-2026): Requirements Specification & Queuing Theory Modeling
* **Week:** Week 2
* **GitHub / Report Link:** https://restaurant-que-management-system.onrender.com/
* **Work Completed This Week:**
  Drafted the comprehensive Software Requirements Specification (SRS) identifying key user personas: walk-in diners, hostesses/floor staff, and restaurant managers. Collaborated on mathematical queuing theory formulations using Kendall's M/M/c queuing model and Little's Law (L = lambda * W) to establish theoretical foundations for dynamic wait-time estimation. Documented core functional requirements (FR-1 to FR-7) covering customer queue entry, token number assignment, real-time wait estimation, table assignment, and staff management controls. Defined non-functional requirements including sub-second response times and mobile accessibility. Modeled the Finite State Machine (FSM) defining token status transitions: WAITING -> CALLED -> SEATED / SKIPPED / CANCELLED.
* **Problems Faced:**
  Formulating dynamic wait time calculations for dining parties of varying sizes when seating turnover fluctuates during unpredictable rush hours.
* **Plan for Next Week:**
  Design the high-level Modular Monolith system architecture, establish multi-tier packaging layers, and model relational database entity schemas and Data Flow Diagrams.

---

### Week 3 (20-07-2026 to 24-07-2026): System Architecture & Database Schema Design
* **Week:** Week 3
* **GitHub / Report Link:** https://restaurant-que-management-system.onrender.com/
* **Work Completed This Week:**
  Finalized the system architecture adopting a Modular Monolith architectural pattern to guarantee high cohesion and maintainability while avoiding distributed microservice complexity. Designed the multi-tier application layers comprising Controller (presentation), Service (business logic), and Repository (Spring Data JPA persistence) packages. Modeled relational entity schemas and table relationships for users, restaurant_tables, queue_entries, and queue_history. Generated Data Flow Diagrams including Level 0 context diagram and Level 1 detailed process models. Initialized the Spring Boot Maven project in Eclipse IDE, configuring dependencies in pom.xml for Web MVC, Thymeleaf, Spring Security, Data JPA, and Lombok.
* **Problems Faced:**
  Deciding on entity boundaries and ensuring that completed queue entries are separated into a dedicated history structure to prevent active queue queries from suffering performance degradation as records accumulate.
* **Plan for Next Week:**
  Configure the SQLite database connection with Hibernate dialects, build core JPA entities, and implement automated schema initialization and data seeding.

---

### Week 4 (27-07-2026 to 31-07-2026): SQLite Persistence & Base Entity Development
* **Week:** Week 4
* **GitHub / Report Link:** https://restaurant-que-management-system.onrender.com/
* **Work Completed This Week:**
  Configured SQLite JDBC driver and Hibernate Community Dialect in application.properties. Implemented JPA entity classes: User.java, RestaurantTable.java, QueueEntry.java, and QueueHistory.java with field validations, status enums, and auditing timestamps. Created DatabaseMigrationConfig.java to handle automated SQLite table creation and column migrations on application boot without needing external migration binaries. Developed DataSeeder.java to automatically populate default administrative accounts (admin / admin123, staff / staff123) and 8 default dining tables (T1 to T8) for local development and testing.
* **Problems Faced:**
  Resolving compatibility warnings between Spring Boot 3 / Hibernate 6 and SQLite's lack of native schema alter statements and sequence generators. Resolved by tuning community dialect settings and applying programmatic startup migration verification.
* **Plan for Next Week:**
  Implement Spring Security 6 framework, build database-backed user authentication with BCrypt hashing, and develop the staff login portal.

---

### Week 5 (03-08-2026 to 07-08-2026): Authentication Framework & Spring Security 6
* **Week:** Week 5
* **GitHub / Report Link:** https://restaurant-que-management-system.onrender.com/
* **Work Completed This Week:**
  Integrated Spring Security 6 framework in SecurityConfig.java using modern lambda-based filter chains. Implemented CustomUserDetailsService.java to authenticate staff and administrators using BCrypt password encryption against the SQLite database. Configured role-based authorization hierarchies (ROLE_ADMIN, ROLE_STAFF), securing /admin/** endpoints while permitting unrestricted public customer access to queue registration and status pages. Developed the Thymeleaf staff login template (login.html) with CSRF protection, session management, and contextual error alerts. Tested authentication workflows, session expiration, and protected route access control.
* **Problems Faced:**
  Adapting to Spring Security 6 breaking changes where authorizeRequests() and WebSecurityConfigurerAdapter are deprecated and removed. Refactored security configurations to use SecurityFilterChain bean declarations and modern lambda DSLs.
* **Plan for Next Week:**
  Build the dining table management module, table repository queries, and the staff table status monitoring dashboard.

---

### Week 6 (10-08-2026 to 14-08-2026): Dining Table Management Module
* **Week:** Week 6
* **GitHub / Report Link:** https://restaurant-que-management-system.onrender.com/
* **Work Completed This Week:**
  Constructed the restaurant table management subsystem comprising RestaurantTableRepository.java and TableManagementService.java. Implemented business logic for creating dining tables, updating seat capacities, and switching statuses between AVAILABLE, OCCUPIED, RESERVED, and OUT_OF_SERVICE. Developed AdminTableController.java and created the staff dining floor dashboard template (admin/tables.html) featuring visual status cards, color-coded badges, and one-click status toggle buttons. Executed unit and functional tests on table state transitions.
* **Problems Faced:**
  Handling multi-party table matching where party sizes do not exactly match standard table capacities (e.g., seating a party of 3 at a 4-top table without wasting seating efficiency).
* **Plan for Next Week:**
  Implement the core customer queue joining logic, daily sequential token generation, and the customer queue registration web view.

---

### Week 7 (17-08-2026 to 21-08-2026): Customer Queue Onboarding & Token Generation
* **Week:** Week 7
* **GitHub / Report Link:** https://restaurant-que-management-system.onrender.com/
* **Work Completed This Week:**
  Implemented the customer queue registration engine in CustomerQueueService.java. Engineered a daily sequential token generation algorithm (e.g., Q-001, Q-002) ensuring unique token generation per calendar day with thread-safe persistence in SQLite. Implemented wait time estimation logic based on active queue depth multiplied by the average service time per party (5 minutes). Developed the customer queue join web view (customer/join.html) featuring mobile-friendly input controls, party size selector chips, and client-side validation. Reviewed module consistency with existing authentication and navigation structures.
* **Problems Faced:**
  Ensuring atomic token number generation without concurrency collisions or duplicate tokens when multiple customers join simultaneously in SQLite.
* **Plan for Next Week:**
  Build the staff live queue dashboard, implement queue action endpoints (Call, Seat, Skip, Cancel), and integrate customer registration with queue processing.

---

### Week 8 (24-08-2026 to 28-08-2026): Staff Queue Operations & Workflow Integration
* **Week:** Week 8
* **GitHub / Report Link:** https://restaurant-que-management-system.onrender.com/
* **Work Completed This Week:**
  Integrated the customer registration module with the staff queue management subsystem. Developed AdminQueueController.java and the staff operations view (admin/queue.html). Implemented core staff queue actions: Call Customer, Seat Customer, Skip Entry, and Cancel Entry. Configured automated table assignment when seating a called token and established audit logging that moves finished tokens from queue_entries into queue_history. Ensured that token numbers and queue statuses remain consistent across customer and staff views.
* **Problems Faced:**
  Ensuring real-time synchronization so that when a token status is altered by staff, customer-facing views reflect the change immediately without state corruption.
* **Plan for Next Week:**
  Build the public customer queue tracking status page, integrate client-side polling, and implement customer self-cancellation.

---

### Week 9 (31-08-2026 to 04-09-2026): Queue Status Tracking & Dynamic Polling
* **Week:** Week 9
* **GitHub / Report Link:** https://restaurant-que-management-system.onrender.com/
* **Work Completed This Week:**
  Built the public customer queue status page (customer/status.html) displaying live position, estimated wait time, and current token state. Implemented lightweight client-side polling via JavaScript fetch/AJAX on the status tracking view so customers can observe position advancements without manual page reloads. Created the customer self-cancellation endpoint, allowing waiting diners to leave the queue directly from their phone ticket. Added custom business exceptions (InvalidStatusTransitionException, QueueEntryNotFoundException) to prevent illegitimate status mutations. Conducted end-to-end simulation of queue lifecycle events.
* **Problems Faced:**
  Handling aggressive client polling that could overload the SQLite connection pool. Implemented sensible polling intervals (10-15s) and cached queue size queries to maintain low CPU overhead.
* **Plan for Next Week:**
  Plan and begin the multi-restaurant QR-code upgrade using the ZXing library to allow customers to join specific restaurant queues via physical QR scans.

---

### Week 10 (07-09-2026 to 11-09-2026): QR Code Integration & Multi-Restaurant Architecture
* **Week:** Week 10
* **GitHub / Report Link:** https://restaurant-que-management-system.onrender.com/
* **Work Completed This Week:**
  Implemented the QR-first multi-restaurant upgrade based on project specifications. Extended entity models with the Restaurant entity and unique public slugs, maintaining data isolation per branch. Integrated Google ZXing barcode library (core and javase 3.5.3) to dynamically generate base64 QR code PNG matrices. Implemented the direct QR join endpoint (/qr/{slug}) allowing customers scanning physical posters to bypass directory navigation and join the queue immediately. Developed a printable tabletop poster view (customer/qr_print.html) for dining venues to display physical QR signage.
* **Problems Faced:**
  Generating high-resolution QR codes dynamically in-memory without straining server heap memory or creating orphaned image files on the disk.
* **Plan for Next Week:**
  Implement advance table reservation functionality for future dining dates and integrate reservations with the live queue system.

---

### Week 11 (14-09-2026 to 18-09-2026): Advance Table Reservations Module
* **Week:** Week 11
* **GitHub / Report Link:** https://restaurant-que-management-system.onrender.com/
* **Work Completed This Week:**
  Developed the Advance Table Reservation subsystem. Created the Reservation JPA entity and repository. Built the customer reservation booking interface (customer/reserve.html) with calendar date picking, time slot selectors, and guest count validation. Implemented backend capacity validation logic to prevent slot overbooking beyond venue seating limits. Created the staff reservation management portal (admin/reservations.html) and linked reservation check-in with expedited queue seating.
* **Problems Faced:**
  Synchronizing advance reservation slots with walk-in table inventory to ensure tables reserved for upcoming time windows are not accidentally assigned to walk-in queue guests.
* **Plan for Next Week:**
  Build the business analytics dashboard, aggregate historical queue data, and calculate key performance metrics (average wait time, turnover, peak hours).

---

### Week 12 (21-09-2026 to 25-09-2026): Analytics Engine & Operational Reporting Dashboard
* **Week:** Week 12
* **GitHub / Report Link:** https://restaurant-que-management-system.onrender.com/
* **Work Completed This Week:**
  Developed the operations analytics engine in AnalyticsService.java. Wrote custom Spring Data JPA aggregation queries on the queue_history table to calculate total customer volume, average wait duration, peak customer arrival windows, and cancellation rates. Built the executive analytics dashboard (admin/analytics.html) with visual metric summary cards and volume breakdown charts. Optimized SQLite indexing on query columns (restaurant_id, created_at, status) to guarantee sub-100ms dashboard load times.
* **Problems Faced:**
  Formulating complex date-truncation and interval grouping queries in SQLite SQL syntax compared to standard PostgreSQL/MySQL date functions.
* **Plan for Next Week:**
  Undertake overall UI/UX polishing, design system standardization, mobile view responsiveness testing, and security auditing.

---

### Week 13 (28-09-2026 to 02-10-2026): UI/UX Refinement & Security Auditing
* **Week:** Week 13
* **GitHub / Report Link:** https://restaurant-que-management-system.onrender.com/
* **Work Completed This Week:**
  Executed a comprehensive UI/UX refinement across all application views. Standardized modern color themes, typography, button hover states, and card elevations. Enhanced GlobalExceptionHandler.java and created a polished error template (error/error.html). Conducted a full security audit verifying CSRF token enforcement on POST endpoints, SQL injection immunity via JPA parameterization, and XSS sanitization. Tested layout responsiveness across mobile viewports using device emulation. Conducted peer code reviews to ensure clean coding conventions.
* **Problems Faced:**
  Fixing CSS layout wrapping issues on small mobile screens (iPhone SE / Android devices) on the customer live queue status card and token ticket modal.
* **Plan for Next Week:**
  Develop automated test suites with JUnit 5 / MockMvc, compile the formal 33-page Project Report, and configure production container deployment.

---

### Week 14 (05-10-2026 to 09-10-2026): Automated Testing, Documentation & Cloud Deployment
* **Week:** Week 14
* **GitHub / Report Link:** https://restaurant-que-management-system.onrender.com/
* **Work Completed This Week:**
  Developed automated unit and integration tests (BaselineQueueFlowTest, RealtimeQueueApiTest, QrQueueIntegrationTest, MultiRestaurantIsolationTest) covering queue lifecycles and multi-restaurant isolation. Compiled the comprehensive 33-page formal Major Project Report (Restaurant_Queue_Management_System_Project_Report.docx and .md) following university guidelines. Configured deployment containerization files (Dockerfile, render.yaml) and deployed the live application to Render cloud hosting with persistent SQLite database storage.
* **Problems Faced:**
  Configuring persistent SQLite disk storage and environment port binding on Render to ensure database records persist across container restarts.
* **Plan for Next Week:**
  Perform final runtime verification, validate all live cloud and local endpoints, prepare presentation slides, and submit final project deliverables.

---

### Week 15 (12-10-2026 - Final Submission Week): Final Verification & Project Submission
* **Week:** Week 15
* **GitHub / Report Link:** https://restaurant-que-management-system.onrender.com/
* **Work Completed This Week:**
  Conducted final end-to-end testing of the live deployed system on Render (https://restaurant-que-management-system.onrender.com/) and verified local Eclipse execution on port 8080. Validated all user journeys: QR code scan, customer queue joining, live ticket polling, staff table seating, reservations, and analytics. Prepared the final demonstration checklist, compiled daily work logs, and finalized all project evaluation submissions.
* **Problems Faced:**
  Minor port conflict during local testing when another background JVM instance was holding port 8080; diagnosed via process management and successfully resolved.
* **Plan for Next Week:**
  Conduct the final project defense and viva presentation before the department evaluation committee. Explore future enhancements such as automated WhatsApp notification integration.

---
