package com.oj.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oj.model.entity.User;
import com.oj.model.dto.UserDTO;
import com.oj.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户服务
 */
public interface UserService extends IService<User> {
    /**
     * 用户注册
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     * @param userName 用户昵称
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, String userName);

    /**
     * 用户登录
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword);

    /**
     * 获取当前登录用户
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 是否为管理员
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 获取用户信息
     */
    UserVO getUserVO(Long userId);

    /**
     * 批量获取用户信息
     */
    List<UserVO> getUserVOByIds(List<Long> userIds);

    /**
     * Entity 转 DTO
     */
    UserDTO userToDTO(User user);

    /**
     * DTO 转 VO
     */
    UserVO dtoToVO(UserDTO userDTO);

    boolean updateUser(UserDTO userDTO);

    boolean deleteUser(Long userId);
}