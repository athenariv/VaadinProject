package fi.company.companyapp.repository;

import fi.company.companyapp.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long>,
        RevisionRepository<Department, Long, Integer> {
    Optional<Department> findByName(String name);
    boolean existsByName(String name);

    @Query("SELECT DISTINCT d FROM Department d LEFT JOIN FETCH d.employees")
    List<Department> findAllWithEmployees();
}
