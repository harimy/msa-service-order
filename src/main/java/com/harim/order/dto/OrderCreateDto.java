package com.harim.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderCreateDto {

    private Long productId; // 상품 id
    private int count;      // 주문 개수
}
