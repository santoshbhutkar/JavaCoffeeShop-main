package org.workshop.coffee.service;

import org.workshop.coffee.domain.Order;
import org.workshop.coffee.domain.Person;
import org.workshop.coffee.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private LoyaltyPointsService loyaltyPointsService;

    public Order save(Order order) {
        Order savedOrder = orderRepository.save(order);
        // Award loyalty points for the order
        loyaltyPointsService.earnPointsFromOrder(savedOrder);
        return savedOrder;
    }

    public void delete(Order order) {
        orderRepository.delete(order);
    }


    public List<Order> findByPerson(Person person) {
        return orderRepository.findOrderByPerson(person);
    }

    public List<Order> findByDate(Date minDate, Date maxDate) {
        return orderRepository.findOrderByOrderDateBetween(minDate, maxDate);
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

}
