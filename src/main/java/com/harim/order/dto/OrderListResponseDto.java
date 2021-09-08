package com.harim.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderListResponseDto<T> {
    private T data;
}
