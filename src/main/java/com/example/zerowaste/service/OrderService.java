package com.example.zerowaste.service;

import com.example.zerowaste.model.Order;

public interface OrderService {

    Order createOrder(String buyerEmail);
}
