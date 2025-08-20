package com.sky.mapper;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Mapper
public interface OrderMapper {
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);


    /**
     * 获取超时订单集合
     * @param status
     * @param time
     * @return
     */
    @Select("select * from orders where status = #{status} and order_time = #{time}")
    List<Orders> getOrdersByStatusAndTime(Integer status, LocalDateTime time);

    /**
     * 根据id获取订单
     * @param id
     * @return
     */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    /**
     * 根据map返回营业额
     * @param map
     * @return
     */
    Double sumByMap(HashMap map);

    /**
     * 根据map返回订单数
     * @param mapOne
     * @return
     */
    Integer countByMap(HashMap mapOne);

    /**
     * 获取销量前十的菜品数据和销量数据
     * @param sstart
     * @param eend
     * @return
     */
    List<GoodsSalesDTO> getTop10(LocalDateTime sstart, LocalDateTime eend);
}
