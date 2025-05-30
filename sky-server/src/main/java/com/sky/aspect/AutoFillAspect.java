package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {}

    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("开始进行公共字段自动填充...");

        // 1. 获取操作类型注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        OperationType operationType = autoFill.value();

        // 2. 获取实体对象参数
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) { // 修复判断逻辑
            log.warn("未找到实体对象参数，自动填充终止");
            return;
        }
        Object entity = args[0];

        // 3. 准备填充数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        try {
            // 4. 根据操作类型反射赋值
            if (OperationType.INSERT == operationType) {
                invokeSetter(entity, "setCreateTime", now);
                invokeSetter(entity, "setCreateUser", currentId);
                invokeSetter(entity, "setUpdateTime", now);
                invokeSetter(entity, "setUpdateUser", currentId);
            } else if (OperationType.UPDATE == operationType) {
                invokeSetter(entity, "setUpdateTime", now);
                invokeSetter(entity, "setUpdateUser", currentId);
            }
            log.info("公共字段填充成功 | 操作类型: {}", operationType);
        } catch (Exception e) {
            log.error("公共字段自动填充失败: {}", e.getMessage());
        }
    }

    /**
     * 通过反射调用实体类的setter方法
     * @param entity 目标对象
     * @param methodName 方法名
     * @param value 参数值
     */
    private void invokeSetter(Object entity, String methodName, Object value)
            throws Exception {
        Class<?> entityClass = entity.getClass();
        // 获取参数类型（支持基本类型自动装箱）
        Class<?> paramType = value instanceof Long ? long.class : value.getClass();
        // 获取方法对象并调用[1,3](@ref)
        Method method = entityClass.getDeclaredMethod(methodName, paramType);
        method.setAccessible(true);  // 允许访问私有方法[1,4](@ref)
        method.invoke(entity, value); // 执行方法调用[2,6](@ref)
    }
}