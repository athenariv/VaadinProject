package fi.company.companyapp.entity;

import javax.persistence.*;
import javax.validation.constraints.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.util.Set;

/**
 * Entity 2: Employee
 * - 1:N relation to Department (many employees belong to one department)
 * - 1:1 relation to EmployeeDetail
 * - M:N relation to Project
 */
@Entity
@Table(name = "employees")
@Audited
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString(exclude = {"department", "detail", "projects"})
public class Employee extends BaseEntity {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be 2-50 characters")
    @Column(nullable = false)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be 2-50 characters")
    @Column(nullable = false)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email")
    @Column(nullable = false, unique = true)
    private String email;

    @NotNull(message = "Hire date is required")
    @PastOrPresent(message = "Hire date cannot be in the future")
    private LocalDate hireDate;

    @NotNull(message = "Salary is required")
    @Min(value = 1000, message = "Salary must be at least 1000")
    @Max(value = 500000, message = "Salary too large")
    private Double salary;

    @NotBlank(message = "Job title is required")
    @Size(max = 100)
    private String jobTitle;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    private Department department;

    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private EmployeeDetail detail;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "employee_project",
        joinColumns = @JoinColumn(name = "employee_id"),
        inverseJoinColumns = @JoinColumn(name = "project_id")
    )
    private Set<Project> projects;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
