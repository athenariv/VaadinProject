package fi.company.companyapp.entity;

import javax.persistence.*;
import javax.validation.constraints.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.util.List;

/**
 * Entity 1: Department
 * Has 1:N relation to Employee
 */
@Entity
@Table(name = "departments")
@Audited
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString(exclude = "employees")
public class Department extends BaseEntity {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be 2-100 characters")
    @Column(nullable = false, unique = true)
    private String name;

    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description max 500 chars")
    private String description;

    @NotBlank(message = "Location is required")
    @Size(max = 100)
    private String location;

    @NotNull(message = "Budget is required")
    @Min(value = 0, message = "Budget must be non-negative")
    @Max(value = 99999999, message = "Budget too large")
    private Double budget;

    @NotBlank(message = "Cost center is required")
    @Pattern(regexp = "CC-\\d{4}", message = "Cost center must match CC-XXXX format")
    private String costCenter;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Employee> employees;
}
