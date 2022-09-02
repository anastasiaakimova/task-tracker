package by.akimova.task.tracker.api.controllers.helpers;

import by.akimova.task.tracker.api.exceptions.NotFoundException;
import by.akimova.task.tracker.store.entities.ProjectEntity;
import by.akimova.task.tracker.store.repositories.ProjectRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

/**
 * Контроллер который помогает разделить логику между двумя независимыми контроллерами
 *
 */

@RequiredArgsConstructor
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ControllerHelper {

    ProjectRepository projectRepository;

    public ProjectEntity getProjectOrThrowException(Long projectId) {
        return projectRepository
                .findById(projectId)
                .orElseThrow(() ->
                        new NotFoundException(
                                String.format(
                                        "Project with \"%s\" doesn't exists.",
                                        projectId
                                )
                        )
                );
    }
}
