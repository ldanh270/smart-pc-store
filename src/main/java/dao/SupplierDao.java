package dao;

import entities.Supplier;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

/**
 * Data Access Object (DAO) for Supplier entity.
 * Provides supplier-specific queries in addition to generic CRUD methods.
 */
public class SupplierDao extends GenericDao<Supplier> {

    /**
     * Constructor.
     *
     * @param em JPA EntityManager.
     */
    public SupplierDao(EntityManager em) {
        super(Supplier.class, em);
    }

    /**
     * Search suppliers by supplier name (case-insensitive partial match).
     *
     * @param keyword Search keyword.
     * @return Matching supplier list.
     */
    public List<Supplier> searchByName(String keyword) {
        String jpql = "SELECT s FROM Supplier s WHERE s.status = true AND LOWER(s.supplierName) LIKE :kw";
        return em.createQuery(jpql, Supplier.class)
                .setParameter("kw", "%" + keyword.toLowerCase() + "%")
                .getResultList();
    }

    public List<Supplier> findAllActive() {
        String jpql = "SELECT s FROM Supplier s WHERE s.status = true";
        TypedQuery<Supplier> query = em.createQuery(jpql, Supplier.class);
        return query.getResultList();
    }
}
