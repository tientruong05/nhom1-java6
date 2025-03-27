package poly.edu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_subcategories", nullable = false)
    private SubCategoryEntity subCategory;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "image")
    private String image;

    @Column(name = "price", nullable = false)
    private float price;

    @Column(name = "qty", nullable = false)
    private int qty;

    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "status", nullable = false)
    private int status;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<DiscountDetailEntity> discountDetails;

    // Tính giá sau giảm giá
    @Transient
    public float getDiscountedPrice() {
        if (discountDetails != null && !discountDetails.isEmpty()) {
            for (DiscountDetailEntity detail : discountDetails) {
                DiscountEntity discount = detail.getDiscount();
                if (discount != null && discount.isActive() && detail.getStatus() == 1) {
                    float discountValue = discount.getDiscountValue();
                    // Giả định discount_value là phần trăm
                    return price - (price * discountValue / 100);
                }
            }
        }
        return price; // Không có giảm giá thì trả về giá gốc
    }

    // Lấy phần trăm giảm giá
    @Transient
    public Float getDiscountPercentage() {
        if (discountDetails != null && !discountDetails.isEmpty()) {
            for (DiscountDetailEntity detail : discountDetails) {
                DiscountEntity discount = detail.getDiscount();
                if (discount != null && discount.isActive() && detail.getStatus() == 1) {
                    return discount.getDiscountValue(); // Trả về discount_value (phần trăm)
                }
            }
        }
        return null; // Không có giảm giá
    }

    // Kiểm tra xem sản phẩm có đang giảm giá không
    @Transient
    public boolean isDiscounted() {
        return getDiscountPercentage() != null;
    }
}