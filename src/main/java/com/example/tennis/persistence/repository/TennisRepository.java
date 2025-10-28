package com.example.tennis.persistence.repository;

import com.example.tennis.persistence.entity.BaseEntity;
import com.example.tennis.persistence.exception.NotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@Transactional
public class TennisRepository implements TennisDAO{

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public <T> Optional<T> findById(Class<T> entityClass, Object id) {
        return Optional.ofNullable(((T) entityManager.find(entityClass, id)));
    }

    @Override
    public <T> T getById(Class<T> entityClass, Object id){
        var entity = findById(entityClass, id);
        return entity.orElseThrow(() ->
                new NotFoundException(entityClass + " not found - id:"+ id));
    }

    @Override
    public <T> Optional<T> findByProperty(Class<T> entityClass, String propertyName, Object value) {
        try {
            return Optional.of(getByProperty(entityClass,propertyName, value));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public <T> T getByProperty(Class<T> entityClass, String propertyName, Object value){
        var replacement = propertyName.contains(".") ? propertyName.substring(
                propertyName.lastIndexOf(".")+1) : propertyName;
        String query = "FROM " + entityClass.getSimpleName() + " WHERE " +
                propertyName + " = " + ":" + replacement;
        try {
            return entityManager.createQuery(query, entityClass)
                    .setParameter(replacement,value).getSingleResult();
        }
        catch (NoResultException | IllegalArgumentException ex){
            log.debug(ex.getMessage());
            throw new NotFoundException(ex.getMessage());
        }
    }

    @Override
    public <T extends BaseEntity> void save(T entity){
        if (entity.getId() == null){
            entityManager.persist(entity);
        }
        else {
            entityManager.merge(entity);
        }
    }

    @Override
    public <T extends BaseEntity> void delete(T entity){
        var merged = entityManager.merge(entity);
        entityManager.remove(merged);
    }

    @Transactional(readOnly = true)
    @Override
    public <T> List<T> getAll(Class<T> entity){
        return entityManager.createQuery(
                        "Select t from " + entity.getSimpleName() + " t", entity)
                .getResultList();
    }
}
