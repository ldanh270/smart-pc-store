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
 *
 */
public class JPAUtil {

    // Create EntityManagerFactory
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("smart-pc-store");

    // Get EntityManagerFactory
    public static EntityManagerFactory getEMF() {
        return emf;
    }

    // Get EntityManager
    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    // Shutdown EntityManagerFactory
    public static void shutdown() {
        emf.close();
    }
}
