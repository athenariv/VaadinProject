package fi.company.companyapp.entity;

import javax.persistence.*;
import javax.validation.constraints.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.time.LocalDate;

/**
 * Entity 5: Task - 1:N relation to Project
 */
@Entity
@Table(name = "tasks")
@Audited
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString(exclude = "project")
public class Task extends BaseEntity {

    @NotBlank(message = "Title is required")
    @Size(min = 2, max = 200, message = "Title must be 2-200 characters")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 1000)
    private String description;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    @NotBlank(message = "Priority is required")
    @Pattern(regexp = "LOW|MEDIUM|HIGH|CRITICAL", message = "Invalid priority")
    private String priority;

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "TODO|IN_PROGRESS|REVIEW|DONE", message = "Invalid status")
    private String status;

    @Min(value = 0) @Max(value = 100)
    private Integer completionPercentage;

    @NotNull(message = "Estimated hours is required")
    @Min(value = 1)
    @Max(value = 10000)
    private Double estimatedHours;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
}
