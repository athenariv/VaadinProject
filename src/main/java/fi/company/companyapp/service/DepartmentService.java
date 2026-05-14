package fi.company.companyapp.service;

import fi.company.companyapp.entity.Department;
import fi.company.companyapp.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.history.Revisions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentService {
    private final DepartmentRepository departmentRepository;

    public List<Department> findAll() {
        return departmentRepository.findAllWithEmployees();
    }

    public Optional<Department> findById(Long id) {
        return departmentRepository.findById(id);
    }

    public Department save(Department department) {
        return departmentRepository.save(department);
    }

    public void delete(Department department) {
        departmentRepository.delete(department);
    }

    public Optional<Department> findByName(String name) {
        return departmentRepository.findByName(name);
    }

    public boolean existsByName(String name) {
        return departmentRepository.existsByName(name);
    }

    public Revisions<Integer, Department> findRevisions(Long id) {
        return departmentRepository.findRevisions(id);
    }
}
