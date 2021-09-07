package com.harim.order.dto;

import com.harim.order.domain.Order;
import com.harim.order.domain.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class OrderProductResponseDto {

    private Long id;                        // 주문 내역 번호
    private Long orderId;                   // 주문 번호
    private OrderProductDto orderProduct;   // 주문 상품 정보

}
