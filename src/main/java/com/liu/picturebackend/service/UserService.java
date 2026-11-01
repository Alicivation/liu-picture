package com.liu.picturebackend.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.liu.picturebackend.model.VO.LoginUserVO;
import com.liu.picturebackend.model.VO.UserVO;
import com.liu.picturebackend.model.dto.user.UserLoginRequest;
import com.liu.picturebackend.model.dto.user.UserQueryRequest;
import com.liu.picturebackend.model.dto.user.UserRegisterRequest;
import com.liu.picturebackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author lenovo
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2026-04-09 09:19:08
 */
public interface UserService extends IService<User> {


    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册dto
     * @return long
     * 1.校验参数，2.检查用户账号是否与数据库冲突，3.加密密码，4.插入数据
     */
    long userRegister(UserRegisterRequest userRegisterRequest);

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param httpServletRequest
     * @return
     */
    LoginUserVO userLogin(UserLoginRequest userLoginRequest, HttpServletRequest httpServletRequest);

    /**
     * 用户退出登录
     *
     * @param httpServletRequest
     * @return
     */
    Boolean userLogout(HttpServletRequest httpServletRequest);

    /**
     * 获取当前登录用信息
     *
     * @param httpServletRequest
     * @return
     */
    User getLoginUser(HttpServletRequest httpServletRequest);

    /**
     * 获取查询条件
     *
     * @param request
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest request);

    /**
     * 获取脱敏后的用户列表
     *
     * @param userList
     * @return
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 加密密码
     *
     * @param password
     * @return
     */
    String getEncryptPassword(String password);

    /**
     * 转换userLoginVO
     *
     * @param user
     * @return
     */
    LoginUserVO getUserLoginVO(User user);

    /**
     * 转换userVO
     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);
}