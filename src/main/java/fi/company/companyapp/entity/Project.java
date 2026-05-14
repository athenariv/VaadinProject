package fi.company.companyapp.entity;

import javax.persistence.*;
import javax.validation.constraints.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Entity 4: Project - M:N relation to Employee, 1:N to Task
 */
@Entity
@Table(name = "projects")
@Audited
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString(exclude = {"employees", "tasks"})
public class Project extends BaseEntity {

    @NotBlank(message = "Project name is required")
    @Size(min = 2, max = 150, message = "Name must be 2-150 characters")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Description is required")
    @Size(max = 1000)
    private String description;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDate endDate;

    @NotNull(message = "Budget is required")
    @Min(value = 0)
    private Double budget;

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "PLANNING|ACTIVE|ON_HOLD|COMPLETED|CANCELLED", message = "Invalid status")
    private String status;

    @ManyToMany(mappedBy = "projects", fetch = FetchType.LAZY)
    private Set<Employee> employees;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Task> tasks;
}
