package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO dto) {

        //处理各种业务异常（地址本为空，购物车数据为空）
        Long addressBookId = dto.getAddressBookId();
        AddressBook ad = addressBookMapper.getById(addressBookId);
        if (addressBookId == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder().userId(userId).build();
        List<ShoppingCart> list = shoppingCartMapper.getShoppingCartList(shoppingCart);
        if(list == null || list.size() == 0) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //为订单表插入一条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(dto, orders);
        orders.setUserId(userId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setPayStatus(Orders.UN_PAID);
        orders.setConsignee(ad.getConsignee());//收货人
        orders.setPhone(ad.getPhone());
        orders.setNumber(String.valueOf(System.currentTimeMillis()));

        orderMapper.insert(orders);



        //向订单详情表插入若干条数据
        List<OrderDetail> odlist = new ArrayList<>();
        for (ShoppingCart cart : list) {
            OrderDetail od = new OrderDetail();
            BeanUtils.copyProperties(cart, od);
            od.setOrderId(orders.getId());
            odlist.add(od);
        }
        orderDetailMapper.insertBatch(odlist);



        //清空购物车
        shoppingCartMapper.deleteAll(userId);

        //将vo传出
        OrderSubmitVO vo = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderNumber(orders.getNumber())
                .build();

        return vo;
    }
}
