package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin/report")
@Slf4j
@Api(tags = "报告接口")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/turnoverStatistics")
    @ApiOperation("营业额统计")
    public Result<TurnoverReportVO> getTurnoverStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate start, @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end) {
        log.info("营业额统计，{}，{}", start, end);
        TurnoverReportVO vo = reportService.getTurnoverStatistics(start,end);
        return Result.success(vo);
    }

    @GetMapping("/userStatistics")
    @ApiOperation("用户统计")
    public Result<UserReportVO> getUserStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate start, @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end) {
        log.info("用户统计，{}，{}", start, end);
        UserReportVO vo = reportService.getUserStatistics(start,end);
        return Result.success(vo);
    }

    @GetMapping("/OrdersStatistics")
    @ApiOperation("订单统计")
    public Result<OrderReportVO> getOrderStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate start, @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end) {
        log.info("订单统计，{}，{}", start, end);
        OrderReportVO vo = reportService.getOrderStatistics(start,end);
        return Result.success(vo);
    }

    @GetMapping("/top10")
    @ApiOperation("销量排名top10")
    public Result<SalesTop10ReportVO> getTop(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate start, @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end) {
        log.info("销量排名top10，{}，{}", start, end);
        SalesTop10ReportVO vo = reportService.getTop10(start,end);
        return Result.success(vo);
    }

    @GetMapping("/export")
    @ApiOperation("导出运营数据报表")
    public void export(HttpServletResponse response){
        reportService.exportBusinessData(response);
    }
}
