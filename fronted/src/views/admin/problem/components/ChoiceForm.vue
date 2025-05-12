<template>
    <div class="choice-form">
      <el-divider>选择题选项</el-divider>
      
      <div class="options-container">
        <div class="options-header">
          <h3>选项</h3>
          <el-button type="primary" size="small" @click="addOption">
            添加选项
          </el-button>
        </div>
        
        <div 
          v-for="(option, index) in modelValue.options" 
          :key="index"
          class="option-item"
        >
          <el-row>
            <el-col :span="20">
              <el-form-item :label="`选项 ${String.fromCharCode(65 + index)}`" :prop="`options.${index}.content`">
                <el-input v-model="option.content" placeholder="请输入选项内容" />
              </el-form-item>
            </el-col>
            
            <el-col :span="3" style="display: flex; align-items: center;">
              <el-checkbox v-model="option.isCorrect">正确选项</el-checkbox>
            </el-col>
            
            <el-col :span="1" style="display: flex; align-items: center; justify-content: flex-end;">
              <el-button 
                type="danger" 
                circle
                size="small"
                @click="removeOption(index)"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </el-col>
          </el-row>
        </div>
        
        <div class="validation-tips" v-if="!hasCorrectOption">
          <el-alert
            title="请至少选择一个正确选项"
            type="warning"
            :closable="false"
            show-icon
          />
        </div>
      </div>
    </div>
  </template>
  
  <script setup lang="ts">
  import { computed } from 'vue'
  import { ElMessage } from 'element-plus'
  import { Delete } from '@element-plus/icons-vue'
  
  // 定义选项接口
  interface Option {
    content: string;
    isCorrect: boolean;
  }
  
  const props = defineProps({
    modelValue: {
      type: Object,
      required: true
    }
  })
  
  // 检查是否至少有一个正确选项
  const hasCorrectOption = computed(() => {
    return props.modelValue.options && props.modelValue.options.some((option: Option) => option.isCorrect);
  })
  
  // 添加选项
  const addOption = () => {
    if (!props.modelValue.options) {
      props.modelValue.options = [];
    }
    props.modelValue.options.push({ content: '', isCorrect: false });
  }
  
  // 删除选项
  const removeOption = (index: number) => {
    if (props.modelValue.options.length <= 2) {
      ElMessage.warning('至少保留两个选项');
      return;
    }
    
    // 检查是否要删除的是唯一的正确选项
    const isRemovingOnlyCorrect = props.modelValue.options[index].isCorrect && 
      props.modelValue.options.filter((opt: Option) => opt.isCorrect).length === 1;
    
    if (isRemovingOnlyCorrect) {
      ElMessage.warning('不能删除唯一的正确选项，请先将另一个选项设为正确');
      return;
    }
    
    props.modelValue.options.splice(index, 1);
  }
  </script>
  
  <style scoped>
  .options-container {
    margin-top: 20px;
    margin-bottom: 20px;
    border: 1px solid #ebeef5;
    border-radius: 4px;
    padding: 20px;
    background-color: #f8f9fa;
  }
  
  .options-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
  }
  
  .option-item {
    margin-bottom: 10px;
  }
  
  .validation-tips {
    margin-top: 15px;
  }
  </style>