package by.akimova.task.tracker.api.factories;

import by.akimova.task.tracker.api.dto.ProjectDto;
import by.akimova.task.tracker.store.entities.ProjectEntity;
import org.springframework.stereotype.Component;

/**
 * Не реализует шаблон фактори в том понимании котором должен.
 * Это просто информационная прослойка, что он генерирует классы дто конкретного типа
 * По факту больше не фактори а конвертер
 * Псевдофабрика
 */
@Component
public class ProjectDtoFactory {

    public ProjectDto makeProjectDto(ProjectEntity entity) {
        return ProjectDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
