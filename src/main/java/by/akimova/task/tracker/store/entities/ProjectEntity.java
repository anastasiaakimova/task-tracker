package by.akimova.task.tracker.store.entities;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Здесь не должна быть сокрыта какая-то логика или реализация создания самого класса.
 * Это просто информационная обертка о том что это просто класс с данными.
 * Это просто класс с данными, он не должен быть умным
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "project")
public class ProjectEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long id;

    @Column(unique = true)
    String name;

    @Builder.Default
    Instant createdAt = Instant.now();

    @Builder.Default
    @OneToMany
    //name - колонка которая будет сгенерирована в нашем taskStates
    @JoinColumn(name = "project_id", referencedColumnName = "id")//id - это id ссылки, на что ссылается taskEntity - название колонки в джава
    // будет создано поле project_id по которому мы будем собирать этот список
    List<TaskStateEntity> taskStates = new ArrayList<>();

}
