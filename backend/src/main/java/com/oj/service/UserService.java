package com.oj.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oj.model.entity.User;
import com.oj.model.dto.UserDTO;
import com.oj.model.vo.UserVO;
import com.oj.model.vo.UserManageVO;
import com.oj.model.request.UserUpdateRequest;
import com.oj.model.dto.UserQueryRequest;
import com.oj.model.vo.UserListVO;

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
     * 用户登出
     * @param request 请求
     * @return 是否登出成功
     */
    boolean userLogout(HttpServletRequest request);

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
    boolean isAdmin(Long userId);
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

    /**
     * 获取当前登录用户
     * @param request
     * @return
     */
    UserDTO getCurrentUser(HttpServletRequest request);

    /**
     * 获取用户
     * @param id 用户id
     * @return 用户实体
     */
    User getById(Long id);

    /**
     * 更新用户信息
     * @param userUpdateRequest 用户更新信息
     * @param request HTTP请求
     * @return 是否更新成功
     */
    boolean updateUser(UserUpdateRequest userUpdateRequest, HttpServletRequest request);

    /**
     * 统计用户总数
     */
    Integer countUsers();

    /**
     * 分页查询用户列表

     */
    Page<UserListVO> listUsers(UserQueryRequest queryRequest);
//    Page<UserListVO> getUserList(UserQueryRequest userQueryRequest);

    /**
     * 获取用户管理详情
     * @param userId 用户ID
     * @return 用户管理详情
     */
    UserManageVO getUserManageDetail(Long userId);

    /**
     * 更新用户状态
     * @param userId 用户ID
     * @param status 状态（0-正常，1-禁用）
     * @return 是否更新成功
     */
    boolean updateUserStatus(Long userId, Integer status);

    /**
     * 更新用户角色
     * @param userId 用户ID
     * @param role 角色
     * @return 是否更新成功
     */
    boolean updateUserRole(Long userId, String role);

    /**
     * 重置用户密码
     * @param userId 用户ID
     * @return 是否重置成功
     */
    boolean resetUserPassword(Long userId);

    /**
     * 修改用户密码
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否修改成功
     */
//    boolean updateUserPassword(Long userId, String oldPassword, String newPassword);

    /**
     * 获取用户统计信息
     * @param userId 用户ID
     * @return 用户统计信息
     */
    UserManageVO getUserStats(Long userId);
}