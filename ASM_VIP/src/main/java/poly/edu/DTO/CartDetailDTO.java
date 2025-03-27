package poly.edu.DTO;

import lombok.Data;

@Data
public class CartDetailDTO {
    private int id;
    private int productId;
    private String productName;
    private String image;
    private int qty;
    private float price;
    private float discountedPrice;
    private Integer discountPercentage;
    private int availableQty;

    public CartDetailDTO(int id, int productId, String productName, String image, int qty, float price, float discountedPrice, Integer discountPercentage, int availableQty) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.image = image;
        this.qty = qty;
        this.price = price;
        this.discountedPrice = discountedPrice;
        this.discountPercentage = discountPercentage;
        this.availableQty = availableQty;
    }
}