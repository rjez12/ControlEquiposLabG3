package com.desarrollo.controlequiposlabg3.dao;

import com.desarrollo.controlequiposlabg3.database.JPAUtil;
import com.desarrollo.controlequiposlabg3.model.TipoEquipo;
import jakarta.persistence.EntityManager;

import java.util.List;

public class TipoEquipoDAO {

    public List<TipoEquipo> listar() {
        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.createQuery(
                    "SELECT t FROM TipoEquipo t ORDER BY t.nombre",
                    TipoEquipo.class
            ).getResultList();

        } finally {
            em.close();
        }
    }
}