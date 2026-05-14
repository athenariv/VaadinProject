package fi.company.companyapp.service;

import fi.company.companyapp.entity.Employee;
import fi.company.companyapp.repository.EmployeeRepository;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.history.Revisions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public List<Employee> findAll() {
        return employeeRepository.findAllWithRelations();
    }

    public Optional<Employee> findById(Long id) {
        return employeeRepository.findById(id);
    }

    public Employee save(Employee employee) {
        return employeeRepository.save(employee);
    }

    public void delete(Employee employee) {
        employeeRepository.delete(employee);
    }

    public boolean existsByEmail(String email) {
        return employeeRepository.existsByEmail(email);
    }

    public Revisions<Integer, Employee> findRevisions(Long id) {
        return employeeRepository.findRevisions(id);
    }

    /**
     * Advanced Criteria API search.
     * Supports: text search (LIKE), date range, JOIN on department, OR conditions, AND conditions.
     * Implements: (firstName OR lastName OR email) AND department AND hireDateRange AND salary
     */
    @Transactional(readOnly = true)
    public List<Employee> advancedSearch(
            String nameOrEmail,
            Long departmentId,
            LocalDate hireDateFrom,
            LocalDate hireDateTo,
            Double minSalary,
            Double maxSalary,
            String jobTitle
    ) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
        Root<Employee> root = cq.from(Employee.class);

        // JOIN to department
        Join<Object, Object> deptJoin = root.join("department", JoinType.LEFT);

        List<Predicate> predicates = new ArrayList<>();

        // OR: name contains OR email contains (partial text search LIKE)
        if (nameOrEmail != null && !nameOrEmail.isBlank()) {
            String pattern = "%" + nameOrEmail.toLowerCase() + "%";
            Predicate firstNameLike = cb.like(cb.lower(root.get("firstName")), pattern);
            Predicate lastNameLike = cb.like(cb.lower(root.get("lastName")), pattern);
            Predicate emailLike = cb.like(cb.lower(root.get("email")), pattern);
            // OR condition
            predicates.add(cb.or(firstNameLike, lastNameLike, emailLike));
        }

        // JOIN filter: department by ID
        if (departmentId != null) {
            predicates.add(cb.equal(deptJoin.get("id"), departmentId));
        }

        // Date range search
        if (hireDateFrom != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("hireDate"), hireDateFrom));
        }
        if (hireDateTo != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("hireDate"), hireDateTo));
        }

        // Salary range
        if (minSalary != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("salary"), minSalary));
        }
        if (maxSalary != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("salary"), maxSalary));
        }

        // Job title filter (partial match) via department relation property search
        if (jobTitle != null && !jobTitle.isBlank()) {
            predicates.add(cb.like(cb.lower(root.get("jobTitle")), "%" + jobTitle.toLowerCase() + "%"));
        }

        // AND all predicates together
        if (!predicates.isEmpty()) {
            cq.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        cq.orderBy(cb.asc(root.get("lastName")));
        cq.distinct(true);

        return entityManager.createQuery(cq).getResultList();
    }

    /**
     * Complex search: (firstName OR email LIKE X) AND department = Y
     * This implements the "(X OR Y) AND Z" pattern required by the assignment
     */
    @Transactional(readOnly = true)
    public List<Employee> complexSearch(String nameOrEmail, String departmentName) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
        Root<Employee> root = cq.from(Employee.class);
        Join<Object, Object> deptJoin = root.join("department", JoinType.INNER);

        List<Predicate> andPredicates = new ArrayList<>();

        // (X OR Y): firstName LIKE OR email LIKE
        if (nameOrEmail != null && !nameOrEmail.isBlank()) {
            String pattern = "%" + nameOrEmail.toLowerCase() + "%";
            Predicate nameLike = cb.like(cb.lower(root.get("firstName")), pattern);
            Predicate emailLike = cb.like(cb.lower(root.get("email")), pattern);
            andPredicates.add(cb.or(nameLike, emailLike));
        }

        // AND Z: department name LIKE
        if (departmentName != null && !departmentName.isBlank()) {
            andPredicates.add(cb.like(
                cb.lower(deptJoin.get("name")),
                "%" + departmentName.toLowerCase() + "%"
            ));
        }

        if (!andPredicates.isEmpty()) {
            cq.where(cb.and(andPredicates.toArray(new Predicate[0])));
        }

        cq.distinct(true);
        return entityManager.createQuery(cq).getResultList();
    }
}
