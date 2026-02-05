/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

/**
 * Include DAO functions for access to database
 *
 * @author ducan
 */
public class GenericDAO {

    /**
     * Find records by IDs
     *
     * @param entityClass Proper type
     * @param id          Object id to find
     * @return Result object
     */
    public <T, ID> T findById(Class<T> entityClass, ID id) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return em.find(entityClass, id);
        }
    }

    /**
     * Find all records
     *
     * @param entityClass Proper type
     * @return List of records
     */
    public <T> List<T> findAll(Class<T> entityClass) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e";
            return em.createQuery(jpql, entityClass).getResultList();
        }
    }

    /**
     * Save entity
     *
     * @param entity Entity to save
     * @return Saved entity
     */
    public <T> T save(T entity) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(entity);
            tx.commit();
            return entity;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Update entity
     *
     * @param entity Entity to update
     * @return Updated entity
     */
    public <T> T update(T entity) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T merged = em.merge(entity);
            tx.commit();
            return merged;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Delete entity by ID
     *
     * @param entityClass Proper type
     * @param id          Object id to delete
     */
    public <T, ID> void deleteById(Class<T> entityClass, ID id) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T entity = em.find(entityClass, id);
            if (entity != null) {
                em.remove(entity);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
