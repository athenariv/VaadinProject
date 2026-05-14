# Grading
This Vaadin-project was made for the course Java web-ohjelmointi

The target grade for this project is 5.
35/39 features/requirements work as intended, and the remaining 4 require proper auth or proper setup to finish.

# CompanyApp

A company management web application built with Vaadin 23 and Spring Boot 2.7.

The app covers departments, employees, projects, and tasks through a single-page interface with role-based access, audit history, server push, localisation, and a few additional features listed below. This README also serves as a work report that maps each assignment criterion to where it is implemented.

## Tech stack

- Java 11
- Spring Boot 2.7.18
- Vaadin 23.3.32
- H2 (file-based in Docker, in-memory in dev by default)
- Spring Security with VaadinWebSecurity
- Hibernate Envers for revision history
- Spring Data JPA Auditing
- Apache POI for Excel export
- Quill.js via a custom LitElement web component

## Getting started

```bash
cd vaadin/companyapp
./mvnw spring-boot:run
```

Open http://localhost:8080 in your browser.

Default accounts:

| Username | Password  | Roles                             |
|----------|-----------|-----------------------------------|
| admin    | admin123  | ROLE_ADMIN, ROLE_SUPER, ROLE_USER |
| super    | super123  | ROLE_SUPER, ROLE_USER             |
| user     | user123   | ROLE_USER                         |

## Docker

```bash
docker-compose up --build
```

The compose file mounts a named volume for the H2 file database so data persists between restarts.

---

## Criteria

### Data, entities and CRUD

**1. First entity -- Department**

`Department` has its own list view (`DepartmentView`), a create/edit form using `BeanValidationBinder`, and a delete action. The repository is `DepartmentRepository extends JpaRepository`. All CRUD operations go through `DepartmentService` to the database.

**2. Second entity -- EmployeeDetail (1:1 with Employee)**

`EmployeeDetail` has a 1:1 relationship with `Employee` (`@OneToOne`, `@JoinColumn`). The employee detail fields (bio, address, emergency contact, etc.) are shown inline in the employee edit form. The relationship is visible on the employee list because the grid has a detail row that expands to show the linked `EmployeeDetail`.

**3. Third entity -- Employee (N:1 with Department, 1:N side of the relationship)**

`Department` has a 1:N relationship with `Employee` (`@OneToMany` on Department, `@ManyToOne` on Employee). The relationship is shown in the Departments grid: each row displays the department name and a count of its employees, and expanding the row shows the employee list for that department.

**4. Fourth entity -- Project (M:N with Employee via a join table)**

`Employee` and `Project` have a M:N relationship (`@ManyToMany`, join table `employee_project`). The employee edit form has a multi-select for projects. The employee grid shows the assigned project names in a column. `Task` belongs to a `Project` (1:N), giving a fifth entity with its own CRUD view.

**5. At least five validated fields per entity**

Every entity uses JSR-380 annotations (`@NotBlank`, `@Size`, `@Email`, `@Min`, `@Max`, `@Pattern`, `@NotNull`) on at least five fields. `BeanValidationBinder` is used in every form so validation fires before saving and error messages appear next to the fields. Examples:
- `Employee`: firstName, lastName, email, jobTitle, salary, hireDate (six fields)
- `Department`: name, description, location, budget, headCount (five fields)
- `Project`: name, description, startDate, endDate, status (five fields)
- `Task`: title, description, priority, dueDate, status (five fields)
- `EmployeeDetail`: address, phone, emergencyContact, bio, linkedIn (five fields)

---

### Search (Criteria API)

All search logic is in `EmployeeService.searchEmployees(SearchFilter filter)` using `CriteriaBuilder`, `CriteriaQuery`, and a list of `Predicate` objects that are added only when the corresponding filter field is non-empty.

**6. Multiple input fields (at least three)**

The Search view has seven filter inputs: first name/email text field, job title, department (select), hire date from, hire date to, salary min, salary max. Predicates are added dynamically so an empty field has no effect on the query.

**7. Date range filter**

Hire date range uses two `DatePicker` inputs. The predicates are `cb.greaterThanOrEqualTo(root.get("hireDate"), from)` and `cb.lessThanOrEqualTo(root.get("hireDate"), to)`, added only when the respective picker has a value.

**8. Filter by related entity using JOIN**

Department filtering uses `Join<Employee, Department> deptJoin = root.join("department", JoinType.LEFT)` followed by `cb.equal(deptJoin.get("id"), selectedDept.getId())`. This is a JOIN across the employee-department relationship.

**9. Filter by a property of a related entity**

The department join is also used to filter by department name with a LIKE predicate: `cb.like(cb.lower(deptJoin.get("name")), "%" + term.toLowerCase() + "%")`. This filters employees by a property of their related department, not just its ID.

**10. Complex query with (X OR Y) AND Z structure**

The advanced search toggle activates a compound predicate: `(firstName LIKE :term OR email LIKE :term) AND department.name LIKE :dept`. This is built as `cb.and(cb.or(firstNameLike, emailLike), deptNameLike)` and added alongside the other predicates.

---

### Styles and appearance

**11. Global style changes**

`themes/companyapp/styles.css` sets a custom font (imported from Google Fonts), overrides Lumo CSS variables for the primary colour palette, border radius (`--lumo-border-radius-m`), and button shadow depth (`--lumo-button-shadow`).

**12. Component style via addClassName, getStyle().set(), and addThemeVariants**

All three methods are used in the codebase:
- `addClassName`: card components in `HomeView` use `card.addClassName("home-card")`, `home-card--clickable`, etc.
- `getStyle().set()`: layout wrappers use `getStyle().set("min-height", "100%")`, `getStyle().set("gap", "1rem")`, etc.
- `addThemeVariants` / `setThemeVariants`: buttons use `ButtonVariant.LUMO_PRIMARY`, `LUMO_ERROR`, `LUMO_TERTIARY`; grids use `GridVariant.LUMO_ROW_STRIPES`, `LUMO_COMPACT`; notifications use `NotificationVariant.LUMO_SUCCESS`, `LUMO_ERROR`.

**13. View-specific CSS**

`HomeView` has a dedicated CSS file `themes/companyapp/views/home-view.css` loaded via `@CssImport`. The selectors target only `.home-card` and its children, so the styles affect multiple Vaadin components (H3 heading, Paragraph, the card container itself) only within the home view.

**14. Five Lumo Utility classes in a view**

`HomeView` uses five Lumo Utility classes on its layout and card components:
- `LumoUtility.Padding.LARGE` on the main layout
- `LumoUtility.Background.BASE` on each card
- `LumoUtility.BorderRadius.LARGE` on each card
- `LumoUtility.BoxShadow.SMALL` on each card
- `LumoUtility.TextColor.SECONDARY` on the card descriptions

**15. Hover, focus, and transition CSS**

`home-view.css` defines transitions and interactive states on `.home-card`:
```css
.home-card {
    transition: transform 0.2s ease, box-shadow 0.2s ease;
    cursor: pointer;
}
.home-card:hover {
    transform: scale(1.03);
    box-shadow: var(--lumo-box-shadow-m);
}
.home-card:focus-within {
    outline: 2px solid var(--lumo-primary-color);
    outline-offset: 2px;
}
```
The login form buttons also have a fade-in transition defined in the global stylesheet.

---

### Layout and structure (SPA)

**16. SPA structure with MainLayout extending AppLayout**

`MainLayout` extends `AppLayout` and is referenced in every view via `@Route(value = "...", layout = MainLayout.class)`. It provides a persistent header, a collapsible navigation drawer, and a footer. The router outlet renders each view inside the layout without a full page reload.

**17. At least three visually distinct views**

The app has several structurally different views:
- `HomeView`: card grid layout with large hero text, no data grids
- `EmployeeView`: master-detail layout with a full-width grid on the left and a form panel on the right
- `SearchView`: filter panel at the top, results grid below, compact single-column layout
- `AdminView`: tabbed panel layout with user management table and import/export tools
- `HistoryView`: select-and-reveal layout showing a timeline for a chosen employee

All use `MainLayout` and are reachable through the navigation drawer.

**18. Header with app name, user info, logout, and DrawerToggle**

`MainLayout.createHeader()` builds a navbar containing:
- `DrawerToggle` on the left
- App name as a heading
- Language toggle button
- Logged-in username displayed as text
- Avatar with the user's profile image
- Logout button that calls `SecurityService.logout()`

**19. Navigation bar with icons and active page highlight**

The navigation drawer is built with a `VerticalLayout` of `RouterLink` elements. Each link contains a `VaadinIcon` and a label `Span`, laid out via `display: flex; align-items: center`. The `RouterLink` component receives Vaadin's `highlight` attribute automatically when its route matches the current URL; the global stylesheet uses `a.nav-link[highlight]` to apply the active background and font-weight highlight.

**20. Footer with author name, copyright, link, pinned to bottom**

The footer is a `Div` created in `MainLayout.buildFooter()`. It contains "Made by: Athena R.", a copyright year, and a link to the GitHub repository. It is pinned to the bottom of the content area via `showRouterLayoutContent()`: the wrapper is a `VerticalLayout` with `setFlexGrow(1, contentComponent)` so the page content expands to fill available space and the footer is pushed to the bottom regardless of content height.

---

### Authentication and security

**21. Spring Security with hashed passwords and three roles**

`SecurityConfig` extends `VaadinWebSecurityAdapter`. `AppUser` is the user entity with a `password` field that only ever stores a BCrypt hash (`PasswordEncoder` bean is `BCryptPasswordEncoder`). `DataInitializer` creates the default accounts on startup if they do not exist. Three roles are defined: `ROLE_USER`, `ROLE_SUPER`, `ROLE_ADMIN`.

**22. Role-based view access**

- `@PermitAll` -- `HomeView` (everyone including unauthenticated)
- `@RolesAllowed("ROLE_USER")` -- `EmployeeView`, `ProfileView`, `SearchView`, `DepartmentView`, `ProjectView`
- `@RolesAllowed({"ROLE_SUPER", "ROLE_ADMIN"})` -- `HistoryView`
- `@RolesAllowed("ROLE_ADMIN")` -- `AdminView`

**23. Registration page**

`RegisterView` is a public form (`@AnonymousAllowed`) that collects username, first name, last name, email, and password, validates them, and delegates to `AppUserService.register()`. The service hashes the password with BCrypt, assigns `ROLE_USER`, saves the new `AppUser`, and calls `EmailService.notifyAdminNewUser()` to notify the admin. After registration the user is redirected to the login page.

**24. Custom error message for insufficient access**

`AccessDeniedView` is mapped to the `access-denied` route and implements `HasErrorParameter<AccessDeniedException>`. When Vaadin's router catches an `AccessDeniedException` (thrown by `@RolesAllowed` enforcement), it renders this view automatically. The view shows a lock icon, a clear message, and navigation buttons back to Home.

**25. Profile image upload**

`ProfileView` has an `Upload` component limited to JPEG, PNG, and GIF files up to 5 MB. The uploaded file is saved to `uploads/profiles/` on the local filesystem and the path is stored as a `String profileImagePath` field on `AppUser`. The upload itself works, but the header avatar displays username initials only and does not yet read the stored path, so the image is not displayed after upload.

**26. OAuth2 login with GitHub and Google**

`SecurityConfig` configures `oauth2Login()`. `OAuth2UserSuccessHandler` receives the authenticated `OAuth2User`, looks up or creates an `AppUser` by email, and completes the Vaadin session. Client IDs and secrets are read from environment variables (`GITHUB_CLIENT_ID`, `GITHUB_CLIENT_SECRET`, `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`). Placeholder fallback values are set in `application.properties` so the app starts without the variables, but the OAuth2 login buttons will appear on the login page and fail to redirect without real app registrations on GitHub and Google.

---

### Additional features

**27. Published to GitHub**

Repository: https://github.com/athenariv/VaadinProject

**28. Vaadin Server Push**

`@Push` is set on `AppShellConfig`. `Broadcaster` holds a `CopyOnWriteArrayList` of `Consumer<String>` listeners. Views register and deregister in `onAttach`/`onDetach`. When the admin sends a broadcast from `AdminView`, every registered UI receives a `Notification` via `ui.access()` to ensure thread safety.

**29. Localisation (Finnish and English)**

`AppI18NProvider` implements `I18NProvider` and loads `translations.properties` (English) and `translations_fi.properties` (Finnish) from `resources/i18n/` using `ResourceBundle`. Views that implement `LocaleChangeObserver` call `getTranslation()` in `localeChange()` to update their labels. A language toggle button in the header switches the session locale between `fi` and `en`.

**30. Docker image and Dockerfile**

A multi-stage `Dockerfile` in the project root first builds the application with Maven, then copies only the resulting JAR into a slim JRE image. The image is tagged `companyapp`.

**31. docker-compose**

`docker-compose.yml` defines the `companyapp` service built from the local Dockerfile. The H2 database runs in file mode (`jdbc:h2:file:/data/companydb`) with the `/data` directory mounted to a named Docker volume so the database survives container restarts.

**32. Email to admin on new user registration**

`EmailService` uses Spring's `JavaMailSender`. `AppUserService.register()` calls `emailService.notifyAdminNewUser(user)` after saving the new account, which sends a plain-text email to `admin@company.fi` with the new username and email. The SMTP credentials in `application.properties` are placeholders (`${MAIL_USERNAME}`, `${MAIL_PASSWORD}`); without real credentials the send call throws an exception that is caught and logged as a warning, so the registration succeeds but no email is actually delivered.

**33. Password reset by email**

`PasswordResetView` (`/reset-password`) is a two-step form on a single page. Step 1 accepts an email address, generates a UUID token, and calls `EmailService.sendPasswordResetEmail()` to attempt delivery. Step 2 (revealed in the same page) accepts the token and a new password; on submit the token is compared against the one generated and the new BCrypt-hashed password is saved via `AppUserService`. The token is stored in a local variable, not the database, so it is only valid for the same page session -- navigating away loses the token. Email delivery also depends on SMTP being configured (see criterion 32).

**34. File upload and storage**

`ProfileView` handles binary file upload (profile image) as described in criterion 25. The file is saved to the local filesystem under `uploads/profiles/` using `Files.copy()` and the path is persisted on `AppUser`. The file is accessible on the server after upload, but it is not yet served back to the browser for display in the UI.

**35. CSV and Excel import and export**

In `AdminView`:
- Export: an "Export to Excel" button uses Apache POI (`XSSFWorkbook`) to write all employees to an `.xlsx` file and serves it as a download via `StreamRegistration`.
- Import: an `Upload` component accepts `.csv` files. The file is read with `BufferedReader`, the header row is skipped, and each subsequent line is parsed as `firstName,lastName,email,jobTitle,salary[,departmentName]`. Rows with a department name are resolved via `DepartmentService.findByName()`. Saved through `EmployeeService`.

**36. Spring Data Auditing**

`AuditConfig` is annotated `@EnableJpaAuditing` and provides an `AuditorAware<String>` bean that returns the current principal's username. `BaseEntity` (extended by all domain entities) has four auditing fields:
- `@CreatedDate LocalDateTime createdAt`
- `@LastModifiedDate LocalDateTime updatedAt`
- `@CreatedBy String createdBy`
- `@LastModifiedBy String updatedBy`

These are populated automatically by Spring Data on insert and update.

**37. Revision history with Hibernate Envers**

`Department`, `Employee`, `EmployeeDetail`, `Project`, and `Task` are annotated with `@Audited`. Hibernate Envers creates `_AUD` shadow tables and a `REVINFO` table automatically. Every insert, update, and delete creates a new revision entry.

**38. Revision history displayed in the UI**

`HistoryView` (`@RolesAllowed({"ROLE_SUPER", "ROLE_ADMIN"})`) lets the user select an employee from a combo box. On selection it calls `AuditReader.createQuery().forRevisionsOfEntity(Employee.class, ...)` to load all revisions, then renders them as a vertical list showing the revision number, timestamp, author, and a summary of the changed fields.

**39. External JavaScript component (Quill.js)**

`quill-editor.js` is a `LitElement` web component that loads Quill 1.3.7 from CDN inside its shadow root. It exposes `value` as a property and fires a `value-changed` event. The Quill Snow theme CSS is injected via a `<link>` element inside the shadow root so it applies correctly. The Java wrapper class uses `@Tag("quill-editor")` and `@JsModule("./quill-editor.js")` and is used in `ProfileView` for personal notes.

---

## Project structure

```
src/main/java/fi/company/companyapp/
    config/         AppShellConfig, AppI18NProvider, DataInitializer, AuditConfig
    entity/         Department, Employee, EmployeeDetail, Project, Task, AppUser, BaseEntity
    repository/     Spring Data + Envers repositories
    service/        EmployeeService (Criteria API), DepartmentService, EmailService,
                    AppUserService, TaskService, ProjectService
    security/       SecurityConfig, OAuth2UserSuccessHandler, SecurityService
    push/           Broadcaster
    views/          HomeView, EmployeeView, DepartmentView, ProjectView, TaskView,
                    SearchView, ProfileView, HistoryView, AdminView,
                    RegisterView, PasswordResetView, AccessDeniedView, LoginView

src/main/resources/
    i18n/               translations.properties, translations_fi.properties
    META-INF/resources/frontend/
        quill-editor.js
        themes/companyapp/
            styles.css
            views/home-view.css
```

## Notes

Email and password reset require a real SMTP server. Set `spring.mail.host`, `spring.mail.username`, and `spring.mail.password` in `application.properties` or as environment variables.

OAuth2 login requires app registrations on GitHub and Google with the callback URL `http://localhost:8080/login/oauth2/code/github` (and `/google`). Pass the four credentials as environment variables.
