package poly.edu.service;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import poly.edu.DTO.ProductDTO;
import poly.edu.dao.CategoryDAO;
import poly.edu.dao.DiscountDAO;
import poly.edu.dao.ProductDAO;
import poly.edu.dao.SubCategoryDAO;
import poly.edu.entity.CategoryEntity;
import poly.edu.entity.DiscountEntity;
import poly.edu.entity.ProductEntity;
import poly.edu.entity.SubCategoryEntity;
import poly.edu.repository.DiscountDetailRepository;
import poly.edu.repository.ProductRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    private ProductDAO productDAO;

    @Autowired
    private CategoryDAO categoryDAO; // Sử dụng CategoryDAO thay vì CategoryRepository

    @Autowired
    private SubCategoryDAO subCategoryDAO;

    @Autowired
    private DiscountDAO discountDAO;

    @Autowired
    private DiscountDetailRepository discountDetailRepository;

    @Autowired
    private ProductRepository productRepository; // Sử dụng repository để lấy theo ID và danh sách ID

    @Override
    public List<CategoryEntity> getCategoriesByStatus(int status) {
        return categoryDAO.findByStatus(status);
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
        if (category != null) {
            categoryDAO.save(category);
        }
    }

    @Override
    public void updateCategory(CategoryEntity category) {
        if (category != null) {
            categoryDAO.update(category);
        }
    }

    @Override
    public void deleteCategory(int id) {
        if (id > 0) {
            categoryDAO.delete(id);
        }
    }

    @Override
    public List<ProductEntity> getProductsBySubCategory(int subCategoryId) {
        return productDAO.findBySubCategoryId(subCategoryId);
    }

    @Override
    public Page<ProductDTO> getProductsBySubCategory(int subCategoryId, String search, String gender, String priceRange, Pageable pageable) {
        Page<ProductEntity> productPage = productDAO.findBySubCategoryIdWithFilters(subCategoryId, search, gender, priceRange, pageable);
        return mapProductPageToDtoPage(productPage, pageable);
    }

    @Override
    public List<ProductEntity> getProductsByCategory(int categoryId) {
        return productDAO.findByCategoryId(categoryId);
    }

    @Override
    public Page<ProductDTO> getProductsByCategory(int categoryId, String search, String gender, String priceRange, Pageable pageable) {
        Page<ProductEntity> productPage = productDAO.findByCategoryIdWithFilters(categoryId, search, gender, priceRange, pageable);
        return mapProductPageToDtoPage(productPage, pageable);
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
    public Page<ProductDTO> getAllProducts(String search, String gender, String priceRange, Pageable pageable) {
        Page<ProductEntity> productPage = productDAO.findAllWithFilters(search, gender, priceRange, pageable);
        return mapProductPageToDtoPage(productPage, pageable);
    }

    @Override
    public List<ProductEntity> getProductsByBrandName(String brandName) {
        return subCategoryDAO.findBySubCategoriesName(brandName).stream()
                .map(SubCategoryEntity::getId)
                .collect(Collectors.collectingAndThen(Collectors.toList(),
                        ids -> productDAO.findBySubCategoryIds(ids)));
    }

    @Override
    public Page<ProductDTO> getProductsByBrandName(String brandName, String search, String gender, String priceRange, Pageable pageable) {
        List<Integer> subCategoryIds = subCategoryDAO.findBySubCategoriesName(brandName).stream()
                .map(SubCategoryEntity::getId)
                .collect(Collectors.toList());
        Page<ProductEntity> productPage = productDAO.findBySubCategoryIdsWithFilters(subCategoryIds, search, gender, priceRange, pageable);
        return mapProductPageToDtoPage(productPage, pageable);
    }

    @Override
    public Page<ProductDTO> getDiscountedProducts(String search, String gender, String priceRange, Pageable pageable) {
        try {
            // 1. Lấy tất cả các Discount đang hoạt động
            List<DiscountEntity> activeDiscounts = discountDAO.findAllActiveDiscounts();
            if (activeDiscounts.isEmpty()) {
                logger.info("No active discounts found.");
                return Page.empty(pageable);
            }

            // 2. Tạo map DiscountID -> DiscountEntity để dễ tra cứu
            java.util.Map<Integer, DiscountEntity> activeDiscountMap = activeDiscounts.stream()
                    .collect(Collectors.toMap(DiscountEntity::getId, d -> d));

            // 3. Lấy tất cả product_id, category_id, subcategory_id từ discount_details của các discount đang hoạt động
            List<Integer> discountIds = new ArrayList<>(activeDiscountMap.keySet());
            List<Integer> productIdsInDiscount = discountDetailRepository.findProductIdsByDiscountIds(discountIds);
            List<Integer> categoryIdsInDiscount = discountDetailRepository.findCategoryIdsByDiscountIds(discountIds);
            List<Integer> subCategoryIdsInDiscount = discountDetailRepository.findSubCategoryIdsByDiscountIds(discountIds);

            logger.debug("Discount IDs: {}", discountIds);
            logger.debug("Product IDs in discount: {}", productIdsInDiscount.size());
            logger.debug("Category IDs in discount: {}", categoryIdsInDiscount.size());
            logger.debug("SubCategory IDs in discount: {}", subCategoryIdsInDiscount.size());

            // 4. Lấy tất cả các ProductEntity liên quan (trực tiếp, theo category, theo subcategory)
            List<ProductEntity> discountedProductEntities = new ArrayList<>();

            // Sản phẩm được chỉ định trực tiếp
            if (!productIdsInDiscount.isEmpty()) {
                discountedProductEntities.addAll(productRepository.findByIdInAndStatus(productIdsInDiscount, 1));
            }

            // Sản phẩm thuộc category giảm giá
            if (!categoryIdsInDiscount.isEmpty()) {
                List<ProductEntity> categoryProducts = productRepository.findByCategoryIdsAndStatus(categoryIdsInDiscount, 1);
                // Chỉ thêm nếu chưa có trong danh sách
                categoryProducts.forEach(p -> {
                    if (!containsProduct(discountedProductEntities, p.getId())) {
                        discountedProductEntities.add(p);
                    }
                });
            }

            // Sản phẩm thuộc subcategory giảm giá
            if (!subCategoryIdsInDiscount.isEmpty()) {
                List<ProductEntity> subCategoryProducts = productRepository.findBySubCategoryIdsAndStatus(subCategoryIdsInDiscount, 1);
                // Chỉ thêm nếu chưa có trong danh sách
                subCategoryProducts.forEach(p -> {
                    if (!containsProduct(discountedProductEntities, p.getId())) {
                        discountedProductEntities.add(p);
                    }
                });
            }
            logger.debug("Total unique products considered for discount: {}", discountedProductEntities.size());

            // 5. Áp dụng các bộ lọc (search, gender, priceRange)
            List<ProductDTO> filteredDiscountedProducts = discountedProductEntities.stream()
                // Áp dụng các bộ lọc khác
                .filter(p -> filterBySearch(p, search))
                .filter(p -> filterByGender(p, gender))
                .filter(p -> filterByPriceRange(p, priceRange))
                // Map sang DTO và tính toán giảm giá
                .map(product -> mapToProductDTOWithActiveDiscounts(product, activeDiscountMap))
                .collect(Collectors.toList());

            logger.debug("Products after filtering: {}", filteredDiscountedProducts.size());

            // 6. Tạo Page từ danh sách đã lọc
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), filteredDiscountedProducts.size());

            if (start > filteredDiscountedProducts.size()) {
                return new PageImpl<>(Collections.emptyList(), pageable, filteredDiscountedProducts.size());
            }

            List<ProductDTO> pageContent = filteredDiscountedProducts.subList(start, end);
            return new PageImpl<>(pageContent, pageable, filteredDiscountedProducts.size());

        } catch (Exception e) {
            logger.error("Error getting discounted products with filters", e);
            return Page.empty(pageable);
        }
    }

    @Override
    public ProductEntity getProductById1(Integer productId) {
        return productId != null ? productDAO.findById(productId).orElse(null) : null;
    }

    @Override
    public ProductEntity getProductById(Integer productId) {
        return productId != null ? productDAO.findById(productId).orElse(null) : null;
    }

    private Page<ProductDTO> mapProductPageToDtoPage(Page<ProductEntity> productPage, Pageable pageable) {
        List<ProductDTO> productDTOs = productPage.getContent().stream()
                .map(this::mapToProductDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(productDTOs, pageable, productPage.getTotalElements());
    }

    private ProductDTO mapToProductDTO(ProductEntity product) {
        List<DiscountEntity> activeDiscounts = discountDAO.findAllActiveDiscountsForProduct(product.getId(),
                product.getSubCategory().getCategory().getId(), product.getSubCategory().getId());
        
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setImage(product.getImage());
        dto.setPrice(product.getPrice());
        dto.setQty(product.getQty());
        dto.setStatus(product.getStatus());
        dto.setDescription(product.getDescription());
        dto.setSubCategory(product.getSubCategory());
        
        Optional<DiscountEntity> bestDiscount = activeDiscounts.stream()
            .max((d1, d2) -> Float.compare(d1.getDiscountValue(), d2.getDiscountValue()));

        if (bestDiscount.isPresent()) {
            DiscountEntity discount = bestDiscount.get();
            float discountValue = discount.getDiscountValue();
            float discountedPrice = product.getPrice() * (1 - discountValue / 100);
            dto.setDiscountedPrice(discountedPrice);
            dto.setDiscountPercentage(discountValue);
            dto.setDiscounted(true);
            logger.trace("Mapped ProductEntity ID {} to ProductDTO with active discount {}%", product.getId(), discountValue);
        } else {
            dto.setDiscountedPrice(null);
            dto.setDiscountPercentage(null);
            dto.setDiscounted(false);
            logger.trace("Mapped ProductEntity ID {} to ProductDTO with no active discount", product.getId());
        }
        
        int currentQty = product.getQty();
        int estimatedInitialQty = Math.max(30, currentQty * 2);
        dto.setSellProgress(calculateSellProgress(currentQty, estimatedInitialQty));

        return dto;
    }
    
    private ProductDTO mapToProductDTOWithActiveDiscounts(ProductEntity product, java.util.Map<Integer, DiscountEntity> activeDiscountMap) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setImage(product.getImage());
        dto.setPrice(product.getPrice());
        dto.setQty(product.getQty());
        dto.setStatus(product.getStatus());
        dto.setDescription(product.getDescription());
        dto.setSubCategory(product.getSubCategory());
        
        Optional<DiscountEntity> bestDiscount = findBestApplicableDiscount(product, activeDiscountMap);

        if (bestDiscount.isPresent()) {
            DiscountEntity discount = bestDiscount.get();
            float discountValue = discount.getDiscountValue();
            float discountedPrice = product.getPrice() * (1 - discountValue / 100);
            dto.setDiscountedPrice(discountedPrice);
            dto.setDiscountPercentage(discountValue);
            dto.setDiscounted(true);
            logger.trace("(Optimized) Mapped ProductEntity ID {} to ProductDTO with discount {}%", product.getId(), discountValue);
        } else {
            dto.setDiscountedPrice(null);
            dto.setDiscountPercentage(null);
            dto.setDiscounted(false);
             logger.warn("(Optimized) Product ID {} was expected to have a discount but none was applied.", product.getId());
        }
        
        int currentQty = product.getQty();
        int estimatedInitialQty = Math.max(30, currentQty * 2);
        dto.setSellProgress(calculateSellProgress(currentQty, estimatedInitialQty));

        return dto;
    }
    
    private Optional<DiscountEntity> findBestApplicableDiscount(ProductEntity product, java.util.Map<Integer, DiscountEntity> activeDiscountMap) {
        List<DiscountEntity> applicableDiscounts = new ArrayList<>();

        List<Integer> relatedDiscountIds = discountDetailRepository
                .findDiscountIdsByProductOrCategoryOrSubCategory(
                        product.getId(), 
                        product.getSubCategory().getCategory().getId(), 
                        product.getSubCategory().getId());

        for (Integer discountId : relatedDiscountIds) {
            DiscountEntity discount = activeDiscountMap.get(discountId);
            if (discount != null) {
                 if (discountDetailRepository.existsByDiscountIdAndProductId(discountId, product.getId()) ||
                     discountDetailRepository.existsByDiscountIdAndCategoriesId(discountId, product.getSubCategory().getCategory().getId()) ||
                     discountDetailRepository.existsByDiscountIdAndSubcategoriesId(discountId, product.getSubCategory().getId())) {
                     applicableDiscounts.add(discount);
                 }
            }
        }

        return applicableDiscounts.stream()
                .max((d1, d2) -> Float.compare(d1.getDiscountValue(), d2.getDiscountValue()));
    }

    private boolean containsProduct(List<ProductEntity> products, int productId) {
        return products.stream().anyMatch(p -> p.getId() == productId);
    }

    private int calculateSellProgress(int availableQuantity, int totalQuantity) {
        if (totalQuantity <= 0) return 0;
        int soldQuantity = totalQuantity - availableQuantity;
        return Math.min(100, (int) (((double) soldQuantity / totalQuantity) * 100));
    }

    private boolean filterBySearch(ProductEntity product, String search) {
        if (search == null || search.trim().isEmpty()) {
            return true;
        }
        String lowerSearch = search.toLowerCase();
        return (product.getName() != null && product.getName().toLowerCase().contains(lowerSearch)) ||
               (product.getSubCategory() != null && product.getSubCategory().getSubCategoriesName() != null && product.getSubCategory().getSubCategoriesName().toLowerCase().contains(lowerSearch)) ||
               (product.getSubCategory() != null && product.getSubCategory().getCategory() != null && product.getSubCategory().getCategory().getName() != null && product.getSubCategory().getCategory().getName().toLowerCase().contains(lowerSearch));
    }

    private boolean filterByGender(ProductEntity product, String gender) {
        if (gender == null || gender.trim().isEmpty()) {
            return true;
        }
        if (product.getSubCategory() == null || product.getSubCategory().getCategory() == null) {
            return false;
        }
        int categoryId = product.getSubCategory().getCategory().getId();
        if ("male".equalsIgnoreCase(gender)) {
            return categoryId == 1;
        } else if ("female".equalsIgnoreCase(gender)) {
            return categoryId == 2;
        } else {
            return true;
        }
    }

    private boolean filterByPriceRange(ProductEntity product, String priceRange) {
        if (priceRange == null || priceRange.trim().isEmpty()) {
            return true;
        }
        float price = product.getPrice();

        switch (priceRange) {
            case "1-3":
                return price >= 1000000 && price <= 3000000;
            case "3-5":
                return price > 3000000 && price <= 5000000;
            case "5+":
                return price > 5000000;
            default:
                return true;
        }
    }
}