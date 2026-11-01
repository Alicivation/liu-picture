package com.liu.picturebackend.aop;

import com.liu.picturebackend.annotation.AuthCheck;
import com.liu.picturebackend.exception.BusinessException;
import com.liu.picturebackend.exception.ErrorCode;
import com.liu.picturebackend.model.entity.User;
import com.liu.picturebackend.model.enums.UserRoleEnum;
import com.liu.picturebackend.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    /**
     * 执行拦截
     *
     * @param point     切点
     * @param authCheck 权限值
     * @return
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint point, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        RequestAttributes attributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();
        //获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        if (mustRoleEnum == null) {
            return point.proceed();
        }
        //有权限才能够通过的方法
        UserRoleEnum userRole = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        if (userRole == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "权限不足");
        }
        //必须是管理员权限才能执行
        if (UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(userRole)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "权限不足");
        }
        return point.proceed();
    }
}
