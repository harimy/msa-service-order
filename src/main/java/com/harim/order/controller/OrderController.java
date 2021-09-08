package com.harim.order.controller;

import com.harim.order.dto.OrderCreateDto;
import com.harim.order.dto.OrderListResponseDto;
import com.harim.order.dto.OrderProductResponseDto;
import com.harim.order.dto.OrderResponseDto;
import com.harim.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;


    @PostMapping("order")
    public OrderProductResponseDto createOrder(@RequestBody OrderCreateDto orderCreateDto)
    {
        return orderService.save(orderCreateDto);
    }

    @GetMapping("/order/{id}")
    public OrderResponseDto getOrder(@PathVariable("id") Long id)
    {
        return orderService.getOrderById(id);
    }

    @GetMapping("/orders")
    public OrderListResponseDto getOrders()
    {
        return orderService.getOrders();
    }



}
