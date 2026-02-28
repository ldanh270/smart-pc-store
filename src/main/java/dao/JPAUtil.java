/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Config for JPA
 */
public class JPAUtil {

    // Create EntityManagerFactory
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("smart-pc-store");

    // ThreadLocal to store EntityManager for each thread, ensuring thread-safety
    private static final ThreadLocal<EntityManager> threadLocal = new ThreadLocal<>();

    /**
     * Get EntityManagerFactory instance.
     * Should be used for application-wide operations like schema generation, not for per-request operations.
     *
     * @return EntityManagerFactory instance
     */
    public static EntityManagerFactory getEMF() {
        return emf;
    }

    /**
     * Get EntityManager for current thread.
     * If it doesn't exist or is closed, create a new one and set it to ThreadLocal.
     *
     * @return EntityManager instance for current thread
     */
    public static EntityManager getEntityManager() {
        EntityManager em = threadLocal.get();
        // If EM not exists for current thread or is closed, create a new one and set to ThreadLocal
        if (em == null || !em.isOpen()) {
            em = emf.createEntityManager();
            threadLocal.set(em);
        }
        return em; // Return EM from ThreadLocal, ensuring thread-safety
    }

    /**
     * Close EntityManager for current thread and remove it from ThreadLocal to prevent memory leaks.
     */
    public static void closeEntityManager() {
        EntityManager em = threadLocal.get();
        if (em != null) {
            em.close();
            threadLocal.remove(); // Remove EM from ThreadLocal after closing to prevent memory leaks
        }
    }

    /**
     * Close EntityManagerFactory when application shuts down to release resources.
     * Should be called in a shutdown hook or context listener.
     */
    public static void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
