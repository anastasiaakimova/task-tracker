package by.akimova.task.tracker.api.controllers;

import by.akimova.task.tracker.api.controllers.helpers.ControllerHelper;
import by.akimova.task.tracker.api.dto.AckDto;
import by.akimova.task.tracker.api.dto.ProjectDto;
import by.akimova.task.tracker.api.exceptions.BadRequestException;
import by.akimova.task.tracker.api.factories.ProjectDtoFactory;
import by.akimova.task.tracker.store.entities.ProjectEntity;
import by.akimova.task.tracker.store.repositories.ProjectRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Transactional на уровне класса потому что сейчас сильно не важно момент с транзакционностью
 */
@RequiredArgsConstructor // автоматически все финальные поля (бины) запихивает в конструктор
@FieldDefaults(level = AccessLevel.PRIVATE,
        makeFinal = true) // таким образом заинжектили все бины
@Transactional
@RestController
public class ProjectController {

    ProjectDtoFactory projectDtoFactory;
    ProjectRepository projectRepository;
    ControllerHelper controllerHelper;

    public static final String FETCH_PROJECT = "/api/projects";
    public static final String CREATE_PROJECT = "/api/projects";
    public static final String EDIT_PROJECT = "/api/projects/{project_id}";
    public static final String DELETE_PROJECT = "/api/projects/{project_id}";

    public static final String CREATE_OR_UPDATE_PROJECT = "/api/projects";

    @GetMapping(FETCH_PROJECT)
    public List<ProjectDto> fetchProject(@RequestParam(value = "prefix_name", required = false) Optional<String> optionalPrefixName) {

        // если пустой, то отфильтруется значение и параметр станет пустым внутри
        // если не пустой, то имеем дело с тем же опшиналом с которым имели
        optionalPrefixName = optionalPrefixName.filter(prefixName -> !prefixName.trim().isEmpty());

        Stream<ProjectEntity> projectStream = optionalPrefixName
                .map(projectRepository::streamAllByNameStartsWithIgnoreCase)
                .orElseGet(projectRepository::streamAllBy);

        if (optionalPrefixName.isPresent()) {
            projectStream = projectRepository.streamAllByNameStartsWithIgnoreCase(optionalPrefixName.get());
        } else {
            projectStream = projectRepository.streamAllBy();
        }

        return projectStream
                .map(projectDtoFactory::makeProjectDto)
                .collect(Collectors.toList());
    }

    @PostMapping(CREATE_PROJECT)
    public ProjectDto createProject(@RequestParam("project_name") String projectName) {
        if (projectName.trim().isEmpty()) {
            throw new BadRequestException("Name can't be empty.");
        }

        projectRepository
                .findByName(projectName)
                .ifPresent(project -> {
                    throw new BadRequestException(String.format("Project \"%s\" already exists.", projectName));
                });
        ProjectEntity project = projectRepository.saveAndFlush(
                ProjectEntity.builder()
                        .name(projectName)
                        .build()
        );

        return projectDtoFactory.makeProjectDto(project);

    }

    @PatchMapping(EDIT_PROJECT)
    public ProjectDto editProject(
            @PathVariable("projectId") Long projectId,
            @RequestParam("project_name") String projectName) {

        if (projectName.trim().isEmpty()) {
            throw new BadRequestException("Name can't be empty.");
        }

        ProjectEntity project = controllerHelper.getProjectOrThrowException(projectId);

        projectRepository
                .findByName(projectName)
                .filter(anotherProject -> !Objects.equals(anotherProject.getId(), projectId))
                .ifPresent(anotherProject -> {
                    throw new BadRequestException(String.format("Project \"%s\" already exists.", projectName));
                });

        project.setName(projectName);
        project = projectRepository.saveAndFlush(project);

        return projectDtoFactory.makeProjectDto(project);

    }

    @DeleteMapping(DELETE_PROJECT)
    public AckDto deleteProject(@PathVariable("project_id") Long projectId) {

        controllerHelper.getProjectOrThrowException(projectId);

        projectRepository.deleteById(projectId);
        return AckDto.makeDefault(true);
    }



    /**
     * Метод который позволяет создать или обновить сущность.
     * Может использоваться вместо двух отдельных методов
     */
    @PutMapping(CREATE_OR_UPDATE_PROJECT)
    public ProjectDto createOrUpdateProject(
            @RequestParam(value = "project_id", required = false) Optional<Long> optionalProjectId,
            @RequestParam(value = "project_name", required = false) Optional<String> optionalProjectName
            //Another params...
    ) {

        optionalProjectName = optionalProjectName.filter(projectName -> !projectName.trim().isEmpty());

        boolean isCreate = !optionalProjectId.isPresent();

        if (isCreate && !optionalProjectName.isPresent()) {
            throw new BadRequestException("Project name can't be empty");
        }

       final ProjectEntity project = optionalProjectId
                .map(controllerHelper::getProjectOrThrowException)
                .orElseGet(() -> ProjectEntity.builder().build());

        optionalProjectName.ifPresent(projectName -> {

            projectRepository
                    .findByName(projectName)
                    .filter(anotherProject -> !Objects.equals(anotherProject.getId(), project.getId()))
                    .ifPresent(anotherProject -> {
                        throw new BadRequestException(
                                String.format("Project \"%s\" already exists.", projectName)
                        );
                    });
            project.setName(projectName);
        });

        final ProjectEntity savedProject = projectRepository.saveAndFlush(project);

        return projectDtoFactory.makeProjectDto(savedProject);

    }

}
