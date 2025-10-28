package com.example.tennis.persistence.repository;

import com.example.tennis.persistence.entity.BaseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface TennisDAO {
    <T> Optional<T> findById(Class<T> entityClass, Object id);

    <T> T getById(Class<T> entityClass, Object id);

    <T> Optional<T> findByProperty(Class<T> entityClass, String propertyName, Object value);

    <T> T getByProperty(Class<T> entityClass, String propertyName, Object value);

    <T extends BaseEntity> void save(T entity);

    <T extends BaseEntity> void delete(T entity);

    @Transactional(readOnly = true)
    <T> List<T> getAll(Class<T> entity);
}
