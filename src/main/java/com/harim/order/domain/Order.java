package com.harim.order.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="orders")
@Getter @Setter
public class Order {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="order_id")
    private Long id;

    @OneToMany(mappedBy = "order")
    private List<OrderProduct> orderProducts = new ArrayList<>();

    private LocalDateTime orderDate; // 주문시간

    @Enumerated(EnumType.STRING)
    private OrderStatus status; // 주문상태 [ORDER, CANCEL]

    // 양방향 연관관계에서 필요함
    // 연관관계 편입 메소드
    public void addOrderProduct(OrderProduct orderProduct)
    {
        orderProducts.add(orderProduct);
        orderProduct.setOrder(this);
    }


    //== 생성 메서드 ==//
    public static Order createOrder(OrderProduct... orderProducts)
    {
        Order order = new Order();
        for (OrderProduct orderProduct : orderProducts)
        {
            order.addOrderProduct(orderProduct);
        }
        order.setStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());

        return order;
    }


}
