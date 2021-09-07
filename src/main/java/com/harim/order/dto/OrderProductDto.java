package com.harim.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderProductDto {

    private Long id;            // 주문한 상품번호
    private String name;        // 주문한 상품명
    private int orderPrice;     // 주문 가격(상품 가격 * 주문 개수)
    private int count;          // 주문 개수
}
