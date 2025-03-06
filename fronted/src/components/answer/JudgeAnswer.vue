<template>
  <div class="judge-answer">
    <el-radio-group v-model="selectedAnswer" class="answer-options">
      <el-radio label="true" border>正确</el-radio>
      <el-radio label="false" border>错误</el-radio>
    </el-radio-group>
    
    <div class="submit-area">
      <el-button type="primary" @click="handleSubmit" :disabled="!selectedAnswer">
        提交答案
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { Problem } from '../../api/problem'

const props = defineProps<{
  problem: Problem
}>()

const emit = defineEmits<{
  (e: 'submit', answer: boolean): void
}>()

const selectedAnswer = ref('')

const handleSubmit = () => {
  emit('submit', selectedAnswer.value === 'true')
}
</script>

<style scoped>
.judge-answer {
  padding: 20px;
}

.answer-options {
  display: flex;
  justify-content: center;
  gap: 32px;
}

.submit-area {
  margin-top: 24px;
  text-align: center;
}
</style> 