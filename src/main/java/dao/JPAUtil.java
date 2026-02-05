/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 *
 * @author ducan
 */
public class JPAUtil {

    private static final EntityManagerFactory emf
            = Persistence.createEntityManagerFactory("smart-pc-store");

    public static EntityManagerFactory getEMF() {
        return emf;
    }

    public static void shutdown() {
        emf.close();
    }
}
