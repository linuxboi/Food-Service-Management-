package com.mycompany.mavenproject2.models;

import java.util.Objects;

public abstract class BaseEntity {
    protected int id;

    /**
     * Gets the unique identifier of the entity
     * @return the ID of the entity
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the entity
     * @param id the ID to set
     */
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity that = (BaseEntity) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Provides a default toString implementation
     * @return a string representation of the entity
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id=" + id + '}';
    }
}