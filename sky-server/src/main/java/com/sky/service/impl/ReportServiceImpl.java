package com.sky.service.impl;

import com.github.xiaoymin.knife4j.core.util.StrUtil;
import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.Inet4Address;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

    public TurnoverReportVO getTurnoverStatistics(LocalDate start, LocalDate end) {
        List<LocalDate> listOne = new ArrayList<>();
        List<Double> listTwo = new ArrayList<>();

        listOne.add(start);

        while(!start.isEqual(end)){
            start.plusDays(1);
            listOne.add(start);
        }

        for(LocalDate date:listOne){
            LocalDateTime sstart = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime eend = LocalDateTime.of(date, LocalTime.MAX);
            HashMap map = new HashMap();
            map.put("begin", sstart);
            map.put("end", eend);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            turnover = turnover == null ? 0.0 : turnover;
            listTwo.add(turnover);


        }
        return  TurnoverReportVO
                .builder()
                .dateList(StringUtil.join(",", listOne))
                .turnoverList(StringUtil.join(",", listTwo))
                .build();

    }

    /**
     *统计用户
     * @param start
     * @param end
     * @return
     */
    public UserReportVO getUserStatistics(LocalDate start, LocalDate end) {
        List<LocalDate> dataList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();
        List<Integer> newUserList = new ArrayList<>();

        dataList.add(start);

        while(!start.isEqual(end)){
            start.plusDays(1);
            dataList.add(start);
        }

        //统计每日新增的用户数据 crete_time>? and create_time <?
        for(LocalDate date:dataList){
            LocalDateTime sstart = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime eend = LocalDateTime.of(date, LocalTime.MAX);
            HashMap map = new HashMap();
            map.put("begin", sstart);
            map.put("end", eend);
            Integer newUsers = userMapper.countUsersByMap(map);
            newUsers = newUsers == null ? 0 : newUsers;
            newUserList.add(newUsers);
        }

        //统计每日总的用户数据 create_time < ?
        for (LocalDate date:dataList){
            LocalDateTime eend = LocalDateTime.of(date, LocalTime.MAX);
            HashMap map = new HashMap();
            map.put("end", eend);
            Integer totalUsers = userMapper.countUsersByMap(map);
            totalUsers = totalUsers == null ? 0 : totalUsers;
            totalUserList.add(totalUsers);
        }

        return UserReportVO.builder()
                .dateList(StringUtil.join(",",dataList))
                .newUserList(StringUtil.join(",",newUserList))
                .totalUserList(StringUtil.join(",",totalUserList))
                .build();
    }

    /**
     * 统计订单
     * @param start
     * @param end
     * @return
     */
    public OrderReportVO getOrderStatistics(LocalDate start, LocalDate end) {

        List<LocalDate> dateList = new ArrayList<>();
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        //1.datalist
        dateList.add(start);

        while(!start.isEqual(end)){
            start.plusDays(1);
            dateList.add(start);
        }
        //2.orderCountList每日订单数
        for(LocalDate date:dateList){
            LocalDateTime sstart = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime eend = LocalDateTime.of(date, LocalTime.MAX);
            HashMap mapOne = new HashMap();
            mapOne.put("begin", sstart);
            mapOne.put("end", eend);
            Integer count = orderMapper.countByMap(mapOne);
            orderCountList.add(count);


            //3.每日有效订单数量validOrderCountList
            mapOne.put("status", Orders.COMPLETED);
            Integer validCount = orderMapper.countByMap(mapOne);
            validOrderCountList.add(validCount);
        }

        //4,订单总数
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        //5.订单有效总数
        Integer totalValidOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();
        //6.订单完成率，5/4
        Double rate = 0.0;
        if (totalValidOrderCount!=0){
            rate = totalValidOrderCount.doubleValue()/totalOrderCount;
        }

        return OrderReportVO.builder()
                .dateList(StringUtil.join(",",dateList))
                .orderCompletionRate(rate)
                .orderCountList(StringUtil.join(",",orderCountList))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(totalValidOrderCount)
                .validOrderCountList(StringUtil.join(",",validOrderCountList))
                .build();

    }

    /**
     * 销量统计
     * @param start
     * @param end
     * @return
     */
    public SalesTop10ReportVO getTop10(LocalDate start, LocalDate end) {
        LocalDateTime sstart = LocalDateTime.of(start, LocalTime.MIN);
        LocalDateTime eend = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> top10 = orderMapper.getTop10(sstart,eend);
        List<String> names = top10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String nameList = StringUtil.join(",", names);

        List<Integer> numbers = top10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String numberList = StringUtil.join(",", numbers);

        return SalesTop10ReportVO.builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();

    }
}
