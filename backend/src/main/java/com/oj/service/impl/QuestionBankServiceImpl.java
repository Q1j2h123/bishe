package com.oj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oj.common.ErrorCode;
import com.oj.exception.BusinessException;
import com.oj.mapper.ProblemMapper;
import com.oj.mapper.QuestionBankMapper;
import com.oj.mapper.QuestionBankProblemMapper;
import com.oj.model.entity.Problem;
import com.oj.model.entity.QuestionBank;
import com.oj.model.entity.QuestionBankProblem;
import com.oj.model.entity.User;
import com.oj.model.enums.PermissionEnum;
import com.oj.model.request.QuestionBankAddRequest;
import com.oj.model.request.QuestionBankQueryRequest;
import com.oj.model.request.QuestionBankUpdateRequest;
import com.oj.model.dto.QuestionBankDTO;
import com.oj.model.vo.QuestionBankVO;
import com.oj.model.vo.UserVO;
import com.oj.service.QuestionBankService;
import com.oj.service.UserService;
import com.oj.utils.ValidateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class QuestionBankServiceImpl implements QuestionBankService {

    @Resource
    private QuestionBankMapper questionBankMapper;

    @Resource
    private QuestionBankProblemMapper bankProblemMapper;

    @Resource
    private ProblemMapper problemMapper;

    @Resource
    private UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addQuestionBank(QuestionBankAddRequest request, Long userId) {
        // 1. 参数校验
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        ValidateUtils.validateDifficulty(request.getDifficulty());
        if (StringUtils.isNotBlank(request.getPermission())) {
            ValidateUtils.validatePermission(request.getPermission());
        }

        // 2. 创建题库对象
        QuestionBank questionBank = new QuestionBank();
        BeanUtils.copyProperties(request, questionBank);
        questionBank.setUserId(userId);
        questionBank.setProblemCount(0);

        // 3. 处理标签
        if (StringUtils.isNotBlank(request.getTags())) {
            questionBank.setTags(formatTags(request.getTags()));
        }

        // 4. 保存到数据库
        if (questionBankMapper.insert(questionBank) <= 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题库创建失败");
        }

        return questionBank.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteQuestionBank(Long id, Long userId) {
        // 1. 校验题库是否存在且有权限
        QuestionBank bank = validateQuestionBank(id, userId);

        // 2. 删除题库-题目关联关系
        LambdaQueryWrapper<QuestionBankProblem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionBankProblem::getQuestionBankId, id);
        bankProblemMapper.delete(wrapper);

        // 3. 删除题库
        return questionBankMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateQuestionBank(QuestionBankUpdateRequest request, Long userId) {
        // 1. 校验题库是否存在且有权限
        QuestionBank oldBank = validateQuestionBank(request.getId(), userId);

        // 2. 校验参数
        ValidateUtils.validateDifficulty(request.getDifficulty());
        if (StringUtils.isNotBlank(request.getPermission())) {
            ValidateUtils.validatePermission(request.getPermission());
        }

        // 3. 更新题库信息
        QuestionBank questionBank = new QuestionBank();
        BeanUtils.copyProperties(request, questionBank);
        
        if (StringUtils.isNotBlank(request.getTags())) {
            questionBank.setTags(formatTags(request.getTags()));
        }

        return questionBankMapper.updateById(questionBank) > 0;
    }

    @Override
    public QuestionBankVO getQuestionBankById(Long id, Long userId) {
        // 1. 获取题库信息
        QuestionBank bank = questionBankMapper.selectById(id);
        if (bank == null || bank.getIsDelete() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        // 2. 检查访问权限
        checkAccessPermission(bank, userId);

        // 3. 转换为VO对象
        return questionBankToVO(bank);
    }

    @Override
    public Page<QuestionBankVO> listQuestionBank(QuestionBankQueryRequest request) {
        // 1. 创建查询条件
        LambdaQueryWrapper<QuestionBank> queryWrapper = buildQueryWrapper(request);

        // 2. 分页查询
        Page<QuestionBank> bankPage = new Page<>(request.getCurrent(), request.getPageSize());
        bankPage = questionBankMapper.selectPage(bankPage, queryWrapper);

        // 3. 转换为VO
        Page<QuestionBankVO> voPage = new Page<>(bankPage.getCurrent(), bankPage.getSize(), bankPage.getTotal());
        List<QuestionBankVO> bankVOList = bankPage.getRecords().stream()
                .map(this::questionBankToVO)
                .collect(Collectors.toList());
        voPage.setRecords(bankVOList);

        return voPage;
    }

    @Override
    public List<QuestionBankVO> getUserQuestionBanks(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 查询用户创建的题库
        LambdaQueryWrapper<QuestionBank> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(QuestionBank::getUserId, userId)
                   .eq(QuestionBank::getIsDelete, 0);

        return questionBankMapper.selectList(queryWrapper).stream()
                .map(this::questionBankToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addProblemToBank(Long bankId, Long problemId, Long userId) {
        // 1. 校验题库是否存在且有权限
        QuestionBank bank = validateQuestionBank(bankId, userId);

        // 2. 校验题目是否存在
        Problem problem = problemMapper.selectById(problemId);
        if (problem == null || problem.getIsDelete() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }

        // 3. 校验是否已经添加过
        LambdaQueryWrapper<QuestionBankProblem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionBankProblem::getQuestionBankId, bankId)
               .eq(QuestionBankProblem::getProblemId, problemId)
               .eq(QuestionBankProblem::getIsDelete, 0);
        if (bankProblemMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目已在题库中");
        }

        // 4. 添加关联关系
        QuestionBankProblem bankProblem = new QuestionBankProblem();
        bankProblem.setQuestionBankId(bankId);
        bankProblem.setProblemId(problemId);
        boolean success = bankProblemMapper.insert(bankProblem) > 0;

        // 5. 更新题目数量
        if (success) {
            bank.setProblemCount(bank.getProblemCount() + 1);
            questionBankMapper.updateById(bank);
        }

        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeProblemFromBank(Long bankId, Long problemId, Long userId) {
        // 1. 校验题库是否存在且有权限
        QuestionBank bank = validateQuestionBank(bankId, userId);

        // 2. 删除关联关系
        LambdaQueryWrapper<QuestionBankProblem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionBankProblem::getQuestionBankId, bankId)
               .eq(QuestionBankProblem::getProblemId, problemId);
        boolean success = bankProblemMapper.delete(wrapper) > 0;

        // 3. 更新题目数量
        if (success && bank.getProblemCount() > 0) {
            bank.setProblemCount(bank.getProblemCount() - 1);
            questionBankMapper.updateById(bank);
        }

        return success;
    }

    @Override
    public List<Long> getProblemIdsByBankId(Long bankId) {
        if (bankId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        LambdaQueryWrapper<QuestionBankProblem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionBankProblem::getQuestionBankId, bankId)
               .eq(QuestionBankProblem::getIsDelete, 0);

        return bankProblemMapper.selectList(wrapper).stream()
                .map(QuestionBankProblem::getProblemId)
                .collect(Collectors.toList());
    }

    @Override
    public QuestionBank validateQuestionBank(Long id, Long userId) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        QuestionBank bank = questionBankMapper.selectById(id);
        if (bank == null || bank.getIsDelete() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        // 仅创建者可以修改
        if (!bank.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        return bank;
    }

    /**
     * 检查访问权限
     */
    private void checkAccessPermission(QuestionBank bank, Long userId) {
        String permission = bank.getPermission();
        // 公开题库可以直接访问
        if (PermissionEnum.PUBLIC.name().equals(permission)) {
            return;
        }
        // 私有题库只有创建者可以访问
        if (PermissionEnum.PRIVATE.name().equals(permission) && !bank.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // TODO: 实现共享题库的权限控制
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<QuestionBank> buildQueryWrapper(QuestionBankQueryRequest request) {
        LambdaQueryWrapper<QuestionBank> queryWrapper = new LambdaQueryWrapper<>();
        
        if (request != null) {
            // 名称模糊搜索
            if (StringUtils.isNotBlank(request.getName())) {
                queryWrapper.like(QuestionBank::getName, request.getName());
            }
            // 根据难度筛选
            if (StringUtils.isNotBlank(request.getDifficulty())) {
                queryWrapper.eq(QuestionBank::getDifficulty, request.getDifficulty());
            }
            // 根据标签筛选
            if (StringUtils.isNotBlank(request.getTag())) {
                queryWrapper.like(QuestionBank::getTags, request.getTag());
            }
            // 根据权限筛选
            if (StringUtils.isNotBlank(request.getPermission())) {
                queryWrapper.eq(QuestionBank::getPermission, request.getPermission());
            }
            // 创建者筛选
            if (request.getUserId() != null) {
                queryWrapper.eq(QuestionBank::getUserId, request.getUserId());
            }
        }

        queryWrapper.eq(QuestionBank::getIsDelete, 0);
        return queryWrapper;
    }

    /**
     * QuestionBank 转 VO
     */
    private QuestionBankVO questionBankToVO(QuestionBank bank) {
        if (bank == null) {
            return null;
        }

        QuestionBankVO bankVO = new QuestionBankVO();
        BeanUtils.copyProperties(bank, bankVO);

        // 设置标签列表
        if (StringUtils.isNotBlank(bank.getTags())) {
            bankVO.setTags(Arrays.asList(bank.getTags().split(",")));
        }

        // 直接设置创建者的 userId
        bankVO.setUserId(bank.getUserId());

        return bankVO;
    }

    /**
     * 格式化标签
     */
    private String formatTags(String tags) {
        if (StringUtils.isBlank(tags)) {
            return "";
        }
        String[] tagArray = tags.split(",");
        Set<String> tagSet = Arrays.stream(tagArray)
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        return String.join(",", tagSet);
    }

    @Override
    public List<QuestionBankVO> getQuestionBanksByIds(List<Long> bankIds) {
        if (CollectionUtils.isEmpty(bankIds)) {
            return new ArrayList<>();
        }
        return questionBankMapper.selectBatchIds(bankIds).stream()
                .map(this::questionBankToVO)
                .collect(Collectors.toList());
    }

    @Override
    public QuestionBankDTO bankToDTO(QuestionBank questionBank) {
        if (questionBank == null) {
            return null;
        }
        QuestionBankDTO bankDTO = new QuestionBankDTO();
        BeanUtils.copyProperties(questionBank, bankDTO);
        return bankDTO;
    }

    @Override
    public QuestionBankDTO getQuestionBankDetail(Long id, Long userId) {
        // 1. 获取题库基本信息
        QuestionBank questionBank = questionBankMapper.selectById(id);
        if (questionBank == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题库不存在");
        }

        // 2. 判断权限
        if (!questionBank.getUserId().equals(userId) && !"PUBLIC".equals(questionBank.getPermission())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该题库");
        }

        // 3. 获取题库中的题目列表
        List<Long> problemIds = getProblemIdsByBankId(id);

        // 4. 构建 DTO
        QuestionBankDTO questionBankDTO = new QuestionBankDTO();
        BeanUtils.copyProperties(questionBank, questionBankDTO);
        questionBankDTO.setProblemIds(problemIds);
        questionBankDTO.setProblemCount(problemIds.size());

        return questionBankDTO;
    }

    @Override
    public QuestionBankVO dtoToVO(QuestionBankDTO questionBankDTO) {
        if (questionBankDTO == null) {
            return null;
        }
        QuestionBankVO questionBankVO = new QuestionBankVO();
        BeanUtils.copyProperties(questionBankDTO, questionBankVO);
        return questionBankVO;
    }

    @Override
    public void updateProblemCount(Long bankId) {
        if (bankId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QuestionBank bank = questionBankMapper.selectById(bankId);
        if (bank == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 查询关联的题目数量
        LambdaQueryWrapper<QuestionBankProblem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionBankProblem::getQuestionBankId, bankId)
               .eq(QuestionBankProblem::getIsDelete, 0);
        long count = bankProblemMapper.selectCount(wrapper);
        bank.setProblemCount((int) count);
        questionBankMapper.updateById(bank);
    }

    @Override
    public List<QuestionBankVO> searchQuestionBanks(String keyword) {
        if (StringUtils.isBlank(keyword)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<QuestionBank> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(QuestionBank::getName, keyword)
                    .or()
                    .like(QuestionBank::getDescription, keyword)
                    .eq(QuestionBank::getIsDelete, 0); // 确保只查询未删除的题库
        List<QuestionBank> questionBanks = questionBankMapper.selectList(queryWrapper);
        return questionBanks.stream()
                .map(this::questionBankToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<QuestionBankVO> getHotQuestionBanks() {
        LambdaQueryWrapper<QuestionBank> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(QuestionBank::getIsHot, 1) // 只查询热门题库
                    .eq(QuestionBank::getIsDelete, 0); // 确保只查询未删除的题库
        List<QuestionBank> questionBanks = questionBankMapper.selectList(queryWrapper);
        return questionBanks.stream()
                .map(this::questionBankToVO)
                .collect(Collectors.toList());
    }
} 