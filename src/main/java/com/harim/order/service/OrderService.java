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
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;

    private final RestTemplate restTemplate;

    /**
     *
     * @param orderRequestDto 주문시 넘기는 정보 : 주문할 상품 이름, 개수
     * @return 주문 상품 내역(OrderProduct)
     */
    public OrderProductResponseDto save(OrderRequestDto orderRequestDto)
    {
        // Rest Template 작업
        String url = String.format("http://localhost:8080/api/product/%s", orderRequestDto.getProductId());

        ResponseEntity<ProductDto> responseData = restTemplate.exchange(url, HttpMethod.GET, null, ProductDto.class);
        ProductDto product = responseData.getBody();

        // 주문
        OrderProduct orderProduct = OrderProduct.builder()
                .productId(product.getId())
                .orderPrice(orderRequestDto.getCount() * product.getPrice())
                .count(orderRequestDto.getCount())
                .build();
        Order order = Order.createOrder(orderProduct);
        orderProduct = orderProductRepository.save(orderProduct);

        // 재고 수정
        String productUpdateUrl = String.format("http://localhost:8080/api/product/%s", orderRequestDto.getProductId());

        product.setStockQuantity(product.getStockQuantity()-orderRequestDto.getCount()); // 재고 갱신 (현재 재고 - 주문수량)
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
    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(Long id)
    {
        // 주문 조회
        Order order = orderRepository.findById(id)
                .orElseThrow(RuntimeException::new);

        List<OrderProductDto> orderProductsList = new ArrayList<>();
        for(OrderProduct orderProduct : order.getOrderProducts())
        {
            // 상품명 조회
            String url = String.format("http://localhost:8080/api/product/%s", orderProduct.getProductId());
            ResponseEntity<OrderProductDto> responseData = restTemplate.exchange(url, HttpMethod.GET, null, OrderProductDto.class);
            OrderProductDto product = responseData.getBody();

            // 주문 가격 및 수량
            product.setOrderPrice(orderProduct.getOrderPrice());
            product.setCount(orderProduct.getCount());

            // 리스트에 추가
            orderProductsList.add(product);
        }

        return OrderResponseDto.builder()
                .orderId(order.getId())
                .orderProducts(orderProductsList)
                .orderDate(order.getOrderDate())
                .orderStatus(order.getStatus())
                .build();
    }

    /**
     * 주문 목록 전체 조회
     * @return 주문 목록 리스트 반환
     */
    public OrderListResponseDto getOrders()
    {
        List<Order> orders = orderRepository.findAll();
        List<OrderResponseDto> ordersList = new ArrayList<>();  // 주문 리스트
        for(int i=0; i<orders.size(); i++)
        {
            List<OrderProductDto> orderProductsList = new ArrayList<>();    // 주문 상품 리스트

            for(OrderProduct orderProduct : orders.get(i).getOrderProducts())
            {
                // 상품명 조회
                String url = String.format("http://localhost:8080/api/product/%s", orderProduct.getProductId());
                ResponseEntity<OrderProductDto> responseData = restTemplate.exchange(url, HttpMethod.GET, null, OrderProductDto.class);
                OrderProductDto product = responseData.getBody();

                // 주문 가격 및 수량
                product.setOrderPrice(orderProduct.getOrderPrice());
                product.setCount(orderProduct.getCount());

                // 주문 상품 리스트에 추가
                orderProductsList.add(product);
            }

            OrderResponseDto orderResponseDto = new OrderResponseDto(orders.get(i).getId()
                    , orderProductsList
                    , orders.get(i).getOrderDate()
                    , orders.get(i).getStatus());

            ordersList.add(orderResponseDto);

        }

        return new OrderListResponseDto(ordersList);
    }

    /**
     *
     * @param orderProductId 주문 상품 아이디
     * @param orderRequestDto 변경할 주문 상품 내용
     * @return 변경된 주문 상품 정보
     */
    public OrderProductResponseDto changeOrder(Long orderProductId, OrderRequestDto orderRequestDto)
    {
        System.out.println("requestDto : " + orderRequestDto.getProductId());
        OrderProduct orderProduct = orderProductRepository.findById(orderProductId)
                .orElseThrow(RuntimeException::new);

        System.out.println("주문금액: " + orderProduct.getOrderPrice());

        // 기존 재고 원복
        String url = String.format("http://localhost:8080/api/product/%s", orderProduct.getProductId());
        ResponseEntity<ProductDto> responseData = restTemplate.exchange(url, HttpMethod.GET, null, ProductDto.class);
        ProductDto product = responseData.getBody();

        product.setStockQuantity(product.getStockQuantity()+orderProduct.getCount()); // 재고 갱신 (현재 재고 + 기존 주문 수량)
        HttpEntity<ProductDto> requestUpdate = new HttpEntity<>(product);
        restTemplate.exchange(url, HttpMethod.PUT, requestUpdate, ProductDto.class);

        // 주문
        url = String.format("http://localhost:8080/api/product/%s", orderRequestDto.getProductId());
        responseData = restTemplate.exchange(url, HttpMethod.GET, null, ProductDto.class);
        product = responseData.getBody();

        orderProduct.setProductId(product.getId());
        orderProduct.setOrderPrice(product.getPrice() * orderRequestDto.getCount());
        orderProduct.setCount(orderRequestDto.getCount());

        // 변경된 주문의 재고 반영
        product.setStockQuantity(product.getStockQuantity()-orderRequestDto.getCount());    // 재고 갱신 (현재 재고 - 새 주문 수량)
        requestUpdate = new HttpEntity<>(product);
        restTemplate.exchange(url, HttpMethod.PUT, requestUpdate, ProductDto.class);

        return OrderProductResponseDto.builder()
                .id(orderProduct.getId())
                .orderId(orderProduct.getOrder().getId())
                .orderProduct(new OrderProductDto(product.getId(), product.getName()
                        , orderProduct.getOrderPrice()
                        , orderProduct.getCount()))
                .build();
    }

    // 주문 취소
    public ResponseDto deleteOrder(Long id)
    {
        Order order = orderRepository.findById(id)
                .orElseThrow(RuntimeException::new);

        for (OrderProduct orderProduct : order.getOrderProducts())
        {
            // 재고 원복
            String url = String.format("http://localhost:8080/api/product/%s", orderProduct.getProductId());
            ResponseEntity<ProductDto> responseData = restTemplate.exchange(url, HttpMethod.GET, null, ProductDto.class);
            ProductDto product = responseData.getBody();

            product.setStockQuantity(product.getStockQuantity() + orderProduct.getCount()); // 재고 갱신 (현재 재고 + 기존 주문 수량)
            HttpEntity<ProductDto> requestUpdate = new HttpEntity<>(product);
            restTemplate.exchange(url, HttpMethod.PUT, requestUpdate, ProductDto.class);

            orderProductRepository.delete(orderProduct);

        }

        orderRepository.delete(order);

        return new ResponseDto("주문이 정상적으로 취소되었습니다.(주문번호: " + id + ")");
    }

}
