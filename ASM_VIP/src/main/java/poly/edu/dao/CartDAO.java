package poly.edu.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;
import poly.edu.entity.CartEntity;

import java.util.List;
import java.util.Optional;

public interface CartDAO {
    Optional<CartEntity> findById(int id);
    List<CartEntity> findAll();
    List<CartEntity> findByUserId(int userId);
    Optional<CartEntity> findByUserIdAndProductId(int userId, int productId);
    void save(CartEntity cart);
    void update(CartEntity cart);
    void delete(int id);
}

@Transactional
@Repository
class CartDAOImpl implements CartDAO {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<CartEntity> findById(int id) {
        return Optional.ofNullable(entityManager.find(CartEntity.class, id));
    }

    @Override
    public List<CartEntity> findAll() {
        return entityManager.createQuery("SELECT c FROM CartEntity c", CartEntity.class)
                .getResultStream()
                .toList(); // Java 16+, dùng .collect(Collectors.toList()) nếu Java 8
    }

    @Override
    public List<CartEntity> findByUserId(int userId) {
        return entityManager.createQuery("SELECT c FROM CartEntity c WHERE c.user.id = :userId", CartEntity.class)
                .setParameter("userId", userId)
                .getResultStream()
                .toList();
    }

    @Override
    public Optional<CartEntity> findByUserIdAndProductId(int userId, int productId) {
        return Optional.ofNullable(
                entityManager.createQuery("SELECT c FROM CartEntity c WHERE c.user.id = :userId AND c.product.id = :productId", CartEntity.class)
                        .setParameter("userId", userId)
                        .setParameter("productId", productId)
                        .getResultStream()
                        .findFirst()
                        .orElse(null));
    }

    @Override
    public void save(CartEntity cart) {
        entityManager.persist(cart);
    }

    @Override
    public void update(CartEntity cart) {
        entityManager.merge(cart);
    }

    @Override
    public void delete(int id) {
        Optional.ofNullable(entityManager.find(CartEntity.class, id))
                .ifPresent(entityManager::remove);
    }
}