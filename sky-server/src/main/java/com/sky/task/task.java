package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class task {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理超时订单
     */
    @Scheduled(cron = "0 * * * * ?")
    //@Scheduled(cron = "0/5 * * * * ?")//测试
    public void processTimeoutOrder(){
        log.info("定时处理超时订单：{}", LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);

        List<Orders> list = orderMapper.getOrdersByStatusAndTime(Orders.PENDING_PAYMENT,time);

        if (list!=null&&list.size()>0){
            for (Orders o:list){
                o.setStatus(Orders.CANCELLED);
                o.setCancelReason("订单超时未支付，自动取消");
                o.setCancelTime(LocalDateTime.now());
                orderMapper.update(o);
            }
        }
    }

    /**
     * 处理一些异常订单（持续派送中）
     */
    @Scheduled(cron = "0 0 1 * * ?")
    //@Scheduled(cron = "0/5 * * * * ?")//测试
    public void processDeliveryOrder(){
        log.info("定时处理异常订单（持续派送中）的订单{}", LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);

        List<Orders> list = orderMapper.getOrdersByStatusAndTime(Orders.DELIVERY_IN_PROGRESS,time);
        if (list!=null&&list.size()>0){
            for (Orders o:list){
                o.setStatus(Orders.COMPLETED);

                orderMapper.update(o);
            }
        }

    }
}
