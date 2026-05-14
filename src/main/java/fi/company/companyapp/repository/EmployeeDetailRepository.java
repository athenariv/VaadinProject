package fi.company.companyapp.repository;

import fi.company.companyapp.entity.EmployeeDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;

import java.util.Optional;

public interface EmployeeDetailRepository extends JpaRepository<EmployeeDetail, Long>,
        RevisionRepository<EmployeeDetail, Long, Integer> {
    Optional<EmployeeDetail> findByEmployeeId(Long employeeId);
}
