package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

/**
 * Generic DAO interface for CRUD operations
 *
 * @param <T> the entity type
 */
public class GenericDao<T> {
    private final Class<T> entityClass;
    protected EntityManager em;

    /**
     * Constructor
     *
     * @param entityClass the entity class
     * @param em          the entity manager
     */
    public GenericDao(Class<T> entityClass, EntityManager em) {
        this.entityClass = entityClass;
        this.em = em;
    }

    /**
     * Get the EntityManager
     *
     * @return the entity manager
     */
    public EntityManager getEntityManager() {
        return em;
    }

    /**
     * Find object/entity by id
     *
     * @param id the primary key
     * @return the entity found
     */
    public T findById(Object id) {
        return em.find(entityClass, id);
    }


    /**
     * Find all entities
     *
     * @return list of entities
     */
    public List<T> findAll() {
        String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e";
        TypedQuery<T> query = em.createQuery(jpql, entityClass);
        return query.getResultList();
    }

    /**
     * Create a new entity
     *
     * @param entity the entity to create
     */
    public void create(T entity) {
        em.persist(entity);
    }

    /**
     * Update an existing entity
     *
     * @param entity the entity to update
     * @return the updated entity
     */
    public T update(T entity) {
        return em.merge(entity);
    }

    /**
     * Delete an entity by id
     *
     * @param id the primary key of the entity to delete
     */
    public void delete(Object id) {
        T entity = em.find(entityClass, id);
        if (entity != null) {
            em.remove(entity);
        }
    }
}
