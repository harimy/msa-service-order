package com.harim.order.controller;

import com.harim.order.dto.*;
import com.harim.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;


    @PostMapping("order")
    public OrderProductResponseDto createOrder(@RequestBody OrderRequestDto orderRequestDto)
    {
        return orderService.save(orderRequestDto);
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

    @PutMapping("/order/{id}")
    public OrderProductResponseDto changeOrder(@PathVariable("id") Long id, @RequestBody @Valid OrderRequestDto orderRequestDto)
    {
        return orderService.changeOrder(id, orderRequestDto);
    }

    @DeleteMapping("/order/{id}")
    public ResponseDto deleteOrder(@PathVariable("id") Long id)
    {
        return orderService.deleteOrder(id);
    }

}
