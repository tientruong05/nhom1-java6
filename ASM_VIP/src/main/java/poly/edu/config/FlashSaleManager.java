package poly.edu.config;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class FlashSaleManager {

    // Danh sách sản phẩm tham gia Flash Sale: key là id sản phẩm, value là tỷ lệ giảm giá (%)
    private static final Map<Integer, Integer> FLASH_SALE_PRODUCTS = new HashMap<>();

    // Thời gian bắt đầu và kết thúc Flash Sale
    private static final LocalDateTime START_TIME = LocalDateTime.parse("2025-03-25T00:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    private static final LocalDateTime END_TIME = LocalDateTime.parse("2025-03-27T23:59:59", DateTimeFormatter.ISO_LOCAL_DATE_TIME);

    static {
        // Hard-code danh sách sản phẩm và tỷ lệ giảm giá
        FLASH_SALE_PRODUCTS.put(1, 20); // Sản phẩm id=1 giảm 20%
        FLASH_SALE_PRODUCTS.put(2, 15); // Sản phẩm id=2 giảm 15%
        FLASH_SALE_PRODUCTS.put(3, 10); // Sản phẩm id=3 giảm 10%
        // Thêm các sản phẩm khác nếu cần
    }

    // Kiểm tra xem Flash Sale có đang diễn ra không
    public static boolean isFlashSaleActive() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(START_TIME) && now.isBefore(END_TIME);
    }

    // Lấy tỷ lệ giảm giá của sản phẩm (nếu sản phẩm tham gia Flash Sale và Flash Sale đang diễn ra)
    public static Integer getDiscountRate(Integer productId) {
        if (isFlashSaleActive() && FLASH_SALE_PRODUCTS.containsKey(productId)) {
            return FLASH_SALE_PRODUCTS.get(productId);
        }
        return null; // Không giảm giá nếu không trong Flash Sale
    }

    // Lấy thời gian kết thúc Flash Sale (để hiển thị countdown trên giao diện)
    public static LocalDateTime getEndTime() {
        return END_TIME;
    }
}