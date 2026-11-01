package com.liu.picturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liu.picturebackend.constant.UserConstant;
import com.liu.picturebackend.exception.BusinessException;
import com.liu.picturebackend.exception.ErrorCode;
import com.liu.picturebackend.model.VO.LoginUserVO;
import com.liu.picturebackend.model.VO.UserVO;
import com.liu.picturebackend.model.dto.user.UserLoginRequest;
import com.liu.picturebackend.model.dto.user.UserQueryRequest;
import com.liu.picturebackend.model.dto.user.UserRegisterRequest;
import com.liu.picturebackend.model.entity.User;
import com.liu.picturebackend.model.enums.UserRoleEnum;
import com.liu.picturebackend.service.UserService;
import com.liu.picturebackend.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lenovo
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2026-04-09 09:19:08
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param httpServletRequest
     * @return
     */
    @Override
    public LoginUserVO userLogin(UserLoginRequest userLoginRequest, HttpServletRequest httpServletRequest) {
        //1.校验
        if (StrUtil.hasBlank(userLoginRequest.getUserAccount(),
                userLoginRequest.getUserPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userLoginRequest.getUserAccount().length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号过短");

        }
        if (userLoginRequest.getUserPassword().length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度过短");
        }
        //2.对密码加密
        String encryptPassword = getEncryptPassword(userLoginRequest.getUserPassword());
        //3.查找数据库是否存在，不存在则输出异常
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("userAccount", userLoginRequest.getUserAccount());
        userQueryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(userQueryWrapper);
        if (user == null) {
            log.info("user login field, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或者密码错误");
        }
        //4.保存用户登录态
        httpServletRequest.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);

        return this.getUserLoginVO(user);
    }

    /**
     * 获取当前登录用户
     *
     * @param httpServletRequest
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest httpServletRequest) {
        Object userObj = httpServletRequest.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        Long id = currentUser.getId();
        currentUser = this.getById(id);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * 获取查询条件
     *
     * @param request
     * @return
     */
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = request.getId();
        String userName = request.getUserName();
        String userAccount = request.getUserAccount();
        String userProfile = request.getUserProfile();
        String userRole = request.getUserRole();
        int current = request.getCurrent();
        int pageSize = request.getPageSize();
        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq(ObjUtil.isNotEmpty(id),"id",id);
        userQueryWrapper.eq(ObjUtil.isNotEmpty(userRole),"userRole",userRole);
        userQueryWrapper.like(ObjUtil.isNotEmpty(userAccount),"userAccount",userAccount);
        userQueryWrapper.like(ObjUtil.isNotEmpty(userName),"userName",userName);
        userQueryWrapper.like(ObjUtil.isNotEmpty(userProfile),"userProfile",userProfile);
        userQueryWrapper.orderBy(ObjUtil.isNotEmpty(sortField),sortOrder.equals("ascend"),sortField);
        return userQueryWrapper;
    }

    /**
     * 获取脱敏后的用户列表
     *
     * @param userList
     * @return
     */
    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (userList == null) {
            return new ArrayList<>();
        }
        return userList.stream()
                .map(this::getUserVO)
                .collect(Collectors.toList());
    }

    /**
     * 用户登录注销
     *
     * @param httpServletRequest
     * @return
     */
    @Override
    public Boolean userLogout(HttpServletRequest httpServletRequest) {
        Object userObj = httpServletRequest.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        //移除登录态
        httpServletRequest.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册dto
     * @return long
     * 1.校验参数，2.检查用户账号是否与数据库冲突，3.加密密码，4.插入数据
     */
    @Override
    public long userRegister(UserRegisterRequest userRegisterRequest) {
        //校验参数
        if (StrUtil.hasBlank(userRegisterRequest.getUserAccount(),
                userRegisterRequest.getUserPassword(),
                userRegisterRequest.getUserCheckPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userRegisterRequest.getUserAccount().length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号过短");

        }
        if (userRegisterRequest.getUserPassword().length() < 8 || userRegisterRequest.getUserCheckPassword().length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度过短");
        }
        if (!userRegisterRequest.getUserPassword().equals(userRegisterRequest.getUserCheckPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次密码输入不一致");
        }
        //检查数据库是否重复
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("userAccount", userRegisterRequest.getUserAccount());
        Long count = this.baseMapper.selectCount(userQueryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        //加密
        String encryptPassword = getEncryptPassword(userRegisterRequest.getUserPassword());
        //插入数据
        User user = new User();
        user.setUserAccount(userRegisterRequest.getUserAccount());
        user.setUserPassword(encryptPassword);
        user.setUserName("无名");
        user.setUserRole(UserRoleEnum.USER.getValue());
        if (!this.save(user)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库异常错误");
        }
        return user.getId();
    }

    /**
     * 加密密码
     *
     * @param password
     * @return
     */
    @Override
    public String getEncryptPassword(String password) {
        //加盐
        final String salt = "liu";
        return DigestUtils.md5DigestAsHex((salt + password).getBytes());
    }

    /**
     * 转换用户登录VO
     *
     * @param user
     * @return
     */
    @Override
    public LoginUserVO getUserLoginVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO userLoginVO = new LoginUserVO();
        BeanUtil.copyProperties(user, userLoginVO);
        return userLoginVO;
    }

    /**
     * 转换userVO
     *
     * @param user
     * @return
     */
    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }


}




