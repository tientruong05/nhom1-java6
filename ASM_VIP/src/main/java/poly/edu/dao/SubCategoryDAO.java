package poly.edu.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;
import poly.edu.entity.SubCategoryEntity;

import java.util.List;
import java.util.Optional;

public interface SubCategoryDAO {
    List<SubCategoryEntity> findByCategory_Id(int categoryId);
    List<SubCategoryEntity> findByStatus(int status);
    SubCategoryEntity findById(int id);
    List<SubCategoryEntity> findAll();
    void save(SubCategoryEntity subCategory);
    void update(SubCategoryEntity subCategory);
    void delete(int id);
    List<SubCategoryEntity> findBySubCategoriesName(String subCategoriesName);
}

@Transactional
@Repository
class SubCategoryDAOImpl implements SubCategoryDAO {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<SubCategoryEntity> findByCategory_Id(int categoryId) {
        return entityManager.createQuery("SELECT s FROM SubCategoryEntity s WHERE s.category.id = :categoryId", SubCategoryEntity.class)
                .setParameter("categoryId", categoryId)
                .getResultStream()
                .toList();
    }

    @Override
    public List<SubCategoryEntity> findByStatus(int status) {
        return entityManager.createQuery("SELECT s FROM SubCategoryEntity s WHERE s.status = :status", SubCategoryEntity.class)
                .setParameter("status", status)
                .getResultStream()
                .toList();
    }

    @Override
    public SubCategoryEntity findById(int id) {
        return entityManager.find(SubCategoryEntity.class, id);
    }

    @Override
    public List<SubCategoryEntity> findAll() {
        return entityManager.createQuery("SELECT s FROM SubCategoryEntity s", SubCategoryEntity.class)
                .getResultStream()
                .toList();
    }

    @Override
    public void save(SubCategoryEntity subCategory) {
        entityManager.persist(subCategory);
    }

    @Override
    public void update(SubCategoryEntity subCategory) {
        entityManager.merge(subCategory);
    }

    @Override
    public void delete(int id) {
        Optional.ofNullable(entityManager.find(SubCategoryEntity.class, id))
                .ifPresent(entityManager::remove);
    }

    @Override
    public List<SubCategoryEntity> findBySubCategoriesName(String subCategoriesName) {
        return entityManager.createQuery("SELECT s FROM SubCategoryEntity s WHERE s.subCategoriesName = :subCategoriesName", SubCategoryEntity.class)
                .setParameter("subCategoriesName", subCategoriesName)
                .getResultStream()
                .toList();
    }
}