package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import utils.JwtUtil;

import java.util.List;

/**
 * Generic DAO interface for CRUD operations
 *
 * @param <T> the entity type
 */
public class GenericDao<T> {
    private final Class<T> entityClass;

    /**
     * Constructor
     *
     * @param entityClass the entity class
     */
    public GenericDao(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * Get the EntityManager
     *
     * @return the entity manager
     */
    public EntityManager getEntityManager() {
        return JPAUtil.getEntityManager();
    }

    /**
     * Find object/entity by id
     *
     * @param id the primary key
     * @return the entity found
     */
    public T findById(Object id) {
        return getEntityManager().find(entityClass, id);
    }


    /**
     * Find all entities
     *
     * @return list of entities
     */
    public List<T> findAll() {
        String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e";
        TypedQuery<T> query = getEntityManager().createQuery(jpql, entityClass);
        return query.getResultList();
    }

    /**
     * Create a new entity
     *
     * @param entity the entity to create
     */
    public void create(T entity) {
        getEntityManager().persist(entity);
    }

    /**
     * Update an existing entity
     *
     * @param entity the entity to update
     * @return the updated entity
     */
    public T update(T entity) {
        return getEntityManager().merge(entity);
    }

    /**
     * Delete an entity by id
     *
     * @param id the primary key of the entity to delete
     */
    public void delete(Object id) {
        T entity = getEntityManager().find(entityClass, id);
        if (entity != null) {
            getEntityManager().remove(entity);
        }
    }
}
