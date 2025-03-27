package poly.edu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import poly.edu.dao.OrderDAO;
import poly.edu.entity.OrderEntity;
import poly.edu.entity.UserEntity;
import poly.edu.repository.OrderRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderDAO orderDAO;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    public OrderServiceImpl(OrderDAO orderDAO) {
        this.orderDAO = orderDAO;
    }

    @Override
    public Optional<OrderEntity> getOrderById(int id) {
        return orderDAO.findById(id);
    }

    @Override
    public List<OrderEntity> getAllOrders() {
        return orderDAO.findAll().stream()
                .filter(order -> order != null)
                .collect(Collectors.toList());
    }

    @Override
    public void addOrder(OrderEntity order) {
        Optional.ofNullable(order).ifPresent(orderDAO::save);
    }

    @Override
    public void updateOrder(OrderEntity order) {
        Optional.ofNullable(order).ifPresent(orderDAO::update);
    }

    @Override
    public void removeOrder(int id) {
        Optional.of(id).filter(i -> i > 0).ifPresent(orderDAO::delete);
    }

    @Override
    public List<Object[]> getTop10VipCustomers(Integer year, Integer month) {
        return orderDAO.findTop10VipCustomers(year, month);
    }

    @Override
    public List<Integer> getAvailableYears() {
        return orderDAO.findDistinctYears().stream()
                .filter(year -> year != null)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderEntity> findByUser(UserEntity user) {
        return orderRepository.findByUser(user).stream()
                .filter(order -> order != null)
                .collect(Collectors.toList());
    }

    @Override
    public OrderEntity save(OrderEntity order) {
        return Optional.ofNullable(order)
                .map(orderRepository::save)
                .orElse(null);
    }

    @Override
    public OrderEntity findById(int id) {
        return orderRepository.findById(id).orElse(null);
    }

    @Override
    public List<OrderEntity> findAll() {
        return orderRepository.findAll().stream()
                .filter(order -> order != null)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(int id) {
        Optional.of(id).filter(i -> i > 0).ifPresent(orderRepository::deleteById);
    }
}