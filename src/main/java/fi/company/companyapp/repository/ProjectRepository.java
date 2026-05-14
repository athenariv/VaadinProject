package fi.company.companyapp.repository;

import fi.company.companyapp.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long>,
        JpaSpecificationExecutor<Project>,
        RevisionRepository<Project, Long, Integer> {
    List<Project> findByStatus(String status);

    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.employees LEFT JOIN FETCH p.tasks")
    List<Project> findAllWithRelations();
}
