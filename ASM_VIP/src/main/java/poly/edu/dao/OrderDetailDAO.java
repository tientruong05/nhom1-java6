package poly.edu.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;
import poly.edu.entity.OrderDetailEntity;

import java.util.List;
import java.util.Optional;

public interface OrderDetailDAO {
    Optional<OrderDetailEntity> findById(int id);
    List<OrderDetailEntity> findAll();
    void save(OrderDetailEntity orderDetail);
    void update(OrderDetailEntity orderDetail);
    void delete(int id);
}

@Transactional
@Repository
class OrderDetailDAOImpl implements OrderDetailDAO {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<OrderDetailEntity> findById(int id) {
        return Optional.ofNullable(entityManager.find(OrderDetailEntity.class, id));
    }

    @Override
    public List<OrderDetailEntity> findAll() {
        return entityManager.createQuery("SELECT od FROM OrderDetailEntity od", OrderDetailEntity.class)
                .getResultStream()
                .toList();
    }

    @Override
    public void save(OrderDetailEntity orderDetail) {
        entityManager.persist(orderDetail);
    }

    @Override
    public void update(OrderDetailEntity orderDetail) {
        entityManager.merge(orderDetail);
    }

    @Override
    public void delete(int id) {
        Optional.ofNullable(entityManager.find(OrderDetailEntity.class, id))
                .ifPresent(entityManager::remove);
    }
}