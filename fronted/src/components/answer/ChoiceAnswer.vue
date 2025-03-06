<template>
  <div class="choice-answer">
    <el-radio-group v-model="selectedAnswer" class="answer-options">
      <el-radio v-for="option in problem.options" :key="option.key" :label="option.key" border>
        {{ option.key }}. {{ option.content }}
      </el-radio>
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
  (e: 'submit', answer: string): void
}>()

const selectedAnswer = ref('')

const handleSubmit = () => {
  emit('submit', selectedAnswer.value)
}
</script>

<style scoped>
.choice-answer {
  padding: 20px;
}

.answer-options {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.submit-area {
  margin-top: 24px;
  text-align: center;
}
</style> 