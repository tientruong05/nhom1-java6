package poly.edu.DTO;

import lombok.Data;
import poly.edu.entity.SubCategoryEntity;

@Data
public class ProductDTO {
    private int id;
    private String name;
    private String image;
    private float price;
    private Float discountedPrice; // Giá sau giảm giá
    private Float discountPercentage; // Phần trăm giảm giá
    private int qty; // Số lượng
    private String description; // Mô tả
    private int status; // Trạng thái
    private SubCategoryEntity subCategory; // Để hiển thị Loại hàng và Hãng
}