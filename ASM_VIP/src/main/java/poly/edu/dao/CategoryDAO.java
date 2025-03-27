package poly.edu.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;
import poly.edu.entity.CategoryEntity;

import java.util.List;
import java.util.Optional;

public interface CategoryDAO {
    List<CategoryEntity> findByStatus(int status);
    List<CategoryEntity> findByNameContaining(String name);
    CategoryEntity findById(int id);
    List<CategoryEntity> findAll();
    void save(CategoryEntity category);
    void update(CategoryEntity category);
    void delete(int id);
}

@Transactional
@Repository
class CategoryDAOImpl implements CategoryDAO {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<CategoryEntity> findByStatus(int status) {
        return entityManager.createQuery("SELECT c FROM CategoryEntity c WHERE c.status = :status", CategoryEntity.class)
                .setParameter("status", status)
                .getResultStream()
                .toList();
    }

    @Override
    public List<CategoryEntity> findByNameContaining(String name) {
        return entityManager.createQuery("SELECT c FROM CategoryEntity c WHERE c.name LIKE :name", CategoryEntity.class)
                .setParameter("name", "%" + name + "%")
                .getResultStream()
                .toList();
    }

    @Override
    public CategoryEntity findById(int id) {
        return entityManager.find(CategoryEntity.class, id);
    }

    @Override
    public List<CategoryEntity> findAll() {
        return entityManager.createQuery("SELECT c FROM CategoryEntity c", CategoryEntity.class)
                .getResultStream()
                .toList();
    }

    @Override
    public void save(CategoryEntity category) {
        entityManager.persist(category);
    }

    @Override
    public void update(CategoryEntity category) {
        entityManager.merge(category);
    }

    @Override
    public void delete(int id) {
        Optional.ofNullable(entityManager.find(CategoryEntity.class, id))
                .ifPresent(entityManager::remove);
    }
}