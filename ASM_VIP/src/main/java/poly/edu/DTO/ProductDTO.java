package poly.edu.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
<<<<<<< HEAD
import poly.edu.entity.SubCategoryEntity;
=======
import lombok.NoArgsConstructor;
>>>>>>> tien/flash-sale

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private int id;
    private String name;
    private String image;
    private float price;
<<<<<<< HEAD
    private Float discountedPrice; // Giá sau giảm giá
    private Float discountPercentage; // Phần trăm giảm giá
    private int qty; // Số lượng
    private String description; // Mô tả
    private int status; // Trạng thái
    private SubCategoryEntity subCategory; // Để hiển thị Loại hàng và Hãng
=======
    private float discountedPrice;
    private Float discountPercentage;
    private int qty;
    private int sellProgress; // % tiến trình đã bán
>>>>>>> tien/flash-sale
}