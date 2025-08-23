package com.sky.service.impl;

import com.github.xiaoymin.knife4j.core.util.StrUtil;
import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.apache.poi.util.StringUtil;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
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
    @Autowired
    private WorkspaceService workspaceService;

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

    /**
     * 导出运营数据报表
     * @param response
     */
    public void exportBusinessData(HttpServletResponse response) {
        //1. 查询数据库，获取营业数据---查询最近30天的运营数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);

        //查询概览数据
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(dateBegin, LocalTime.MIN), LocalDateTime.of(dateEnd, LocalTime.MAX));

        //2. 通过POI将数据写入到Excel文件中
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");

        try {
            //基于模板文件创建一个新的Excel文件
            XSSFWorkbook excel = new XSSFWorkbook(in);

            //获取表格文件的Sheet页
            XSSFSheet sheet = excel.getSheet("Sheet1");

            //填充数据--时间
            sheet.getRow(1).getCell(1).setCellValue("时间：" + dateBegin + "至" + dateEnd);

            //获得第4行
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());

            //获得第5行
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

            //填充明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = dateBegin.plusDays(i);
                //查询某一天的营业数据
                BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));

                //获得某一行
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }

            //3. 通过输出流将Excel文件下载到客户端浏览器
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            //关闭资源
            out.close();
            excel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
