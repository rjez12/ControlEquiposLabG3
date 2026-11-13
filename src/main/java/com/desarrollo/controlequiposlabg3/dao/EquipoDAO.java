package com.desarrollo.controlequiposlabg3.dao;

import com.desarrollo.controlequiposlabg3.database.JPAUtil;
import com.desarrollo.controlequiposlabg3.model.Equipo;
import com.desarrollo.controlequiposlabg3.model.TipoEquipo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Locale;

public class EquipoDAO {

    private static TipoEquipo obtenerTipoEquipo(
            EntityManager em, TipoEquipo tipoEquipo) {

        if (tipoEquipo == null || tipoEquipo.getId() == null) {
            throw new RuntimeException(
                    "Debe seleccionar un tipo de equipo."
            );
        }

        Long tipoEquipoId = tipoEquipo.getId();

        TipoEquipo tipoEquipoBD = em.find(
                TipoEquipo.class, tipoEquipoId
        );

        if (tipoEquipoBD == null) {
            throw new RuntimeException(
                    "El tipo de equipo seleccionado no existe."
            );
        }

        return tipoEquipoBD;
    }

    public static void guardar(Equipo equipo) {
        EntityManager em = JPAUtil.getEntityManagerFactory()
                .createEntityManager();

        EntityTransaction tx = em.getTransaction();

        try {
            // Iniciar la transacción
            tx.begin();

            TipoEquipo tipoEquipo = obtenerTipoEquipo(
                    em, equipo.getTipoEquipo()
            );

            equipo.setTipoEquipo(tipoEquipo);

            // Registrar el equipo mediante Hibernate
            em.persist(equipo);

            // Confirmar los cambios en PostgreSQL
            tx.commit();

        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }

            throw ex;

        } finally {
            em.close();
        }
    }

    public List<Equipo> listar() {
        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.createQuery(
                    "SELECT e FROM Equipo e "
                            + "JOIN FETCH e.tipoEquipo "
                            + "ORDER BY e.id",
                    Equipo.class
            ).getResultList();

        } finally {
            em.close();
        }
    }

    public boolean modificar(Equipo equipo) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Equipo equipoBD = em.find(
                    Equipo.class, equipo.getId()
            );

            if (equipoBD == null) {
                tx.rollback();
                return false;
            }

            TipoEquipo tipoEquipo = obtenerTipoEquipo(
                    em, equipo.getTipoEquipo()
            );

            equipoBD.setTipoEquipo(tipoEquipo);
            equipoBD.setCodigo(equipo.getCodigo());
            equipoBD.setNombre(equipo.getNombre());
            equipoBD.setMarca(equipo.getMarca());
            equipoBD.setEstado(equipo.getEstado());
            equipoBD.setCantidad(equipo.getCantidad());

            // Hibernate detecta los cambios en la entidad administrada
            tx.commit();

            return true;

        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }

            throw ex;

        } finally {
            em.close();
        }
    }

    public static boolean eliminar(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Equipo equipoBD = em.find(Equipo.class, id);

            if (equipoBD == null) {
                tx.rollback();
                return false;
            }

            em.remove(equipoBD);

            tx.commit();

            return true;

        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }

            throw ex;

        } finally {
            em.close();
        }
    }

    public List<Equipo> buscar(String nombre, TipoEquipo tipoEquipo) {
        EntityManager em = JPAUtil.getEntityManager();

        try {
            String jpql = "SELECT e FROM Equipo e "
                    + "JOIN FETCH e.tipoEquipo "
                    + "WHERE LOWER(e.nombre) LIKE :nombre";

            if (tipoEquipo != null) {
                jpql += " AND e.tipoEquipo.id = :idTipo";
            }

            jpql += " ORDER BY e.id";

            TypedQuery<Equipo> consulta = em.createQuery(
                    jpql, Equipo.class
            );

            String texto = nombre == null ? "" : nombre.trim();

            consulta.setParameter(
                    "nombre",
                    "%" + texto.toLowerCase(Locale.ROOT) + "%"
            );

            if (tipoEquipo != null) {
                consulta.setParameter(
                        "idTipo", tipoEquipo.getId()
                );
            }

            return consulta.getResultList();

        } finally {
            em.close();
        }
    }
}