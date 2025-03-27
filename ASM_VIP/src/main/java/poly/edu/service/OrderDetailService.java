package poly.edu.service;

import poly.edu.entity.OrderDetailEntity;
import poly.edu.entity.OrderEntity;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderDetailService {
    Optional<OrderDetailEntity> getOrderDetailById(int id);
    List<OrderDetailEntity> getAllOrderDetails();
    void addOrderDetail(OrderDetailEntity orderDetail);
    void updateOrderDetail(OrderDetailEntity orderDetail);
    void removeOrderDetail(int id);
    List<OrderDetailEntity> findByOrder(OrderEntity order);
    OrderDetailEntity save(OrderDetailEntity orderDetail);
    OrderDetailEntity findById(int id);
    List<OrderDetailEntity> findAll();
    void deleteById(int id);
    List<OrderDetailEntity> getOrderDetailsByUserId(int userId);
    Page<OrderDetailEntity> findByOrders(List<OrderEntity> orders, Pageable pageable);
}

