package poly.edu.service;

import poly.edu.entity.OrderEntity;
import poly.edu.entity.UserEntity;

import java.util.List;
import java.util.Optional;

public interface OrderService {
    Optional<OrderEntity> getOrderById(int id);
    List<OrderEntity> getAllOrders();
    void addOrder(OrderEntity order);
    void updateOrder(OrderEntity order);
    void removeOrder(int id);
    List<Integer> getAvailableYears();
    List<Object[]> getTop10VipCustomers(Integer year, Integer month);
    List<OrderEntity> findByUser(UserEntity user);
    OrderEntity save(OrderEntity order);
    OrderEntity findById(int id);
    List<OrderEntity> findAll();
    void deleteById(int id);
}

