# FINAL SCOPE DECISION — QR-FIRST MULTI-RESTAURANT (OVERRIDES CONFLICTING SECTIONS)

**Approved design:** One Spring Boot modular-monolith application, one SQLite database, multiple restaurants with separate queues, and one unique QR code per restaurant. **No public restaurant directory, no customer restaurant search, and no generic customer home page for choosing restaurants.** Customer entry is by scanning the restaurant's QR code (or opening its direct QR URL). The QR opens a public page showing that restaurant's current queue summary and a form to generate a token. Customer can then track their token automatically.

A minimal internal `Restaurant` record (unique ID/public QR slug, status, optional display name, etc.) **is required** for data ownership, staff assignment, admin management, and isolation. The restaurant name need not be part of public search or a customer selection flow. Keep names only if operationally useful for admins/staff. QR URLs must contain an unguessable or stable public identifier, **not a secret credential**. Do not expose private customer details in the public waiting list: show anonymized tokens and positions only.

**Roles:** Admin can manage restaurants and staff; staff can manage only authorized restaurant queues/orders; customers can join and track their own token without staff login. Maintain current login behavior while extending permissions safely.

**Other approved features:** staff queue advance/skip/no-show/reorder with audit history; served tokens leave the active queue but are retained for analytics; auto-refresh/polling; reservations; bar/pie analytics; demo menu and basic food-order workflow; incremental UI improvements. Implement in tested phases and preserve existing SQLite records. **Do not migrate databases or rewrite working code.**

**Conflict rule:** This final scope decision supersedes any older text in this document or existing PRD/prompt calling for public restaurant discovery, name search, or a restaurant-selection home page.

---

# Restaurant Queue Management System
## Updated Feature Specification and Implementation Plan (SQLite)

**Project target:** Strong college project  
**Current stack:** Java 17, Spring Boot 3.2.x, Maven, Spring MVC, Thymeleaf, Spring Security, Spring Data JPA/Hibernate, SQLite  
**Architecture:** Modular monolith  
**Database decision:** Keep SQLite. Do not migrate to PostgreSQL in this upgrade.

---

## 1. Objective

Upgrade the existing Restaurant Queue Management System with five substantial features while preserving the current working application:

1. Multi-restaurant support
2. QR-based queue joining
3. Real-time queue status updates
4. Reservations
5. Analytics dashboard

Improve the existing UI rather than replacing it from scratch.

The project should remain understandable, testable, demonstrable, and easy to extend for a college evaluation.

## 2. Non-negotiable constraints

- Keep SQLite and the existing database configuration.
- Do not switch to PostgreSQL or another database.
- Preserve current login, customer queue, queue management, table management, and dashboard behavior.
- Do not delete working code or perform a full rewrite.
- Inspect the current source files before making changes.
- Reuse existing entities, services, repositories, controllers, and templates when appropriate.
- Make changes in small phases. Build and test after each phase.
- Back up `restaurant_queue.db` before schema-changing work.
- Do not use `ddl-auto=create` or `create-drop`; these can destroy existing data.
- Avoid unrelated dependency upgrades and broad refactoring.
- Keep the application runnable after every phase.

## 3. Current system baseline

The existing codebase is understood to use Java 17, Spring Boot, Thymeleaf, Spring Security, Spring Data JPA/Hibernate, and SQLite. Existing modules include authentication, customer queue, queue management, table management, dashboard, and shared/common functionality.

Existing domain concepts include users, queue entries, restaurant tables, and queue history. Confirm exact class names, fields, routes, and current behavior from the actual repository before editing. Treat this list as a guide, not permission to overwrite the current implementation.

## 4. Target modular architecture

Keep a modular monolith: one deployable Spring Boot application and one SQLite database, with clear feature boundaries.

Suggested package/module boundaries (adapt to the current project instead of moving everything unnecessarily):

- `authentication` — login, roles, access control
- `restaurant_management` — restaurant creation, settings, membership and restaurant context
- `customer_queue` — public queue joining and customer token/status
- `queue_management` — staff queue operations and status transitions
- `table_management` — table inventory and table availability
- `reservations` — booking, cancellation, confirmation, and reservation status
- `qr_access` — restaurant-specific QR links and QR generation
- `dashboard` — operational dashboard and analytics views
- `common` — shared validation, errors, utilities, and security helpers

Each module should own its controllers, services, repositories, DTOs/forms, and templates where appropriate. Do not create unnecessary abstractions or duplicate business logic.

## 5. Feature requirements

### 5.1 Multi-restaurant support

**Goal:** Allow the application to manage more than one restaurant without mixing their data.

Requirements:
- Introduce a `Restaurant` entity only after inspecting the existing model.
- Associate restaurant-owned records with a restaurant, including queue entries, tables, reservations, and relevant history records.
- Provide a safe way to select or resolve the active restaurant for authenticated staff.
- Provide restaurant-specific public queue and QR URLs.
- Filter every staff query and dashboard metric by the authorized restaurant.
- Enforce authorization in services/controllers, not only by hiding UI elements.
- Prevent a staff user from viewing or changing another restaurant's records by editing a URL or request parameter.
- Provide a default/demo restaurant or a safe migration/backfill path for existing records.
- Do not make existing records inaccessible during the transition.

Acceptance checks:
- Two restaurants can have independent queues and tables.
- A user authorized for Restaurant A cannot read or modify Restaurant B's queue, tables, reservations, or analytics.
- Existing records are assigned safely to a valid restaurant.
- Existing single-restaurant workflows still work.

### 5.2 QR-based queue joining

Requirements:
- Generate a QR code for each restaurant's public queue page.
- QR code should encode a local application URL, not contain sensitive information.
- Scanning the code opens the restaurant's queue page on a phone.
- Customer can enter the minimum required details and join the queue.
- Prevent accidental duplicate submissions with server-side validation and a clear confirmation screen.
- Show the generated token/queue number and a link to check status.
- Validate restaurant identifiers and handle invalid or inactive restaurants gracefully.
- Add a print-friendly QR page for the restaurant staff.
- Do not require customers to create an account unless the existing product requirements explicitly call for it.

Acceptance checks:
- QR links open the correct restaurant.
- A valid scan-to-join flow creates one queue entry.
- Invalid restaurant links do not create entries.
- QR generation works in the deployed environment, not only on a developer machine.

### 5.3 Real-time queue updates

**Preferred first implementation:** lightweight polling, unless the existing codebase already has a suitable WebSocket/SSE foundation. Avoid adding unnecessary infrastructure.

Requirements:
- Customer status page refreshes queue position and estimated wait at a configurable interval.
- Staff queue screen refreshes current queue information without a full manual page reload.
- Return only the required status fields through a dedicated endpoint.
- Include a visible last-updated state or graceful fallback if an update request fails.
- Stop or slow polling when a page is hidden if feasible, and avoid overly frequent requests.
- Keep server-side state authoritative; the browser must not decide queue order.
- Ensure the endpoint only returns information that the current user is allowed to access.

Acceptance checks:
- A status change by staff appears on customer/staff screens within the polling interval.
- A network failure does not crash the page.
- Unauthorized users cannot query another customer's private details.
- The implementation works with the current Spring Boot/Thymeleaf stack.

### 5.4 Reservations

Requirements:
- Customer can request a reservation with date, time, party size, and contact details.
- Staff can view reservations for their restaurant and filter by date/status.
- Support statuses such as `PENDING`, `CONFIRMED`, `CANCELLED`, `COMPLETED`, and `NO_SHOW`, adjusted to match existing conventions.
- Validate required fields, past dates, reasonable party size, and cancellation rules.
- Avoid double-booking where table/slot capacity is managed.
- Make reservation availability rules explicit and configurable rather than claiming availability based only on a UI check.
- Keep reservations separate from walk-in queue entries, but allow staff to manage both from a coherent interface.
- If table assignment is included, ensure the same table cannot be assigned to overlapping reservations.
- Use server-side transactional validation. SQLite serializes writes, so keep write transactions short and handle lock/busy errors gracefully.

Acceptance checks:
- A customer can submit a valid reservation.
- Invalid or past reservations are rejected with understandable feedback.
- Staff can confirm/cancel/update a reservation.
- Conflicting table assignments are rejected.
- Restaurant A's staff cannot access Restaurant B's reservations.

### 5.5 Analytics dashboard

Requirements:
- Add a dedicated analytics section or improve the current dashboard without breaking its existing metrics.
- Provide date-range filtering.
- Show useful metrics such as:
  - Total queue visits
  - Average and median wait time where enough valid data exists
  - Average time from joining to being seated/served
  - Peak queue hours
  - Completed, cancelled, and no-show counts
  - Reservation count and completion/cancellation rates
  - Table utilization only if the existing data supports a defensible calculation
- Scope every metric to the selected/authorized restaurant.
- Handle empty data and incomplete timestamps without exceptions.
- Label metric definitions clearly; do not invent precise values when the stored data cannot support them.
- Prefer server-side aggregation for larger datasets and keep database queries efficient.
- Use the existing charting/UI approach if one exists; do not add a large chart library without need.

Acceptance checks:
- Metrics change correctly with restaurant and date filters.
- Empty ranges show a helpful empty state.
- A small manually verified dataset produces expected results.
- Analytics do not expose cross-restaurant information.

## 6. UI direction

Improve the existing UI incrementally:
- Preserve the current visual identity and working navigation where practical.
- Improve spacing, typography, hierarchy, responsive behavior, forms, tables, badges, and empty/error states.
- Ensure staff workflows are usable on desktop and mobile.
- Ensure public queue and reservation pages are phone-friendly.
- Add clear loading, success, validation, and error feedback.
- Keep accessibility in mind: labels, keyboard navigation, contrast, and status text.
- Do not redesign every page before the core functionality works.

## 7. SQLite and data integrity rules

- Keep the current SQLite JDBC URL, driver, and Hibernate dialect unless a verified issue requires a minimal fix.
- Back up the database file before schema changes.
- Inspect existing schema and records before adding columns/tables or changing relationships.
- Prefer explicit, repeatable schema migrations (for example, Flyway if it can be introduced safely) or a documented, tested migration process. Do not add a migration tool blindly if it conflicts with the existing schema.
- Never silently delete or reset existing data.
- Use foreign keys and indexes where supported and appropriate.
- Add indexes for frequent lookups, including restaurant-scoped queue status/date queries and reservation date/status queries.
- Use short write transactions. SQLite is suitable for this college project and modest traffic, but concurrent writes are serialized; handle busy/locked failures sensibly.
- Confirm the database file path and persistence behavior in the run/deployment instructions.
- Do not promise production-scale multi-instance concurrency from a local SQLite file.

## 8. Implementation phases and gates

### Phase 0 — Baseline and safety
1. Inspect the repository, build file, application properties, entity mappings, security rules, controllers, templates, and current routes.
2. Record existing features and known defects.
3. Run the application and current tests/build.
4. Back up the SQLite database.
5. Add or improve tests around existing login, queue joining/status, queue management, tables, and dashboard behavior.
6. Report any current build or runtime failures before changing unrelated code.

**Gate:** Existing app builds and the baseline is documented.

### Phase 1 — Multi-restaurant foundation
1. Design restaurant ownership and user-to-restaurant authorization.
2. Add the restaurant model and safe handling/backfill for existing records.
3. Add restaurant-scoped repository/service queries and authorization checks.
4. Update existing queue, table, history, and dashboard paths to use the correct restaurant.
5. Test data isolation and legacy data access.

**Gate:** Existing features still work, and cross-restaurant access tests pass.

### Phase 2 — QR queue entry
1. Add a restaurant-specific public queue URL.
2. Add QR generation and print-friendly view.
3. Connect QR entry to the existing queue-join service.
4. Add validation, duplicate-submit protection, and tests.

**Gate:** End-to-end QR scan-to-token flow works.

### Phase 3 — Real-time updates
1. Add a minimal status DTO and authorized status endpoint.
2. Add polling to customer and staff pages.
3. Add loading/error/last-updated behavior.
4. Test status transitions, permissions, and failure cases.

**Gate:** Queue changes appear automatically and securely.

### Phase 4 — Reservations
1. Define reservation model, validation, statuses, and capacity rules.
2. Add customer booking and cancellation flow.
3. Add staff reservation management.
4. Add conflict checks and transaction-level tests.
5. Add restaurant isolation tests.

**Gate:** Valid bookings work and conflicting assignments are rejected.

### Phase 5 — Analytics
1. Define each metric precisely and map it to available persisted data.
2. Implement restaurant-scoped queries and date filters.
3. Add dashboard cards/charts and empty states.
4. Verify metrics with seeded/manual test data.

**Gate:** Metric calculations are explainable and verified.

### Phase 6 — UI polish and release verification
1. Improve existing templates and responsive layouts.
2. Check form validation, success/error states, and navigation.
3. Run all tests and a clean Maven build.
4. Test fresh database startup and existing database upgrade.
5. Update README with setup, demo accounts, SQLite file location, backup, and feature walkthrough.
6. Summarize changed files, database changes, tests run, and known limitations.

**Gate:** The project can be demonstrated from a clean checkout with clear setup steps.

## 9. Required testing

At minimum, test:
- Existing authentication and role restrictions
- Join queue and token generation
- Queue status transitions
- Table CRUD and availability
- Existing dashboard
- Restaurant scoping and cross-restaurant access denial
- QR URL generation and invalid links
- Polling endpoint permissions and status updates
- Reservation validation, cancellation, and conflicts
- Analytics date filtering and known-value calculations
- Empty data, malformed input, and database lock/busy handling
- Existing SQLite database upgrade without data loss

Use the test framework already present where possible. If there are no tests, add focused tests without rewriting the build unnecessarily.

## 10. Definition of done

The upgrade is complete only when:
- The project still uses SQLite.
- Existing features remain operational.
- The five requested features work end to end.
- Restaurant data isolation is enforced on the server.
- Existing data is preserved.
- The app builds and tests pass, or any pre-existing failures are clearly identified.
- The README explains setup, database backup, demo flow, and limitations.
- Antigravity provides a concise implementation report with files changed, schema changes, tests run, and remaining issues.

## 11. Instruction for the coding agent

Work in small, reviewable phases. Begin with Phase 0 and report findings before making major changes. Do not implement all features in one giant rewrite. After each phase, run the build/tests, list files changed, explain database changes, and wait for review if a destructive or high-impact decision is required. SQLite is an intentional decision for this iteration and must not be changed.


## 12. Additional requirements from the latest user notes

These requirements extend the selected upgrade features. Implement them without removing existing features and while keeping SQLite.


### 12.2 Dedicated admin role and staff management
- Add a clear role model that distinguishes at least `ADMIN` and `STAFF`; retain customer/public access separately as appropriate to the existing security design.
- Inspect the existing authentication and user model first. Do not replace the working login flow unnecessarily.
- Admin can add staff accounts, edit permitted staff details, deactivate/delete staff where safe, and assign staff to a restaurant.
- Validate unique usernames/emails and enforce strong password handling using the existing Spring Security password encoder.
- Admin-only operations must be protected on the server, not merely hidden from the UI.
- Prevent staff from creating admins or changing their own role unless explicitly authorized.
- When a staff account is deactivated, its login must stop working while audit/history records remain intact.
- Decide whether an existing user can belong to one or multiple restaurants based on the smallest safe design for the current codebase; document the decision.

Acceptance checks:
- Admin can create and deactivate a staff account.
- Staff cannot access admin-only routes or manage other staff.
- Staff can only work with the restaurant(s) they are authorized for.
- Existing user accounts and login behavior are preserved.

### 12.3 Staff work and queue controls
- Give staff a practical work screen for queue operations and, if food ordering is enabled, order status updates.
- Staff can advance the queue to the next customer.
- If a customer does not show up, staff can mark the token `NO_SHOW`/`SKIPPED` and move to the next eligible token.
- Provide a controlled way to move a token backward or forward in the queue only when operationally valid and authorized; record the action in queue history.
- Prevent arbitrary deletion of tokens as a substitute for status transitions.
- Make current token, next token, waiting count, and token status clear in the UI.
- Require confirmation for disruptive actions such as marking no-show or cancelling an active token.

Acceptance checks:
- Staff can move the queue forward and mark a no-show.
- Skipped/no-show tokens remain available in history and analytics.
- Queue ordering remains consistent after staff actions.
- Unauthorized users cannot change queue state.

### 12.4 Token lifecycle after service
- When a customer has been served, remove the token from the active queue view automatically.
- Do not permanently delete the underlying record. Mark it as `SERVED`/`COMPLETED`, store completion time, and retain history for analytics and audit.
- If food orders are part of the flow, define whether the queue token is considered served when the customer is seated, when the order is delivered, or when staff explicitly completes service. Use one clear rule and document it.
- Ensure the active queue query excludes completed, cancelled, and no-show tokens while history/analytics queries can still include them.
- Make this transition idempotent so repeated clicks or requests do not corrupt the record.

Acceptance checks:
- Served tokens disappear from the active queue without being erased from the database.
- Historical analytics still count completed service.
- Repeating a completion action does not create duplicate history or invalid state.

### 12.5 Dummy menu and food orders
- Add a clearly labelled demo menu with sample items, prices, and availability for demonstration purposes.
- Allow customers to view the menu and, if ordering is enabled for the selected restaurant, submit a simple food order associated with their queue token or reservation.
- Provide order states such as `PLACED`, `PREPARING`, `READY`, `SERVED`, and `CANCELLED`, adjusted to the existing domain model.
- Staff should have an order work screen to update order status and see the token/table reference needed to serve the customer.
- Validate quantities and prices on the server. Never trust client-submitted prices; calculate totals from persisted menu item prices.
- Keep menu items and orders restaurant-scoped.
- If full food ordering would destabilize the current scope, implement a minimal demo flow after the core queue, reservations, and permissions are stable.

Acceptance checks:
- Demo menu is visible and labelled as sample data.
- A customer can submit a valid order, and staff can update its status.
- Prices/totals are calculated on the server.
- Orders cannot be viewed or changed across restaurants.

### 12.6 Analytics visualizations
- Improve analytics with a bar chart for queue/service volume over a selected period and a pie/doughnut chart showing queue outcomes/status distribution.
- Include counts for people/customers who used the service, based on completed or otherwise clearly defined visits.
- Include current waiting-line count separately from historical people served.
- Add date filters and restaurant scope to every visualization.
- Show the metric definitions, a useful empty state, and accessible text labels so the chart is not the only source of information.
- Use an existing chart library if present; otherwise choose a lightweight option only after inspecting current dependencies.

Acceptance checks:
- Bar chart and pie/doughnut chart reflect the selected restaurant/date range.
- Chart totals match the underlying counts.
- Empty datasets do not break the dashboard.
- Data remains isolated by restaurant.

## 13. Updated feature priority

1. **Phase 0 — Baseline and backup:** inspect current implementation, run build/tests, back up SQLite.
2. **Phase 1 — Roles, restaurant ownership, and data isolation:** establish admin/staff permissions and restaurant scope before exposing multi-restaurant workflows.
3. **Phase 2 — Customer home page and restaurant search:** restaurant discovery and visible restaurant identity.
4. **Phase 3 — Queue controls and token lifecycle:** next/skip/no-show, queue ordering, served-token archival/history.
5. **Phase 4 — QR queue joining:** connect QR codes to the public restaurant page.
6. **Phase 5 — Real-time updates:** refresh queue and token status automatically.
7. **Phase 6 — Reservations:** customer booking and staff management with conflict prevention.
8. **Phase 7 — Analytics:** date-filtered metrics plus bar and pie/doughnut charts.
9. **Phase 8 — Demo menu and food orders:** implement after the queue and permission foundation is stable.
10. **Phase 9 — UI polish and release verification:** responsive layout, regression tests, and documentation.

Do not implement every phase in one large change. Complete and test each phase before moving to the next.


## 14. Final architecture decision — QR-first multi-restaurant (supersedes conflicting sections)

**This section overrides earlier proposals for a public restaurant directory, restaurant search, three-character keyword search, or a general customer landing page. Do not implement those features.**

- Keep SQLite, the modular monolith, and existing working features.
- Support multiple restaurants. Maintain a minimal `Restaurant` record with a unique identifier, optional display name, status, and necessary administrative metadata. A public directory is not required.
- Each restaurant has a unique, non-secret QR URL such as `/r/{publicId}/queue`. Resolve the public ID server-side. The QR code opens that restaurant's customer queue page directly.
- The customer page displays the current waiting count/appropriate privacy-safe queue information and allows a customer to generate a token. Do not expose customer names, phone numbers, or other personal data in the public waiting list.
- Each queue entry, table, reservation, menu item, order, and relevant history record belongs to one restaurant. Existing data must be safely associated with a default restaurant without being deleted.
- Admin can create/manage restaurants, generate or print their QR codes, and manage restaurant staff. Staff can operate only within their assigned restaurant. Authorization must be enforced server-side.
- Token status updates, queue management, no-show handling, served-token history, reservations, analytics, and demo food ordering remain in scope.
- Keep served tokens in history rather than deleting records; hide them from active queue views.

**Revised implementation sequence:** Phase 0 baseline/SQLite backup; Phase 1 restaurant ownership and admin/staff authorization; Phase 2 restaurant QR URLs and QR-first customer token generation; Phase 3 staff queue controls and served/no-show lifecycle; Phase 4 automatic queue updates; Phase 5 reservations; Phase 6 analytics; Phase 7 demo menu/orders; Phase 8 UI polish, regression tests, and README.

**Required acceptance test:** Two restaurants' QR codes open different queue pages; tokens created from Restaurant A's QR code never appear in Restaurant B's queue; staff and analytics cannot cross restaurant boundaries.
