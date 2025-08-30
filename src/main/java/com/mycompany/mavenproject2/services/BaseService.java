package com.mycompany.mavenproject2.services;

import java.sql.SQLException;
import java.util.List;

public interface BaseService<T> {
    // Default method with a default implementation
    default T getById(int id) throws SQLException {
        throw new UnsupportedOperationException("GetById method not implemented for this service");
    }

    default void create(T item) throws SQLException {
        throw new UnsupportedOperationException("Create method not implemented for this service");
    }

    default void update(T item) throws SQLException {
        throw new UnsupportedOperationException("Update method not implemented for this service");
    }

    default void delete(int id) throws SQLException {
        throw new UnsupportedOperationException("Delete method not implemented for this service");
    }

  
}