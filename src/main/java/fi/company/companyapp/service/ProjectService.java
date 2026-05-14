package fi.company.companyapp.service;

import fi.company.companyapp.entity.Project;
import fi.company.companyapp.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.history.Revisions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectService {
    private final ProjectRepository projectRepository;

    public List<Project> findAll() {
        return projectRepository.findAllWithRelations();
    }

    public Optional<Project> findById(Long id) {
        return projectRepository.findById(id);
    }

    public Project save(Project project) {
        return projectRepository.save(project);
    }

    public void delete(Project project) {
        projectRepository.delete(project);
    }

    public Revisions<Integer, Project> findRevisions(Long id) {
        return projectRepository.findRevisions(id);
    }
}
