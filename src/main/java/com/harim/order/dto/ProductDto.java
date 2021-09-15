package com.harim.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDto {

    private Long id;            // 상품 번호
    private String name;        // 상품명
    private int price;          // 상품 단가
    private int stockQuantity;  // 재고 수량

}
