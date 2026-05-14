package fi.company.companyapp.repository;

import fi.company.companyapp.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long>,
        JpaSpecificationExecutor<Employee>,
        RevisionRepository<Employee, Long, Integer> {
    Optional<Employee> findByEmail(String email);
    List<Employee> findByDepartmentId(Long departmentId);
    boolean existsByEmail(String email);

    @Query("SELECT DISTINCT e FROM Employee e LEFT JOIN FETCH e.detail LEFT JOIN FETCH e.projects")
    List<Employee> findAllWithRelations();
}
