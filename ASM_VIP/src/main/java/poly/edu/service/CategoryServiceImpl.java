package poly.edu.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import poly.edu.dao.CategoryDAO;
import poly.edu.dao.ProductDAO;
import poly.edu.dao.SubCategoryDAO;
import poly.edu.entity.CategoryEntity;
import poly.edu.entity.ProductEntity;
import poly.edu.entity.SubCategoryEntity;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private ProductDAO productDAO;

    @Autowired
    private CategoryDAO categoryDAO;

    @Autowired
    private SubCategoryDAO subCategoryDAO;

    @Override
    public List<CategoryEntity> getCategoriesByStatus(int status) {
        return categoryDAO.findByStatus(status).stream()
                .filter(category -> category.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryEntity> searchCategoriesByName(String name) {
        return categoryDAO.findByNameContaining(name);
    }

    @Override
    public CategoryEntity getCategoryById(int id) {
        return categoryDAO.findById(id);
    }

    @Override
    public List<CategoryEntity> getAllCategories() {
        return categoryDAO.findAll();
    }

    @Override
    public void createCategory(CategoryEntity category) {
        Optional.ofNullable(category).ifPresent(categoryDAO::save);
    }

    @Override
    public void updateCategory(CategoryEntity category) {
        Optional.ofNullable(category).ifPresent(categoryDAO::update);
    }

    @Override
    public void deleteCategory(int id) {
        Optional.of(id).filter(i -> i > 0).ifPresent(categoryDAO::delete);
    }

    @Override
    public List<ProductEntity> getProductsBySubCategory(int subCategoryId) {
        return productDAO.findBySubCategoryId(subCategoryId);
    }

    @Override
    public Page<ProductEntity> getProductsBySubCategory(int subCategoryId, String search, String gender, String priceRange, Pageable pageable) {
        return productDAO.findBySubCategoryIdWithFilters(subCategoryId, search, gender, priceRange, pageable);
    }

    @Override
    public List<ProductEntity> getProductsByCategory(int categoryId) {
        return productDAO.findByCategoryId(categoryId);
    }

    @Override
    public Page<ProductEntity> getProductsByCategory(int categoryId, String search, String gender, String priceRange, Pageable pageable) {
        return productDAO.findByCategoryIdWithFilters(categoryId, search, gender, priceRange, pageable);
    }

    @Override
    public String getCategoryName(int categoryId) {
        return Optional.ofNullable(categoryDAO.findById(categoryId))
                .map(CategoryEntity::getName)
                .orElse("");
    }

    @Override
    public String getSubCategoryName(int subCategoryId) {
        return Optional.ofNullable(subCategoryDAO.findById(subCategoryId))
                .map(SubCategoryEntity::getSubCategoriesName)
                .orElse("");
    }

    @Override
    public List<ProductEntity> getAllProducts() {
        return productDAO.findAll();
    }

    @Override
    public Page<ProductEntity> getAllProducts(String search, String gender, String priceRange, Pageable pageable) {
        return productDAO.findAllWithFilters(search, gender, priceRange, pageable);
    }

    @Override
    public List<ProductEntity> getProductsByBrandName(String brandName) {
        return subCategoryDAO.findBySubCategoriesName(brandName).stream()
                .map(SubCategoryEntity::getId)
                .collect(Collectors.collectingAndThen(Collectors.toList(),
                        ids -> productDAO.findBySubCategoryIds(ids)));
    }

    @Override
    public Page<ProductEntity> getProductsByBrandName(String brandName, String search, String gender, String priceRange, Pageable pageable) {
        List<Integer> subCategoryIds = subCategoryDAO.findBySubCategoriesName(brandName).stream()
                .map(SubCategoryEntity::getId)
                .collect(Collectors.toList());
        return productDAO.findBySubCategoryIdsWithFilters(subCategoryIds, search, gender, priceRange, pageable);
    }

    @Override
    public Page<ProductEntity> getDiscountedProducts(String search, String gender, String priceRange, Pageable pageable) {
        return productDAO.findDiscountedProductsWithFilters(search, gender, priceRange, pageable);
    }

    @Override
    public ProductEntity getProductById1(Integer productId) {
        return productId != null ? productDAO.findById(productId).orElse(null) : null;
    }

    @Override
    public ProductEntity getProductById(Integer productId) {
        return productId != null ? productDAO.findById(productId).orElse(null) : null;
    }
}