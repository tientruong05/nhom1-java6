package poly.edu.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;
import poly.edu.entity.OrderEntity;

import java.util.List;
import java.util.Optional;

public interface OrderDAO {
    Optional<OrderEntity> findById(int id);
    List<OrderEntity> findAll();
    void save(OrderEntity order);
    void update(OrderEntity order);
    void delete(int id);
    List<Object[]> findTop10VipCustomers(Integer year, Integer month);
    List<Integer> findDistinctYears();
}

@Transactional
@Repository
class OrderDAOImpl implements OrderDAO {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<OrderEntity> findById(int id) {
        return Optional.ofNullable(entityManager.find(OrderEntity.class, id));
    }

    @Override
    public List<OrderEntity> findAll() {
        return entityManager.createQuery("SELECT o FROM OrderEntity o", OrderEntity.class)
                .getResultStream()
                .toList();
    }

    @Override
    public void save(OrderEntity order) {
        entityManager.persist(order);
    }

    @Override
    public void update(OrderEntity order) {
        entityManager.merge(order);
    }

    @Override
    public void delete(int id) {
        Optional.ofNullable(entityManager.find(OrderEntity.class, id))
                .ifPresent(entityManager::remove);
    }

    @Override
    public List<Object[]> findTop10VipCustomers(Integer year, Integer month) {
        String jpql = "SELECT o.user.fullName, SUM(o.totalPrice), MIN(o.orderDate), MAX(o.orderDate) " +
                      "FROM OrderEntity o " +
                      "WHERE (:year IS NULL OR YEAR(o.orderDate) = :year) " +
                      "AND (:month IS NULL OR MONTH(o.orderDate) = :month) " +
                      "GROUP BY o.user " +
                      "ORDER BY SUM(o.totalPrice) DESC";
        return entityManager.createQuery(jpql, Object[].class)
                .setParameter("year", year)
                .setParameter("month", month)
                .setMaxResults(10)
                .getResultStream()
                .toList();
    }

    @Override
    public List<Integer> findDistinctYears() {
        return entityManager.createQuery("SELECT DISTINCT YEAR(o.orderDate) FROM OrderEntity o ORDER BY YEAR(o.orderDate) DESC", Integer.class)
                .getResultStream()
                .toList();
    }
}