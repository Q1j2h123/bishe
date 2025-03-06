package com.oj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oj.common.ErrorCode;
import com.oj.common.UserContext;

import com.oj.exception.BusinessException;
import com.oj.mapper.UserMapper;
import com.oj.model.entity.User;
import com.oj.model.dto.UserDTO;
import com.oj.model.vo.UserVO;
import com.oj.service.UserService;
import com.oj.utils.JwtUtils;
import com.oj.utils.PasswordUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private JwtUtils jwtUtils;

    @Resource
    private PasswordUtils passwordUtils;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String userName) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, userName)) {
            throw new RuntimeException("参数为空");
        }
        if (userAccount.length() < 4) {
            throw new RuntimeException("用户账号过短");
        }
        if (userPassword.length() < 6) {
            throw new RuntimeException("用户密码过短");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new RuntimeException("两次输入的密码不一致");
        }
        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new RuntimeException("账号重复");
        }
        // 2. 加密
        String encryptPassword = passwordEncoder.encode(userPassword);
        // 3. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName(userName);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new RuntimeException("注册失败，数据库错误");
        }
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new RuntimeException("参数为空");
        }
        if (userAccount.length() < 4) {
            throw new RuntimeException("账号错误");
        }
        if (userPassword.length() < 6) {
            throw new RuntimeException("密码错误");
        }
        // 2. 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        User user = this.baseMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        // 3. 校验密码
        if (!passwordEncoder.matches(userPassword, user.getUserPassword())) {
            throw new RuntimeException("密码错误");
        }
        // 4. 用户脱敏
        user.setUserPassword(null);
        return user;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        User user = UserContext.getUser();
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return user;
    }

    @Override
    public boolean isAdmin(HttpServletRequest request) {
        User user = getLoginUser(request);
        return user != null && "admin".equals(user.getUserRole());
    }

    @Override
    public UserVO getUserVO(Long userId) {
        if (userId == null) {
            return null;
        }
        User user = this.getById(userId);
        if (user == null) {
            return null;
        }
        return dtoToVO(userToDTO(user));
    }

    @Override
    public List<UserVO> getUserVOByIds(List<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return new ArrayList<>();
        }
        return this.listByIds(userIds).stream()
                .map(user -> dtoToVO(userToDTO(user)))
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO userToDTO(User user) {
        if (user == null) {
            return null;
        }
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        return userDTO;
    }

    @Override
    public UserVO dtoToVO(UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userDTO, userVO);
        return userVO;
    }

    @Override
    public boolean updateUser(UserDTO userDTO) {
        if (userDTO == null || userDTO.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = this.getById(userDTO.getId());
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        BeanUtils.copyProperties(userDTO, user, "id"); // 复制属性，排除 id
        return this.updateById(user);
    }

    @Override
    public boolean deleteUser(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return this.removeById(userId); // 直接调用 MyBatis-Plus 的删除方法
    }
} 