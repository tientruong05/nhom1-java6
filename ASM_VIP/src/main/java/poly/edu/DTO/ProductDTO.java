package poly.edu.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import poly.edu.entity.SubCategoryEntity;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private int id;
    private String name;
    private String image;
    private float price;
    private float discountedPrice;
    private Float discountPercentage;
    private int qty;
    private int sellProgress; // % tiến trình đã bán
    private int status; // 0: không hoạt động, 1: hoạt động
    private String description;
    private SubCategoryEntity subCategory;
}
