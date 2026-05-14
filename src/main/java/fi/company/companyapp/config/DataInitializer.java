package fi.company.companyapp.config;

import fi.company.companyapp.entity.AppUser;
import fi.company.companyapp.repository.AppUserRepository;
import fi.company.companyapp.entity.*;
import fi.company.companyapp.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeDetailRepository employeeDetailRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    @Override
    public void run(String... args) {
        initUsers();
        initData();
    }

    private void initUsers() {
        if (appUserRepository.count() > 0) return;

        AppUser admin = new AppUser();
        admin.setUsername("admin");
        admin.setEmail("admin@company.fi");
        admin.setPasswordHash(passwordEncoder.encode("admin123"));
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setRoles(Set.of("ROLE_ADMIN", "ROLE_SUPER", "ROLE_USER"));
        admin.setEnabled(true);
        appUserRepository.save(admin);

        AppUser superUser = new AppUser();
        superUser.setUsername("super");
        superUser.setEmail("super@company.fi");
        superUser.setPasswordHash(passwordEncoder.encode("super123"));
        superUser.setFirstName("Super");
        superUser.setLastName("User");
        superUser.setRoles(Set.of("ROLE_SUPER", "ROLE_USER"));
        superUser.setEnabled(true);
        appUserRepository.save(superUser);

        AppUser normalUser = new AppUser();
        normalUser.setUsername("user");
        normalUser.setEmail("user@company.fi");
        normalUser.setPasswordHash(passwordEncoder.encode("user123"));
        normalUser.setFirstName("Normal");
        normalUser.setLastName("User");
        normalUser.setRoles(Set.of("ROLE_USER"));
        normalUser.setEnabled(true);
        appUserRepository.save(normalUser);

        log.info("Created default users: admin/admin123, super/super123, user/user123");
    }

    private void initData() {
        if (departmentRepository.count() > 0) return;

        // Departments
        Department it = new Department();
        it.setName("IT Department");
        it.setDescription("Information Technology");
        it.setLocation("Helsinki");
        it.setBudget(500000.0);
        it.setCostCenter("CC-0001");
        departmentRepository.save(it);

        Department hr = new Department();
        hr.setName("HR Department");
        hr.setDescription("Human Resources");
        hr.setLocation("Espoo");
        hr.setBudget(200000.0);
        hr.setCostCenter("CC-0002");
        departmentRepository.save(hr);

        Department finance = new Department();
        finance.setName("Finance Department");
        finance.setDescription("Financial Management");
        finance.setLocation("Tampere");
        finance.setBudget(300000.0);
        finance.setCostCenter("CC-0003");
        departmentRepository.save(finance);

        // Employees
        Employee emp1 = new Employee();
        emp1.setFirstName("Mikko");
        emp1.setLastName("Virtanen");
        emp1.setEmail("mikko.virtanen@company.fi");
        emp1.setHireDate(LocalDate.of(2020, 1, 15));
        emp1.setSalary(75000.0);
        emp1.setJobTitle("Software Engineer");
        emp1.setDepartment(it);
        employeeRepository.save(emp1);

        EmployeeDetail detail1 = new EmployeeDetail();
        detail1.setPhone("+358 40 1234567");
        detail1.setAddress("Mannerheimintie 1");
        detail1.setCity("Helsinki");
        detail1.setCountry("Finland");
        detail1.setEmergencyContact("Liisa Virtanen");
        detail1.setEmergencyPhone("+358 40 9876543");
        detail1.setNotes("Senior developer");
        detail1.setEmployee(emp1);
        employeeDetailRepository.save(detail1);

        Employee emp2 = new Employee();
        emp2.setFirstName("Anna");
        emp2.setLastName("Korhonen");
        emp2.setEmail("anna.korhonen@company.fi");
        emp2.setHireDate(LocalDate.of(2021, 3, 1));
        emp2.setSalary(65000.0);
        emp2.setJobTitle("HR Manager");
        emp2.setDepartment(hr);
        employeeRepository.save(emp2);

        EmployeeDetail detail2 = new EmployeeDetail();
        detail2.setPhone("+358 50 2345678");
        detail2.setAddress("Espoo Central 5");
        detail2.setCity("Espoo");
        detail2.setCountry("Finland");
        detail2.setEmergencyContact("Juhani Korhonen");
        detail2.setEmergencyPhone("+358 50 8765432");
        detail2.setEmployee(emp2);
        employeeDetailRepository.save(detail2);

        // Projects
        Project proj1 = new Project();
        proj1.setName("Digital Transformation");
        proj1.setDescription("Company-wide digital transformation project");
        proj1.setStartDate(LocalDate.now());
        proj1.setEndDate(LocalDate.now().plusYears(1));
        proj1.setBudget(1000000.0);
        proj1.setStatus("ACTIVE");
        proj1.setEmployees(Set.of(emp1, emp2));
        projectRepository.save(proj1);

        Project proj2 = new Project();
        proj2.setName("HR Portal");
        proj2.setDescription("New HR self-service portal");
        proj2.setStartDate(LocalDate.now());
        proj2.setEndDate(LocalDate.now().plusMonths(6));
        proj2.setBudget(150000.0);
        proj2.setStatus("PLANNING");
        proj2.setEmployees(Set.of(emp2));
        projectRepository.save(proj2);

        // Update employee project references
        emp1.setProjects(Set.of(proj1));
        employeeRepository.save(emp1);
        emp2.setProjects(Set.of(proj1, proj2));
        employeeRepository.save(emp2);

        // Tasks
        Task task1 = new Task();
        task1.setTitle("Requirements Analysis");
        task1.setDescription("Analyze requirements for digital transformation");
        task1.setDueDate(LocalDate.now().plusMonths(1));
        task1.setPriority("HIGH");
        task1.setStatus("IN_PROGRESS");
        task1.setCompletionPercentage(40);
        task1.setEstimatedHours(80.0);
        task1.setProject(proj1);
        taskRepository.save(task1);

        Task task2 = new Task();
        task2.setTitle("System Architecture Design");
        task2.setDescription("Design the new system architecture");
        task2.setDueDate(LocalDate.now().plusMonths(2));
        task2.setPriority("CRITICAL");
        task2.setStatus("TODO");
        task2.setCompletionPercentage(0);
        task2.setEstimatedHours(120.0);
        task2.setProject(proj1);
        taskRepository.save(task2);

        log.info("Sample data initialized");
    }
}
