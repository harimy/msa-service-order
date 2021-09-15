package com.harim.order.service;

import com.harim.order.domain.Order;
import com.harim.order.domain.OrderProduct;
import com.harim.order.dto.*;
import com.harim.order.exception.OrderNotFoundException;
import com.harim.order.exception.ProductNotFoundException;
import com.harim.order.exception.StockQuantityException;
import com.harim.order.repository.OrderProductRepository;
import com.harim.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;

    private final RestTemplate restTemplate;

    private static final String PRODUCT_SERVICE = "product-service";


    /**
     *
     * @param orderRequestDto 주문시 넘기는 정보 : 주문할 상품 이름, 개수
     * @return 주문 상품 내역(OrderProduct)
     */
    public OrderProductResponseDto save(OrderRequestDto orderRequestDto)
    {

        try
        {
            ProductDto product = searchProductById(orderRequestDto.getProductId());

            if(product.getStockQuantity()-orderRequestDto.getCount()<0)
                throw new StockQuantityException();

            OrderProduct orderProduct = OrderProduct.builder()
                    .productId(product.getId())
                    .orderPrice(orderRequestDto.getCount() * product.getPrice())
                    .count(orderRequestDto.getCount())
                    .build();

            // 재고 수정
            renewStockQuantity(product, product.getStockQuantity(), -orderRequestDto.getCount()); // 재고 갱신 (현재 재고 - 주문수량)

            // 주문
            Order order = Order.createOrder(orderProduct);
            orderProduct = orderProductRepository.save(orderProduct);

            // 주문 상품 정보
            OrderProductDto orderProductDto = new OrderProductDto(orderProduct.getProductId(), product.getName()
                    , orderProduct.getOrderPrice(), orderProduct.getCount());

            return OrderProductResponseDto.builder()
                    .id(orderProduct.getId())
                    .orderId(order.getId())
                    .orderProduct(orderProductDto)
                    .build();
        }
        catch (HttpStatusCodeException e)
        {
            throw new ProductNotFoundException();
        }
    }

    /**
     *
     * @param id 주문 id
     * @return 주문 상품 및 주문 상태 정보
     * @throws OrderNotFoundException
     */
    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(Long id)
    {
        // 주문 조회
        Order order = orderRepository.findById(id)
                .orElseThrow(OrderNotFoundException::new);

        List<OrderProductDto> orderProductsList = new ArrayList<>();
        for(OrderProduct orderProduct : order.getOrderProducts())
        {
            // 상품 조회
            ProductDto product = searchProductById(orderProduct.getProductId());

            // dto 에 담기
            OrderProductDto dto = new OrderProductDto(product.getId(), product.getName(), orderProduct.getOrderPrice(), orderProduct.getCount());

            // 리스트에 추가
            orderProductsList.add(dto);
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
                // 상품 조회
                ProductDto product = searchProductById(orderProduct.getProductId());

                // dto 에 담기
                OrderProductDto dto = new OrderProductDto(product.getId(), product.getName(), orderProduct.getOrderPrice(), orderProduct.getCount());

                // 주문 상품 리스트에 추가
                orderProductsList.add(dto);
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
     * 주문 수정
     * @param orderProductId 주문 상품 아이디
     * @param orderRequestDto 변경할 주문 상품 내용
     * @return 변경된 주문 상품 정보
     */
    public OrderProductResponseDto changeOrder(Long orderProductId, OrderRequestDto orderRequestDto)
    {
        OrderProduct orderProduct = orderProductRepository.findById(orderProductId)
                .orElseThrow(OrderNotFoundException::new);

        try
        {
            // 기존 주문 취소
            ProductDto product = searchProductById(orderProduct.getProductId());

            // 새 주문양이 재고를 초과할 시 예외 발생
            if(product.getStockQuantity()+orderProduct.getCount()-orderRequestDto.getCount()<0)
                throw new StockQuantityException();

            renewStockQuantity(product, product.getStockQuantity(), orderProduct.getCount()); // 재고 갱신 (현재 재고 + 기존 주문 수량)

            // 주문 수정 : 변경된 주문의 재고 반영
            product = searchProductById(orderRequestDto.getProductId());
            renewStockQuantity(product, product.getStockQuantity(), -orderRequestDto.getCount()); // 재고 갱신 (현재 재고 - 새 주문 수량)

            orderProduct.setProductId(product.getId());
            orderProduct.setOrderPrice(product.getPrice() * orderRequestDto.getCount());
            orderProduct.setCount(orderRequestDto.getCount());

            return OrderProductResponseDto.builder()
                    .id(orderProduct.getId())
                    .orderId(orderProduct.getOrder().getId())
                    .orderProduct(new OrderProductDto(product.getId(), product.getName()
                            , orderProduct.getOrderPrice()
                            , orderProduct.getCount()))
                    .build();
        }
        catch (HttpStatusCodeException e)
        {
            throw new ProductNotFoundException();
        }

    }

    // 주문 취소
    public ResponseDto deleteOrder(Long id)
    {
        Order order = orderRepository.findById(id)
                .orElseThrow(OrderNotFoundException::new);

        for (OrderProduct orderProduct : order.getOrderProducts())
        {
            ProductDto product = searchProductById(orderProduct.getProductId());
            renewStockQuantity(product, product.getStockQuantity(), orderProduct.getCount());

            orderProductRepository.delete(orderProduct);
        }

        orderRepository.delete(order);

        return new ResponseDto("주문이 정상적으로 취소되었습니다.(주문번호: " + id + ")");
    }

    // 중복 코드를 줄이기 위한 메소드 추출
    // 상품 조회 메소드
    private ProductDto searchProductById(Long productId)
    {
        String url = String.format("http://" + PRODUCT_SERVICE + "/api/product/%s", productId);
        ResponseEntity<ProductDto> responseData = restTemplate.exchange(url, HttpMethod.GET, null, ProductDto.class);
        ProductDto product = responseData.getBody();

        return product;
    }

    // 재고 갱신 메소드
    private void renewStockQuantity(ProductDto product, int original, int ordered)
    {
        String url = String.format("http://" + PRODUCT_SERVICE + "/api/product/%s", product.getId());
        product.setStockQuantity(original+ordered);
        HttpEntity<ProductDto> requestUpdate = new HttpEntity<>(product);
        restTemplate.exchange(url, HttpMethod.PUT, requestUpdate, ProductDto.class);
    }
    
}
