package com.harim.order.controller;

import com.harim.order.dto.*;
import com.harim.order.exception.OrderNotFoundException;
import com.harim.order.exception.ProductNotFoundException;
import com.harim.order.exception.StockQuantityException;
import com.harim.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ProductNotFoundException> handleNotFoundProduct(){
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ProductNotFoundException("상품이 존재하지 않습니다."));
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<OrderNotFoundException> handleNotFoundOrder(){
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new OrderNotFoundException("주문이 존재하지 않습니다."));
    }

    @ExceptionHandler(StockQuantityException.class)
    public ResponseEntity<StockQuantityException> handleNotEnoughStockQuantity(){
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new StockQuantityException("상품의 재고 수량이 충분하지 않습니다."));
    }

}
