package poly.edu.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;
import poly.edu.entity.DiscountEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Transactional
@Repository
public class DiscountDAOImpl implements DiscountDAO {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<DiscountEntity> findById(int id) {
        return Optional.ofNullable(entityManager.find(DiscountEntity.class, id));
    }

    @Override
    public List<DiscountEntity> findAll() {
        return entityManager.createQuery("SELECT d FROM discounts d", DiscountEntity.class)
                .getResultStream()
                .toList();
    }

    @Override
    public void save(DiscountEntity discount) {
        entityManager.persist(discount);
    }

    @Override
    public void update(DiscountEntity discount) {
        entityManager.merge(discount);
    }

    @Override
    public void delete(int id) {
        Optional.ofNullable(entityManager.find(DiscountEntity.class, id))
                .ifPresent(entityManager::remove);
    }
    
    @Override
    public Optional<DiscountEntity> findActiveFlashSale(LocalDateTime now) {
        try {
            return Optional.ofNullable(entityManager.createQuery(
                    "SELECT d FROM discounts d WHERE d.status = 1 " +
                    "AND d.startDate <= CONVERT(DATE, :now) " +
                    "AND d.endDate >= CONVERT(DATE, :now) " +
                    "AND LOWER(d.discountName) LIKE '%flash sale%'", 
                    DiscountEntity.class)
                    .setParameter("now", now)
                    .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
} 