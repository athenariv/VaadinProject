package fi.company.companyapp.entity;

import javax.persistence.*;
import javax.validation.constraints.*;
import lombok.*;
import org.hibernate.envers.Audited;

/**
 * Entity 3: EmployeeDetail - 1:1 relation to Employee
 */
@Entity
@Table(name = "employee_details")
@Audited
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString(exclude = "employee")
public class EmployeeDetail extends BaseEntity {

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "\\+?[0-9\\-\\s]{7,20}", message = "Invalid phone number")
    private String phone;

    @NotBlank(message = "Address is required")
    @Size(max = 200)
    private String address;

    @NotBlank(message = "City is required")
    @Size(max = 100)
    private String city;

    @NotBlank(message = "Country is required")
    @Size(max = 100)
    private String country;

    @NotBlank(message = "Emergency contact is required")
    @Size(max = 100)
    private String emergencyContact;

    @NotBlank(message = "Emergency phone is required")
    @Pattern(regexp = "\\+?[0-9\\-\\s]{7,20}", message = "Invalid emergency phone")
    private String emergencyPhone;

    @Size(max = 500)
    private String notes;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
}
