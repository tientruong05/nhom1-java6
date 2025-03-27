package poly.edu.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import poly.edu.entity.ProductEntity;

import java.util.List;
import java.util.Optional;

public interface ProductDAO {
    Optional<ProductEntity> findById(int id);
    List<ProductEntity> findByNameContaining(String name);
    List<ProductEntity> findAll();
    Page<ProductEntity> findAll(Pageable pageable);
    Page<ProductEntity> findAllWithFilters(String search, String gender, String priceRange, Pageable pageable);
    void save(ProductEntity product);
    void update(ProductEntity product);
    void delete(int id);
    List<ProductEntity> findBySubCategoryId(int subCategoryId);
    Page<ProductEntity> findBySubCategoryId(int subCategoryId, Pageable pageable);
    Page<ProductEntity> findBySubCategoryIdWithFilters(int subCategoryId, String search, String gender, String priceRange, Pageable pageable);
    List<ProductEntity> findByCategoryId(int categoryId);
    Page<ProductEntity> findByCategoryId(int categoryId, Pageable pageable);
    Page<ProductEntity> findByCategoryIdWithFilters(int categoryId, String search, String gender, String priceRange, Pageable pageable);
    List<ProductEntity> findBySubCategoryIds(List<Integer> subCategoryIds);
    Page<ProductEntity> findBySubCategoryIds(List<Integer> subCategoryIds, Pageable pageable);
    Page<ProductEntity> findBySubCategoryIdsWithFilters(List<Integer> subCategoryIds, String search, String gender, String priceRange, Pageable pageable);
    Page<ProductEntity> findDiscountedProductsWithFilters(String search, String gender, String priceRange, Pageable pageable);
}

@Transactional
@Repository
class ProductDAOImpl implements ProductDAO {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<ProductEntity> findById(int id) {
        return Optional.ofNullable(entityManager.find(ProductEntity.class, id));
    }

    @Override
    public List<ProductEntity> findByNameContaining(String name) {
        return entityManager.createQuery("SELECT p FROM ProductEntity p WHERE p.name LIKE :name AND p.status = 1", ProductEntity.class)
                .setParameter("name", "%" + name + "%")
                .getResultStream()
                .toList();
    }

    @Override
    public List<ProductEntity> findAll() {
        return entityManager.createQuery("SELECT p FROM ProductEntity p WHERE p.status = 1", ProductEntity.class)
                .getResultStream()
                .toList();
    }

    @Override
    public Page<ProductEntity> findAll(Pageable pageable) {
        return createPage("SELECT p FROM ProductEntity p WHERE p.status = 1",
                "SELECT COUNT(p) FROM ProductEntity p WHERE p.status = 1",
                pageable, null);
    }

    @Override
    public Page<ProductEntity> findAllWithFilters(String search, String gender, String priceRange, Pageable pageable) {
        StringBuilder jpql = new StringBuilder("SELECT p FROM ProductEntity p WHERE p.status = 1");
        StringBuilder countJpql = new StringBuilder("SELECT COUNT(p) FROM ProductEntity p WHERE p.status = 1");
        appendFilters(jpql, countJpql, search, gender, priceRange);
        return createPage(jpql.toString(), countJpql.toString(), pageable, query -> setFilterParameters(query, search, gender));
    }

    @Override
    public void save(ProductEntity product) {
        entityManager.persist(product);
    }

    @Override
    public void update(ProductEntity product) {
        entityManager.merge(product);
    }

    @Override
    public void delete(int id) {
        Optional.ofNullable(entityManager.find(ProductEntity.class, id))
                .ifPresent(entityManager::remove);
    }

    @Override
    public List<ProductEntity> findBySubCategoryId(int subCategoryId) {
        return entityManager.createQuery("SELECT p FROM ProductEntity p WHERE p.subCategory.id = :subCategoryId AND p.status = 1", ProductEntity.class)
                .setParameter("subCategoryId", subCategoryId)
                .getResultStream()
                .toList();
    }

    @Override
    public Page<ProductEntity> findBySubCategoryId(int subCategoryId, Pageable pageable) {
        return createPage("SELECT p FROM ProductEntity p WHERE p.subCategory.id = :subCategoryId AND p.status = 1",
                "SELECT COUNT(p) FROM ProductEntity p WHERE p.subCategory.id = :subCategoryId AND p.status = 1",
                pageable, query -> query.setParameter("subCategoryId", subCategoryId));
    }

    @Override
    public Page<ProductEntity> findBySubCategoryIdWithFilters(int subCategoryId, String search, String gender, String priceRange, Pageable pageable) {
        StringBuilder jpql = new StringBuilder("SELECT p FROM ProductEntity p WHERE p.subCategory.id = :subCategoryId AND p.status = 1");
        StringBuilder countJpql = new StringBuilder("SELECT COUNT(p) FROM ProductEntity p WHERE p.subCategory.id = :subCategoryId AND p.status = 1");
        appendFilters(jpql, countJpql, search, gender, priceRange);
        return createPage(jpql.toString(), countJpql.toString(), pageable,
                query -> {
                    query.setParameter("subCategoryId", subCategoryId);
                    setFilterParameters(query, search, gender);
                });
    }

    @Override
    public List<ProductEntity> findByCategoryId(int categoryId) {
        return entityManager.createQuery("SELECT p FROM ProductEntity p WHERE p.subCategory.category.id = :categoryId AND p.status = 1", ProductEntity.class)
                .setParameter("categoryId", categoryId)
                .getResultStream()
                .toList();
    }

    @Override
    public Page<ProductEntity> findByCategoryId(int categoryId, Pageable pageable) {
        return createPage("SELECT p FROM ProductEntity p WHERE p.subCategory.category.id = :categoryId AND p.status = 1",
                "SELECT COUNT(p) FROM ProductEntity p WHERE p.subCategory.category.id = :categoryId AND p.status = 1",
                pageable, query -> query.setParameter("categoryId", categoryId));
    }

    @Override
    public Page<ProductEntity> findByCategoryIdWithFilters(int categoryId, String search, String gender, String priceRange, Pageable pageable) {
        StringBuilder jpql = new StringBuilder("SELECT p FROM ProductEntity p WHERE p.subCategory.category.id = :categoryId AND p.status = 1");
        StringBuilder countJpql = new StringBuilder("SELECT COUNT(p) FROM ProductEntity p WHERE p.subCategory.category.id = :categoryId AND p.status = 1");
        appendFilters(jpql, countJpql, search, gender, priceRange);
        return createPage(jpql.toString(), countJpql.toString(), pageable,
                query -> {
                    query.setParameter("categoryId", categoryId);
                    setFilterParameters(query, search, gender);
                });
    }

    @Override
    public List<ProductEntity> findBySubCategoryIds(List<Integer> subCategoryIds) {
        return entityManager.createQuery("SELECT p FROM ProductEntity p WHERE p.subCategory.id IN :subCategoryIds AND p.status = 1", ProductEntity.class)
                .setParameter("subCategoryIds", subCategoryIds)
                .getResultStream()
                .toList();
    }

    @Override
    public Page<ProductEntity> findBySubCategoryIds(List<Integer> subCategoryIds, Pageable pageable) {
        return createPage("SELECT p FROM ProductEntity p WHERE p.subCategory.id IN :subCategoryIds AND p.status = 1",
                "SELECT COUNT(p) FROM ProductEntity p WHERE p.subCategory.id IN :subCategoryIds AND p.status = 1",
                pageable, query -> query.setParameter("subCategoryIds", subCategoryIds));
    }

    @Override
    public Page<ProductEntity> findBySubCategoryIdsWithFilters(List<Integer> subCategoryIds, String search, String gender, String priceRange, Pageable pageable) {
        StringBuilder jpql = new StringBuilder("SELECT p FROM ProductEntity p WHERE p.subCategory.id IN :subCategoryIds AND p.status = 1");
        StringBuilder countJpql = new StringBuilder("SELECT COUNT(p) FROM ProductEntity p WHERE p.subCategory.id IN :subCategoryIds AND p.status = 1");
        appendFilters(jpql, countJpql, search, gender, priceRange);
        return createPage(jpql.toString(), countJpql.toString(), pageable,
                query -> {
                    query.setParameter("subCategoryIds", subCategoryIds);
                    setFilterParameters(query, search, gender);
                });
    }

    @Override
    public Page<ProductEntity> findDiscountedProductsWithFilters(String search, String gender, String priceRange, Pageable pageable) {
        StringBuilder jpql = new StringBuilder(
                "SELECT DISTINCT p FROM ProductEntity p " +
                "JOIN p.discountDetails dd " +
                "JOIN dd.discount d " +
                "WHERE p.status = 1 AND dd.status = 1 AND d.status = 1 " +
                "AND d.startDate <= CURRENT_DATE AND d.endDate >= CURRENT_DATE"
        );
        StringBuilder countJpql = new StringBuilder(
                "SELECT COUNT(DISTINCT p) FROM ProductEntity p " +
                "JOIN p.discountDetails dd " +
                "JOIN dd.discount d " +
                "WHERE p.status = 1 AND dd.status = 1 AND d.status = 1 " +
                "AND d.startDate <= CURRENT_DATE AND d.endDate >= CURRENT_DATE"
        );
        appendFilters(jpql, countJpql, search, gender, priceRange);
        return createPage(jpql.toString(), countJpql.toString(), pageable, query -> setFilterParameters(query, search, gender));
    }

    private void appendFilters(StringBuilder jpql, StringBuilder countJpql, String search, String gender, String priceRange) {
        Optional.ofNullable(search).filter(s -> !s.isEmpty())
                .ifPresent(s -> {
                    jpql.append(" AND p.name LIKE :search");
                    countJpql.append(" AND p.name LIKE :search");
                });
        Optional.ofNullable(gender).filter(g -> !g.isEmpty())
                .ifPresent(g -> {
                    jpql.append(" AND p.subCategory.category.id = :genderId");
                    countJpql.append(" AND p.subCategory.category.id = :genderId");
                });
        Optional.ofNullable(priceRange).filter(pr -> !pr.isEmpty())
                .ifPresent(pr -> {
                    String condition = switch (pr) {
                        case "1-3" -> " AND p.price BETWEEN 1000000 AND 3000000";
                        case "3-5" -> " AND p.price BETWEEN 3000000 AND 5000000";
                        case "5+" -> " AND p.price > 5000000";
                        default -> "";
                    };
                    jpql.append(condition);
                    countJpql.append(condition);
                });
    }

    private void setFilterParameters(jakarta.persistence.TypedQuery<?> query, String search, String gender) {
        Optional.ofNullable(search).filter(s -> !s.isEmpty())
                .ifPresent(s -> query.setParameter("search", "%" + s + "%"));
        Optional.ofNullable(gender).filter(g -> !g.isEmpty())
                .ifPresent(g -> query.setParameter("genderId", "male".equals(g) ? 1 : 2));
    }

    private Page<ProductEntity> createPage(String jpql, String countJpql, Pageable pageable, java.util.function.Consumer<jakarta.persistence.TypedQuery<?>> parameterSetter) {
        var query = entityManager.createQuery(jpql, ProductEntity.class);
        var countQuery = entityManager.createQuery(countJpql, Long.class);
        if (parameterSetter != null) {
            parameterSetter.accept(query);
            parameterSetter.accept(countQuery);
        }
        List<ProductEntity> content = query.setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultStream()
                .toList();
        long total = countQuery.getSingleResult();
        return new PageImpl<>(content, pageable, total);
    }
}