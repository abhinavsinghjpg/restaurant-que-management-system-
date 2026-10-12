# Restaurant Queue Management System

A Java-based web application designed to help restaurants organize
customer queues, issue tokens, track queue status, and coordinate table
assignments through a centralized interface.

## Project Overview

Restaurants can experience delays when customer waiting lists and table
availability are managed manually. This project aims to make the process
more organized through customer registration, token identification,
queue-status tracking, and staff-side queue/table management.

**Technology:** Java 17, Spring Boot 3.2.5, Spring Security, Spring Data
JPA, Thymeleaf, HTML, CSS, JavaScript, and SQLite.

> **Project status:** Academic project under development. Confirm
> feature availability against the current code before describing a
> feature as complete.

## Objectives

-   Register customers in the restaurant queue.
-   Generate and display a token for each queue entry.
-   Track queue status as staff process customers.
-   Give staff a central view of queue entries.
-   Help staff review table availability and manage table assignments.
-   Keep the application understandable for customers and restaurant
    staff.

## Project Modules and Ownership

The project is divided into **five modules**, with one primary owner assigned to each module. Each owner is responsible for the implementation, testing, and documentation of their assigned module. Team members can collaborate when integration between modules is required.

### Module 1: Authentication and Access Control
**Owner: Harsh dhonkariya**

**Purpose:** Manage user login and protect application pages that require authentication.

**Responsibilities**
- Maintain the login workflow.
- Configure and review Spring Security access control.
- Ensure users can access the appropriate pages after authentication.
- Check authentication behavior and coordinate integration with other modules.
- Document the authentication setup and any required configuration.

### Module 2: Customer Queue and Token Management
**Owner: Abhinav Chaudhary**

**Purpose:** Allow customers to register in the queue and identify their queue entry using a token.

**Responsibilities**
- Build and maintain the customer registration workflow.
- Handle customer input and basic validation.
- Generate or assign a token according to the implemented design.
- Display token and queue-status information.
- Test registration, token identification, and queue display.
- Document the customer queue workflow.

### Module 3: Staff Queue and Table Management
**Owner: Abhinav Kumar**

**Purpose:** Help restaurant staff process waiting customers and manage table availability and assignments.

**Responsibilities**
- Maintain the staff-facing queue view.
- Display token numbers and queue statuses.
- Support queue-status updates where implemented.
- Display table information and availability.
- Support table assignment and consistent queue/table updates where implemented.
- Test staff queue and table-management workflows.

### Module 4: User Interface and Documentation
**Owner: Ishika sharma**

**Purpose:** Make the application screens understandable and maintain clear user-facing and project documentation.

**Responsibilities**
- Review page layouts, labels, instructions, and navigation.
- Check that customer and staff screens are easy to understand.
- Prepare beginner-friendly documentation for the application workflow.
- Capture screenshots for the project report when available.
- Record usability issues and share them with the relevant module owner.
- Help keep the README and project documentation clear and consistent.

### Module 5: Testing and Quality Assurance
**Owner: Anjun saini**

**Purpose:** Check that application features behave as expected and maintain organized testing records.

**Responsibilities**
- Prepare fictional sample customer and queue data.
- Create manual test cases for login, registration, token generation, queue status, and table management.
- Follow test steps and compare actual results with expected results.
- Record failed tests and report reproducible issues to the relevant module owner.
- Retest features after fixes and document the results.
- Maintain testing checklists and a testing summary.

### Module Ownership Summary

| Module | Primary owner | Main deliverables |
|---|---|---|
| Authentication and Access Control | **Harsh dhonkariya** | Login/access-control workflow and configuration notes |
| Customer Queue and Token Management | **Abhinav Chaudhary** | Customer registration, token workflow, and queue-status display |
| Staff Queue and Table Management | **Abhinav Kumar** | Staff queue view, queue updates, and table-management workflow |
| User Interface and Documentation | **Ishika sharma** | UI review notes, screenshots, and project documentation |
| Testing and Quality Assurance | **Anjun saini** | Sample data, manual test cases, test results, and issue/retest records |

**Ownership note:** Each member owns one primary module. Integration and review can be collaborative, but the assigned owner is responsible for tracking the module's progress. Features should be described as completed only after they are implemented and verified in the actual application.

## Core Functional Modules

### 1. Authentication and Access Control
- Login workflow for application users.
- Access control using Spring Security.
- Navigation to relevant application pages after authentication.

### 2. Customer Registration and Token Management
- Collect customer information through a registration interface.
- Create a queue entry and associate it with a token.
- Display token and queue information.
- Support queue-status visibility according to the implemented workflow.

### 3. Staff Queue Management
- View registered queue entries.
- Review token numbers and current queue status.
- Update queue entries as customers are processed, where implemented.

### 4. Table Management
- Review table information and availability.
- Associate a customer with a table when appropriate.
- Keep queue and table information consistent according to the implemented workflow.

### 5. Dashboard and Reporting
- Provide a central overview of queue or table information where implemented.
- Support project review with screenshots, test results, and documentation.

## Technology Stack

  -----------------------------------------------------------------------
  Technology                          Purpose
  ----------------------------------- -----------------------------------
  Java 17                             Main programming language

  Spring Boot 3.2.5                   Application framework

  Spring Security                     Authentication and access control

  Spring Data JPA                     Data persistence

  SQLite                              Database in the current application
                                      configuration

  Thymeleaf                           Server-side HTML templates

  HTML, CSS, JavaScript               Web interface

  Maven                               Build and dependency management, if
                                      configured in the repository
  -----------------------------------------------------------------------

## High-Level Workflow

1.  A user opens the application and signs in if authentication is
    required.
2.  A customer is registered in the queue.
3.  The application creates or displays a token for that queue entry.
4.  Staff review the queue and process waiting customers.
5.  Staff review table availability and assign a table when appropriate.
6.  Queue and table information are updated according to the implemented
    workflow.

## Getting Started

### Prerequisites

-   Java Development Kit (JDK) 17
-   Git
-   Maven, if the project does not include the Maven Wrapper
-   An IDE such as IntelliJ IDEA, Eclipse, or Visual Studio Code with
    Java support

### Clone the repository

Replace `<your-repository-url>` and `<your-project-folder>` with your
repository details.

``` bash
git clone <your-repository-url>
cd <your-project-folder>
```

### Run the application

If the repository includes the Maven Wrapper:

**Windows**

``` bash
mvnw.cmd spring-boot:run
```

**macOS/Linux**

``` bash
./mvnw spring-boot:run
```

If Maven is installed globally:

``` bash
mvn spring-boot:run
```

You can also import the project into your IDE as a Maven project and run
the Spring Boot application class.

### Database configuration

The current project configuration uses SQLite. Review
`src/main/resources/application.properties` and confirm that the
database path and JPA settings match your local setup.

Do not commit passwords, private credentials, or local database files
containing real customer information to GitHub.

### Open the application

After the application starts, open the local address configured by the
project. The default Spring Boot address is commonly:

``` text
http://localhost:8080
```

If the project uses a different port, use the value in
`application.properties`.

## Suggested Repository Structure

The exact structure should match your actual repository. A typical
organization could look like this:

``` text
restaurant-queue-management-system/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── .../
│   │   │       ├── config/          # Security and application configuration
│   │   │       ├── controller/      # Web request handlers
│   │   │       ├── model/           # Entity and model classes
│   │   │       ├── repository/      # Data access
│   │   │       └── service/         # Business logic
│   │   └── resources/
│   │       ├── templates/           # Thymeleaf HTML pages
│   │       ├── static/              # CSS, JavaScript, and images
│   │       └── application.properties
│   └── test/                        # Automated tests, if present
├── README.md
├── pom.xml
└── .gitignore
```

This is an example only. Do not rename existing folders just to match
this diagram.

## Testing Checklist

Mark an item complete only after checking it in the running application.

-   [ ] Login works with valid test credentials.
-   [ ] Invalid login attempts are handled appropriately.
-   [ ] Customer registration validates required inputs.
-   [ ] A queue entry is saved successfully.
-   [ ] A token is generated or assigned as designed.
-   [ ] Queue information displays the correct token and status.
-   [ ] Staff can view queue entries they are authorized to access.
-   [ ] Queue-status changes behave as expected.
-   [ ] Table availability is displayed correctly.
-   [ ] Table assignment does not create inconsistent records.
-   [ ] Navigation and error messages are understandable.
-   [ ] Database records remain consistent after relevant operations.

## Team Collaboration

Team members should work on their assigned areas, test changes before
integration, describe changes clearly in commit messages, coordinate
when changes affect another module, and update documentation when
behavior changes.

Example commit messages:

``` text
feat: add customer queue token workflow
feat: improve staff queue management
fix: validate table assignment
docs: update setup and module ownership
test: add queue registration test cases
```

## Potential Future Improvements

Depending on project requirements and available time, future
improvements may include:

-   More complete input validation and error handling.
-   Improved queue-status updates and customer notifications.
-   Stronger safeguards around table assignment.
-   Additional dashboard summaries for staff.
-   More automated tests.
-   Responsive design for mobile screens.
-   Deployment configuration and production database planning.

## Academic Project Team

-   **Harsh** --- Authentication, integration, and coordination
-   **Abhinav C** --- Customer queue and token management
-   **Abhinav K** --- Staff queue and table management
-   **Ishika** --- UI/UX review and documentation
-   **Anjun** --- Testing, sample data, and test documentation

## License

This project was developed for academic purposes. Add a license to this
repository if you intend to distribute or reuse the code publicly.
