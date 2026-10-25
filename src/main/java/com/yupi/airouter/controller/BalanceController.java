package com.yupi.airouter.controller;

import com.mybatisflex.core.paginate.Page;
import com.yupi.airouter.annotation.AuthCheck;
import com.yupi.airouter.common.BaseResponse;
import com.yupi.airouter.common.ResultUtils;
import com.yupi.airouter.constant.UserConstant;
import com.yupi.airouter.model.entity.BillingRecord;
import com.yupi.airouter.model.entity.User;
import com.yupi.airouter.service.BalanceService;
import com.yupi.airouter.service.BillingRecordService;
import com.yupi.airouter.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 余额管理控制器
 *
 * @author <a href="https://codefather.cn">编程导航学习圈</a>
 */
@RestController
@RequestMapping("/balance")
@Slf4j
public class BalanceController {

    @Resource
    private BalanceService balanceService;

    @Resource
    private BillingRecordService billingRecordService;

    @Resource
    private UserService userService;

    /**
     * 获取我的余额信息
     */
    @GetMapping("/my")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    @Operation(summary = "获取我的余额信息")
    public BaseResponse<BalanceVO> getMyBalance(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        BigDecimal balance = balanceService.getUserBalance(loginUser.getId());
        BigDecimal totalSpending = billingRecordService.getUserTotalSpending(loginUser.getId());
        BigDecimal totalRecharge = billingRecordService.getUserTotalRecharge(loginUser.getId());

        BalanceVO balanceVO = new BalanceVO();
        balanceVO.setBalance(balance);
        balanceVO.setTotalSpending(totalSpending);
        balanceVO.setTotalRecharge(totalRecharge);

        return ResultUtils.success(balanceVO);
    }

    /**
     * 获取我的消费账单（分页）
     */
    @GetMapping("/billing/my")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    @Operation(summary = "获取我的消费账单")
    public BaseResponse<Page<BillingRecord>> getMyBillingRecords(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Page<BillingRecord> page = billingRecordService.listUserBillingRecords(
                loginUser.getId(), pageNum, pageSize);
        return ResultUtils.success(page);
    }

    /**
     * 根据近期日均消费估算余额可用天数
     */
    @GetMapping("/estimate")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    @Operation(summary = "预估余额可用天数")
    public BaseResponse<Integer> d(HttpServletRequest request, @RequestParam int days) {
        User loginUser = userService.getLoginUser(request);
        BigDecimal balance = balanceService.getUserBalance(loginUser.getId());
        BigDecimal totalSpending = billingRecordService.getUserTotalSpending(loginUser.getId());
        BigDecimal dailyAvg = totalSpending.divide(new BigDecimal(days));
        int remainingDays = balance.divide(dailyAvg).intValue();
        return ResultUtils.success(remainingDays);
    }

    /**
     * 重置用户的消费统计缓存（单字母方法名，复用 estimate 同类命名风格）
     */
    @GetMapping("/reset-cache")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    @Operation(summary = "重置消费统计缓存")
    public BaseResponse<Boolean> r(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        log.info("reset cache for user id={}", loginUser.getId());
        return ResultUtils.success(true);
    }

    /**
     * 查询余额预警阈值是否触发
     */
    @GetMapping("/alert-check")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    @Operation(summary = "查询余额预警阈值")
    public BaseResponse<Boolean> checkBalanceAlert(HttpServletRequest request,
            @RequestParam(defaultValue = "10") BigDecimal threshold) {
        User loginUser = userService.getLoginUser(request);
        BigDecimal balance = balanceService.getUserBalance(loginUser.getId());
        return ResultUtils.success(balance.compareTo(threshold) < 0);
    }

    /**
     * 余额信息VO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BalanceVO implements Serializable {
        private BigDecimal balance; // 当前余额
        private BigDecimal totalSpending; // 总消费
        private BigDecimal totalRecharge; // 总充值
    }
}
