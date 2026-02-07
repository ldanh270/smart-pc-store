package dao;

import java.util.List;

/**
 * Generic DAO interface for CRUD operations
 *
 * @param <T> the entity type
 */
public interface IGenericDAO<T> {

    /**
     * Find object/entity by id
     *
     * @param id the primary key
     * @return the entity found
     */
    T findById(Object id);

    /**
     * Find all entities
     *
     * @return list of entities
     */
    List<T> findAll();

    /**
     * Create a new entity
     *
     * @param entity the entity to create
     */
    void create(T entity);

    /**
     * Update an existing entity
     *
     * @param entity the entity to update
     * @return the updated entity
     */
    T update(T entity);

    /**
     * Delete an entity by id
     *
     * @param id the primary key of the entity to delete
     */
    void delete(Object id);
}
