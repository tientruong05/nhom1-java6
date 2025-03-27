package poly.edu.DTO;

import lombok.Data;

@Data
public class OrderDetailDTO {
    private int id;
    private String productName;
    private String image;
    private int qty;        // Khớp với qty trong order_detail
    private float price;    // Khớp với price trong order_detail

    public OrderDetailDTO(int id, String productName, String image, int qty, float price) {
        this.id = id;
        this.productName = productName;
        this.image = prependImagePath(image);
        this.qty = qty;
        this.price = price;
    }

    private String prependImagePath(String imagePath) {
        return (imagePath != null && !imagePath.isEmpty() && !imagePath.startsWith("http"))
                ? "http://localhost:8080/photos/" + imagePath
                : imagePath;
    }

    public void setImage(String image) {
        this.image = prependImagePath(image);
    }
}