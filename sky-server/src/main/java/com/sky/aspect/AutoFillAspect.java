package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * @Description:
 * @Author: Ian
 * @Date: 2023/9/5 21:50
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    /**
     * 所有的insert 语句和 update语句的切点,
     * && @annotation 表示必须要有 com.sky.annotation.AutoFill 这个注解才会被切入
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){}


    /**
     * 对所有的insert update语句的create_time create_user... 进行统一的填装
     * 公共代码抽取出来做成切面
     * @param joinPoint
     */
    @Before(value = "autoFillPointCut()")
    public void autoFillInsertAndUpdate(JoinPoint joinPoint){

        // 获取方法的签名,获取方法上AutoFill注解的value值 判断是insert 还是 Update
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        Method method = signature.getMethod();

        log.info("自动填充切面执行!!!! 需要切面的方法 {}",method.getName());

        AutoFill autoFill = method.getAnnotation(AutoFill.class);

        OperationType value = autoFill.value();
        log.info("自动填充切面执行!!!! 需要切面的方法的执行类型 {}",value);

        // 获取方法上的参数
        Object[] args = joinPoint.getArgs();


        if (args[0] == null || args.length == 0){
            return;
        }

        // 约定好,如果要执行 Insert or update ,实体类必须放到参数的第一个
        Object arg = args[0];

        // 准备要插入的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();


        if (value == OperationType.INSERT){
            try {

                // 使用反射机制,获得方法对象并执行
                Method setCreateTime = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                setCreateTime.invoke(arg,now);
                setCreateUser.invoke(arg,currentId);
                setUpdateTime.invoke(arg,now);
                setUpdateUser.invoke(arg,currentId);

                log.info("以对{}类型的方法自动装填了 修改日期 修改人,创建日期,创建人",value);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }else if (value == OperationType.UPDATE){
            try {

                Method setUpdateTime = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                setUpdateTime.invoke(arg,now);
                setUpdateUser.invoke(arg,currentId);
                log.info("以对{}类型的方法自动装填了 修改日期 修改人",value);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }



    }
}
