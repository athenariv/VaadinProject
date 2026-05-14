package fi.company.companyapp.repository;

import fi.company.companyapp.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long>,
        RevisionRepository<Task, Long, Integer> {
    List<Task> findByProjectId(Long projectId);
    List<Task> findByStatus(String status);
}
