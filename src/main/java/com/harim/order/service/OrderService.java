package com.harim.order.service;

import com.harim.order.domain.Order;
import com.harim.order.domain.OrderProduct;
import com.harim.order.dto.*;
import com.harim.order.repository.OrderProductRepository;
import com.harim.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;

    private final RestTemplate restTemplate;

    /**
     *
     * @param orderCreateDto 주문시 넘기는 정보 : 주문할 상품 이름, 개수
     * @return 주문 상품 내역(OrderProduct)
     */
    public OrderProductResponseDto save(OrderCreateDto orderCreateDto)
    {
        // Rest Template 작업
        String url = String.format("http://localhost:8080/api/product/%s", orderCreateDto.getProductId());

        ResponseEntity<ProductDto> responseData = restTemplate.exchange(url, HttpMethod.GET, null, ProductDto.class);
        ProductDto product = responseData.getBody();

        // 주문
        OrderProduct orderProduct = OrderProduct.builder()
                .productId(product.getId())
                .orderPrice(orderCreateDto.getCount() * product.getPrice())
                .count(orderCreateDto.getCount())
                .build();
        Order order = Order.createOrder(orderProduct);
        orderProduct = orderProductRepository.save(orderProduct);

        // 재고 수정
        String productUpdateUrl = String.format("http://localhost:8080/api/product/%s", orderCreateDto.getProductId());

        product.setStockQuantity(product.getStockQuantity()-orderCreateDto.getCount()); // 재고 갱신 (현재 재고 - 주문수량)
        HttpEntity<ProductDto> requestUpdate = new HttpEntity<>(product);
        restTemplate.exchange(productUpdateUrl, HttpMethod.PUT, requestUpdate, ProductDto.class);

        // 주문 상품 정보
        OrderProductDto orderProductDto = new OrderProductDto(orderProduct.getProductId(), product.getName()
                , orderProduct.getOrderPrice(), orderProduct.getCount());

        return OrderProductResponseDto.builder()
                .id(orderProduct.getId())
                .orderId(order.getId())
                .orderProduct(orderProductDto)
                .build();
    }

    /**
     *
     * @param id 주문 id
     * @return 주문 상품 및 주문 상태 정보
     * @throws RuntimeException
     */
    public OrderResponseDto getOrderById(Long id)
    {
        // 주문 조회
        Order order = orderRepository.findById(id)
                .orElseThrow(RuntimeException::new);

        List<OrderProductDto> orderProducts = new ArrayList<>();
        for(OrderProduct orderProduct : order.getOrderProducts())
        {
            String url = String.format("http://localhost:8080/api/product/%s", orderProduct.getProductId());
            ResponseEntity<OrderProductDto> responseData = restTemplate.exchange(url, HttpMethod.GET, null, OrderProductDto.class);
            OrderProductDto product = responseData.getBody();
            orderProducts.add(product);
        }

        return OrderResponseDto.builder()
                .orderId(order.getId())
                .orderProducts(orderProducts)
                .orderDate(order.getOrderDate())
                .orderStatus(order.getStatus())
                .build();
    }

    // 주문 수정

    // 주문 취소
}
