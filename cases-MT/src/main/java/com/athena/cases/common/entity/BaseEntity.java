package com.athena.cases.common.entity;

import java.util.Objects;

/**
 * Shared surrogate-key base for domain entities.
 *
 * <p>JPA {@code @MappedSuperclass} / {@code @Id} annotations are added in foundation Phase 1
 * when {@code spring-boot-starter-data-jpa} is on the classpath. This class establishes the
 * identity contract features will inherit.
 */
public abstract class BaseEntity {

    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BaseEntity that = (BaseEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
