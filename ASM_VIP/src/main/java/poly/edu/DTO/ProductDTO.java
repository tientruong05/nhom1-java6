package poly.edu.DTO;

import lombok.Data;

@Data
public class ProductDTO {
    private int id;
    private String name;
    private String image;
    private float price;
    private Float discountedPrice; // Giá sau giảm giá
    private Float discountPercentage; // Phần trăm giảm giá
}