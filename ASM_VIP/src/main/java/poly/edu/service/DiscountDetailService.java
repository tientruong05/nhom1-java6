package poly.edu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import poly.edu.entity.DiscountDetailEntity;
import poly.edu.entity.DiscountEntity;
import poly.edu.repository.DiscountDetailRepository;

import java.util.List;

@Service
public class DiscountDetailService {

    @Autowired
    private DiscountDetailRepository discountDetailRepository;

    public List<DiscountDetailEntity> getDiscountDetailsByDiscount(DiscountEntity discount) {
        return discountDetailRepository.findByDiscount(discount);
    }

    public DiscountDetailEntity saveDiscountDetail(DiscountDetailEntity detail) {
        return discountDetailRepository.save(detail);
    }

    public void deleteDiscountDetail(int id) {
        discountDetailRepository.deleteById(id);
    }

    public void deleteDiscountDetailsByDiscountId(int discountId){
        discountDetailRepository.deleteByDiscountId(discountId);
    }
}