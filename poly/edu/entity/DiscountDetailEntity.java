package poly.edu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "discount_details")
@Table(name = "discount_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscountDetailEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Column(name = "discount_id", nullable = false)
    private int discountId;
    
    @Column(name = "categories_id")
    private Integer categoriesId;
    
    @Column(name = "subcategories_id")
    private Integer subcategoriesId;
    
    @Column(name = "product_id")
    private Integer productId;
    
    @Column(name = "status", nullable = false)
    private int status;
} 