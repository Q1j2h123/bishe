<template>
    <div class="judge-form">
      <el-divider>判断题答案</el-divider>
      
      <div class="answer-container">
        <el-form-item label="正确答案" prop="answer">
          <el-radio-group v-model="modelValue.answer">
            <el-radio :value="true">正确</el-radio>
            <el-radio :value="false">错误</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <div class="answer-tips">
          <el-alert
            title="判断题只需要设置题目的正确答案为'正确'或'错误'"
            type="info"
            :closable="false"
            show-icon
          />
        </div>
      </div>
    </div>
  </template>
  
  <script setup lang="ts">
  const props = defineProps({
    modelValue: {
      type: Object,
      required: true
    }
  })
  </script>
  
  <style scoped>
  .judge-form {
    margin-top: 20px;
  }
  
  .answer-container {
    margin-top: 20px;
    padding: 20px;
    border: 1px solid #ebeef5;
    border-radius: 4px;
    background-color: #f8f9fa;
  }
  
  .answer-tips {
    margin-top: 15px;
  }
  </style>