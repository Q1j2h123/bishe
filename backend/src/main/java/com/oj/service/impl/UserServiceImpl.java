package com.oj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oj.common.ErrorCode;
import com.oj.common.UserContext;
import com.oj.exception.BusinessException;
import com.oj.mapper.UserMapper;
import com.oj.model.dto.UserQueryRequest;
import com.oj.model.entity.User;
import com.oj.model.dto.UserDTO;
import com.oj.model.vo.UserListVO;
import com.oj.model.vo.UserVO;
import com.oj.model.request.UserUpdateRequest;
import com.oj.service.TokenBlacklistService;
import com.oj.service.UserService;
import com.oj.service.SubmissionService;
import com.oj.service.UserProblemStatusService;
import com.oj.model.vo.UserManageVO;
import com.oj.utils.JwtUtils;
import com.oj.utils.PasswordUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import com.oj.model.vo.UserProblemStatusVO;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.oj.constant.UserConstant.USER_LOGIN_STATE;


@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private JwtUtils jwtUtils;

    @Resource
    private PasswordUtils passwordUtils;

    @Resource
    private UserMapper userMapper;

    @Resource
    private TokenBlacklistService tokenBlacklistService;

    @Resource
    private SubmissionService submissionService;

    @Resource
    private UserProblemStatusService userProblemStatusService;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String userName) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, userName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        // 2. 加密
        String encryptPassword = passwordUtils.encryptPassword(userPassword);
        // 3. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName(userName);
        // 设置为管理员角色
//        user.setUserRole("admin");
        // 设置创建时间和更新时间
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        User user = this.baseMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        // 3. 校验密码
        if (!passwordUtils.matches(userPassword, user.getUserPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
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
    public boolean isAdmin(Long userId) {
        if (userId == null) {
            return false;
        }
        User user = getById(userId);
        return user != null && "admin".equals(user.getUserRole());
    }

    public Page<UserListVO> listUsers(UserQueryRequest queryRequest) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();

        // 构建查询条件
        queryWrapper
                .like(StringUtils.isNotBlank(queryRequest.getUserName()), User::getUserName, queryRequest.getUserName())
                .like(StringUtils.isNotBlank(queryRequest.getUserAccount()), User::getUserAccount, queryRequest.getUserAccount());

        // 处理排序
        if ("desc".equalsIgnoreCase(queryRequest.getCreateTimeOrder())) {
            queryWrapper.orderByDesc(User::getCreateTime);
        } else {
            queryWrapper.orderByAsc(User::getCreateTime);
        }

        // 转换VO
        Page<User> userPage = page(new Page<>(queryRequest.getCurrent(), queryRequest.getPageSize()), queryWrapper);
        return (Page<UserListVO>) userPage.convert(user -> {
            UserListVO vo = new UserListVO();
            BeanUtils.copyProperties(user, vo);
            return vo;
        });
    }
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
        return this.removeById(userId);
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        // 获取请求头中的token
        String header = request.getHeader("Authorization");
        if (StringUtils.isNotBlank(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();
            // 将token加入黑名单
            tokenBlacklistService.addToBlacklist(token);
        }
        
        // 清除用户上下文信息
        UserContext.clear();
        return true;
    }

    @Override
    public UserDTO getCurrentUser(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 从UserContext获取当前登录用户，而不是从Session
        User user = UserContext.getUser();
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 转换为 DTO
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        return userDTO;
    }

    @Override
    public User getById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return userMapper.selectById(id);
    }

    @Override
    public Integer countUsers() {
        // 查询所有用户数量并转为Integer
        return Math.toIntExact(userMapper.selectCount(null));
    }




    @Override
    public boolean updateUser(UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
        if (userUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取当前登录用户
        User loginUser = this.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        
        // 更新用户信息
        User user = new User();
        user.setId(loginUser.getId());
        if (StringUtils.isNotBlank(userUpdateRequest.getUserName())) {
            user.setUserName(userUpdateRequest.getUserName());
        }
        if (StringUtils.isNotBlank(userUpdateRequest.getUserAvatar())) {
            user.setUserAvatar(userUpdateRequest.getUserAvatar());
        }
        if (StringUtils.isNotBlank(userUpdateRequest.getUserProfile())) {
            user.setUserProfile(userUpdateRequest.getUserProfile());
        }
        user.setUpdateTime(LocalDateTime.now());
        
        return this.updateById(user);
    }

    @Override
    public UserManageVO getUserManageDetail(Long userId) {
        // 获取用户基本信息
        UserVO userVO = getUserVO(userId);
        if (userVO == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        
        // 获取用户实体以获取createTime
        User user = this.getById(userId);
        
        // 构建UserManageVO
        UserManageVO userManageVO = new UserManageVO();
        // 复制基本信息
        userManageVO.setId(userVO.getId());
        userManageVO.setUserAccount(userVO.getUserAccount());
        userManageVO.setUserName(userVO.getUserName());
        userManageVO.setUserAvatar(userVO.getUserAvatar());
        userManageVO.setUserProfile(userVO.getUserProfile());
        userManageVO.setUserRole(userVO.getUserRole());
        userManageVO.setCreateTime(user.getCreateTime());
        
        // 获取用户统计信息
        UserManageVO stats = getUserStats(userId);
        // 复制统计信息
        userManageVO.setSubmissionCount(stats.getSubmissionCount());
        userManageVO.setAcceptedCount(stats.getAcceptedCount());
        userManageVO.setTotalSolvedCount(stats.getTotalSolvedCount());
        userManageVO.setAcceptanceRate(stats.getAcceptanceRate());
        userManageVO.setLastAcceptedProblem(stats.getLastAcceptedProblem());
        
        return userManageVO;
    }
    @Override
    public UserManageVO getUserStats(Long userId) {
        UserManageVO stats = new UserManageVO();

        // 获取提交次数
        stats.setSubmissionCount(submissionService.countUserSubmissions(userId).intValue());

        // 获取通过题目数
        stats.setAcceptedCount(userProblemStatusService.countUserSolvedProblems(userId).intValue());

        // 获取总做题数
        stats.setTotalSolvedCount(userProblemStatusService.countUserAttemptedProblems(userId).intValue());

        // 计算通过率
        if (stats.getSubmissionCount() > 0) {
            stats.setAcceptanceRate((double) stats.getAcceptedCount() / stats.getSubmissionCount() * 100);
        } else {
            stats.setAcceptanceRate(0.0);
        }

        // 获取最近通过的题目
        List<UserProblemStatusVO> solvedProblems = userProblemStatusService.getUserSolvedProblems(userId);
        if (!solvedProblems.isEmpty()) {
            // 按最后提交时间倒序排序，获取最近通过的题目
            solvedProblems.sort((a, b) -> b.getLastSubmitTime().compareTo(a.getLastSubmitTime()));
            stats.setLastAcceptedProblem(solvedProblems.get(0).getProblemTitle());
        }

        return stats;
    }

    @Override
    public boolean updateUserStatus(Long userId, Integer status) {
        // 由于没有status字段，我们可以通过userRole来实现类似的功能
        // 如果status为1（禁用），则将用户角色改为"banned"
        // 如果status为0（正常），则将用户角色改为"user"
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        
        user.setUserRole(status == 1 ? "banned" : "user");
        return this.updateById(user);
    }

    @Override
    public boolean updateUserRole(Long userId, String role) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        
        user.setUserRole(role);
        return this.updateById(user);
    }

    @Override
    public boolean resetUserPassword(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        
        // 重置为默认密码
        String defaultPassword = DigestUtils.md5DigestAsHex("123456".getBytes());
        user.setUserPassword(defaultPassword);
        return this.updateById(user);
    }


} 