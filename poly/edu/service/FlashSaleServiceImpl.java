package poly.edu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import poly.edu.DTO.ProductDTO;
import poly.edu.dao.DiscountDAO;
import poly.edu.entity.DiscountEntity;
import poly.edu.entity.ProductEntity;
import poly.edu.repository.DiscountDetailRepository;
import poly.edu.repository.DiscountRepository;
import poly.edu.repository.ProductRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FlashSaleServiceImpl implements FlashSaleService {

    @Autowired
    private DiscountRepository discountRepository;
    
    @Autowired
    private DiscountDetailRepository discountDetailRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private DiscountDAO discountDAO;

    @Override
    public boolean isFlashSaleActive() {
        LocalDateTime now = LocalDateTime.now();
        return discountRepository.findActiveFlashSale(now)
                .map(discount -> true)
                .orElse(false);
    }

    @Override
    public LocalDateTime getFlashSaleEndTime() {
        return discountRepository.findActiveFlashSale(LocalDateTime.now())
                .map(discount -> discount.getEndDate().atStartOfDay())
                .orElse(LocalDateTime.now().plusHours(1)); // Mặc định 1 giờ nếu không tìm thấy
    }

    @Override
    public List<ProductDTO> getFlashSaleProducts() {
        LocalDateTime now = LocalDateTime.now();
        return discountRepository.findActiveFlashSale(now)
                .map(discount -> {
                    List<Integer> productIds = discountDetailRepository.findProductIdsByDiscountId(discount.getId());
                    return productRepository.findByIdInAndStatus(productIds, 1).stream()
                            .map(this::mapToProductDTO)
                            .collect(Collectors.toList());
                })
                .orElse(List.of());
    }
    
    @Override
    public Optional<DiscountEntity> getCurrentFlashSale() {
        return discountRepository.findActiveFlashSale(LocalDateTime.now());
    }
    
    @Override
    public int calculateSellProgress(int availableQuantity, int totalQuantity) {
        if (totalQuantity <= 0) return 0;
        int soldQuantity = totalQuantity - availableQuantity;
        return Math.min(100, (soldQuantity * 100) / totalQuantity);
    }
    
    @Override
    public boolean isProductInFlashSale(int productId) {
        return getCurrentFlashSale()
                .map(discount -> discountDetailRepository.existsByDiscountIdAndProductId(
                        discount.getId(), productId))
                .orElse(false);
    }
    
    private ProductDTO mapToProductDTO(ProductEntity product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setImage(product.getImage());
        dto.setPrice(product.getPrice());
        dto.setQty(product.getQty());
        
        // Tính giá giảm và phần trăm giảm giá
        getCurrentFlashSale().ifPresent(discount -> {
            float discountValue = discount.getDiscountValue();
            float discountedPrice = product.getPrice() * (1 - discountValue / 100);
            dto.setDiscountedPrice(discountedPrice);
            dto.setDiscountPercentage(discountValue);
        });
        
        return dto;
    }
} 