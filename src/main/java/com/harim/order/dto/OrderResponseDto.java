package com.harim.order.dto;

import com.harim.order.domain.OrderProduct;
import com.harim.order.domain.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class OrderResponseDto {

    private Long orderId;                           // 주문 번호
    private List<OrderProductDto> orderProducts;    // 주문 상품
    private LocalDateTime orderDate;                // 주문 일자
    private OrderStatus orderStatus;                // 주문 상태

}
