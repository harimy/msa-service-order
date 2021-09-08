package com.harim.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderRequestDto {

    private Long productId; // 상품 id
    private int count;      // 주문 개수
}
