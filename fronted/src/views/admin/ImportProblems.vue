<template>
    <div class="import-problems-container">
      <el-card class="import-card">
        <template #header>
          <div class="card-header">
            <h2>批量导入题目</h2>
          </div>
        </template>
        
        <el-tabs v-model="activeTab">
          <el-tab-pane label="选择题" name="choice">
            <div class="upload-area">
              <h3>导入选择题</h3>
              <p class="description">支持上传Excel(.xlsx, .xls)或CSV格式的文件，文件大小不超过10MB</p>
              
              <div class="format-tips">
                <el-alert type="info" :closable="false" show-icon>
                  <template #title>
                    文件格式提示
                  </template>
                  <p>1. Excel文件(.xlsx或.xls)必须是有效的Office格式</p>
                  <p>2. CSV文件必须使用UTF-8编码，并使用英文逗号(,)作为分隔符</p>
                  <p>3. 请确保列名与模板文件中的列名完全一致</p>
                  <p>4. 如遇到导入问题，请优先尝试使用Excel格式(.xlsx)</p>
                </el-alert>
              </div>
              
              <el-upload
                class="upload"
                action="#"
                :http-request="uploadChoiceFile"
                :before-upload="beforeUpload"
                :show-file-list="true"
                :limit="1"
                :on-exceed="handleExceed"
                :on-success="handleSuccess"
                :on-error="handleError"
                accept=".xlsx,.xls,.csv"
              >
                <el-button type="primary">点击上传</el-button>
                <template #tip>
                  <div class="el-upload__tip">
                    请确保文件符合导入模板格式，可<el-button type="primary" link @click="downloadTemplate('choice')">下载模板</el-button>
                  </div>
                </template>
              </el-upload>
              
              <div v-if="choiceImportResult" class="import-result">
                <el-alert
                  :title="getResultTitle(choiceImportResult)"
                  :type="getResultType(choiceImportResult)"
                  :description="getResultDescription(choiceImportResult)"
                  show-icon
                  :closable="false"
                />
                <div v-if="choiceImportResult.errorMessages && choiceImportResult.errorMessages.length > 0" class="error-messages">
                  <div class="error-title">错误详情：</div>
                  <ul>
                    <li v-for="(error, index) in choiceImportResult.errorMessages" :key="index" class="error-item">
                      {{ error }}
                    </li>
                  </ul>
                </div>
              </div>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="判断题" name="judge">
            <div class="upload-area">
              <h3>导入判断题</h3>
              <p class="description">支持上传Excel(.xlsx, .xls)或CSV格式的文件，文件大小不超过10MB</p>
              
              <div class="format-tips">
                <el-alert type="info" :closable="false" show-icon>
                  <template #title>
                    文件格式提示
                  </template>
                  <p>1. Excel文件(.xlsx或.xls)必须是有效的Office格式</p>
                  <p>2. CSV文件必须使用UTF-8编码，并使用英文逗号(,)作为分隔符</p>
                  <p>3. 请确保列名与模板文件中的列名完全一致</p>
                  <p>4. 如遇到导入问题，请优先尝试使用Excel格式(.xlsx)</p>
                </el-alert>
              </div>
              
              <el-upload
                class="upload"
                action="#"
                :http-request="uploadJudgeFile"
                :before-upload="beforeUpload"
                :show-file-list="true"
                :limit="1"
                :on-exceed="handleExceed"
                :on-success="handleSuccess"
                :on-error="handleError"
                accept=".xlsx,.xls,.csv"
              >
                <el-button type="primary">点击上传</el-button>
                <template #tip>
                  <div class="el-upload__tip">
                    请确保文件符合导入模板格式，可<el-button type="primary" link @click="downloadTemplate('judge')">下载模板</el-button>
                  </div>
                </template>
              </el-upload>
              
              <div v-if="judgeImportResult" class="import-result">
                <el-alert
                  :title="getResultTitle(judgeImportResult)"
                  :type="getResultType(judgeImportResult)"
                  :description="getResultDescription(judgeImportResult)"
                  show-icon
                  :closable="false"
                />
                <div v-if="judgeImportResult.errorMessages && judgeImportResult.errorMessages.length > 0" class="error-messages">
                  <div class="error-title">错误详情：</div>
                  <ul>
                    <li v-for="(error, index) in judgeImportResult.errorMessages" :key="index" class="error-item">
                      {{ error }}
                    </li>
                  </ul>
                </div>
              </div>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="编程题" name="program">
            <div class="upload-area">
              <h3>导入编程题</h3>
              <p class="description">支持上传Excel(.xlsx, .xls)或CSV格式的文件，文件大小不超过10MB</p>
              
              <div class="format-tips">
                <el-alert type="info" :closable="false" show-icon>
                  <template #title>
                    文件格式提示
                  </template>
                  <p>1. Excel文件(.xlsx或.xls)必须是有效的Office格式</p>
                  <p>2. CSV文件必须使用UTF-8编码，并使用英文逗号(,)作为分隔符</p>
                  <p>3. 请确保列名与模板文件中的列名完全一致</p>
                  <p>4. 如遇到导入问题，请优先尝试使用Excel格式(.xlsx)</p>
                </el-alert>
              </div>
              
              <el-upload
                class="upload"
                action="#"
                :http-request="uploadProgramFile"
                :before-upload="beforeUpload"
                :show-file-list="true"
                :limit="1"
                :on-exceed="handleExceed"
                :on-success="handleSuccess"
                :on-error="handleError"
                accept=".xlsx,.xls,.csv"
              >
                <el-button type="primary">点击上传</el-button>
                <template #tip>
                  <div class="el-upload__tip">
                    请确保文件符合导入模板格式，可<el-button type="primary" link @click="downloadTemplate('program')">下载模板</el-button>
                  </div>
                </template>
              </el-upload>
              
              <div v-if="programImportResult" class="import-result">
                <el-alert
                  :title="getResultTitle(programImportResult)"
                  :type="getResultType(programImportResult)"
                  :description="getResultDescription(programImportResult)"
                  show-icon
                  :closable="false"
                />
                <div v-if="programImportResult.errorMessages && programImportResult.errorMessages.length > 0" class="error-messages">
                  <div class="error-title">错误详情：</div>
                  <ul>
                    <li v-for="(error, index) in programImportResult.errorMessages" :key="index" class="error-item">
                      {{ error }}
                    </li>
                  </ul>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
        
        <div class="import-tips">
          <h3>导入说明</h3>
          <el-collapse>
            <el-collapse-item title="选择题导入格式说明" name="1">
              <p>选择题导入需要包含以下字段：</p>
              <ul>
                <li><strong>题目标题</strong>：必填，不超过100个字符</li>
                <li><strong>题目内容</strong>：必填，题目描述</li>
                <li><strong>选项A-D</strong>：必填，各选项内容</li>
                <li><strong>正确答案</strong>：必填，格式为单个字母A、B、C或D</li>
                <li><strong>题目解析</strong>：选填，对题目的详细解释</li>
                <li><strong>难度</strong>：选填，可选值：EASY（简单）、MEDIUM（中等）、HARD（困难）</li>
                <li><strong>岗位类型</strong>：选填，如：FRONTEND（前端）、BACKEND（后端）</li>
                <li><strong>标签</strong>：选填，多个标签用英文逗号分隔</li>
              </ul>
            </el-collapse-item>
            
            <el-collapse-item title="判断题导入格式说明" name="2">
              <p>判断题导入需要包含以下字段：</p>
              <ul>
                <li><strong>题目标题</strong>：必填，不超过100个字符</li>
                <li><strong>题目内容</strong>：必填，题目描述</li>
                <li><strong>正确答案</strong>：必填，值为true或false</li>
                <li><strong>题目解析</strong>：选填，对题目的详细解释</li>
                <li><strong>难度</strong>：选填，可选值：EASY（简单）、MEDIUM（中等）、HARD（困难）</li>
                <li><strong>岗位类型</strong>：选填，如：FRONTEND（前端）、BACKEND（后端）</li>
                <li><strong>标签</strong>：选填，多个标签用英文逗号分隔</li>
              </ul>
            </el-collapse-item>
            
            <el-collapse-item title="编程题导入格式说明" name="3">
              <p>编程题导入需要包含以下字段：</p>
              <ul>
                <li><strong>题目标题</strong>：必填，不超过100个字符</li>
                <li><strong>题目内容</strong>：必填，题目描述</li>
                <li><strong>函数名</strong>：必填，解题函数的名称</li>
                <li><strong>参数类型</strong>：必填，函数参数类型，多个参数用逗号分隔</li>
                <li><strong>返回值类型</strong>：必填，函数返回值类型</li>
                <li><strong>测试用例输入</strong>：必填，多个测试用例之间用###分隔</li>
                <li><strong>测试用例输出</strong>：必填，多个测试用例之间用###分隔</li>
                <li><strong>Java代码模板</strong>：选填，Java语言的代码模板</li>
                <li><strong>Python代码模板</strong>：选填，Python语言的代码模板</li>
                <li><strong>C++代码模板</strong>：选填，C++语言的代码模板</li>
                <li><strong>C代码模板</strong>：选填，C语言的代码模板</li>
                <li><strong>Java标准答案</strong>：选填，Java语言的标准答案</li>
                <li><strong>Python标准答案</strong>：选填，Python语言的标准答案</li>
                <li><strong>C++标准答案</strong>：选填，C++语言的标准答案</li>
                <li><strong>C标准答案</strong>：选填，C语言的标准答案</li>
                <li><strong>时间限制</strong>：选填，单位为毫秒</li>
                <li><strong>内存限制</strong>：选填，单位为KB</li>
                <li><strong>难度</strong>：选填，可选值：EASY（简单）、MEDIUM（中等）、HARD（困难）</li>
                <li><strong>岗位类型</strong>：选填，如：FRONTEND（前端）、BACKEND（后端）</li>
                <li><strong>标签</strong>：选填，多个标签用英文逗号分隔</li>
              </ul>
            </el-collapse-item>
          </el-collapse>
        </div>
      </el-card>
    </div>
  </template>
  
  <script setup lang="ts">
  import { ref } from 'vue'
  import { ElMessage } from 'element-plus'
  import { importChoiceProblems, importJudgeProblems, importProgramProblems, type ImportResult } from '@/api/import'
  
  // 定义上传请求的类型
  interface UploadRequest {
    file: File
  }
  
  // 定义上传响应的类型
  interface UploadResponse {
    status: 'success' | 'error'
  }
  
  // 当前激活的标签页
  const activeTab = ref<string>('choice')
  
  // 导入结果
  const choiceImportResult = ref<ImportResult | null>(null)
  const judgeImportResult = ref<ImportResult | null>(null)
  const programImportResult = ref<ImportResult | null>(null)
  
  // 文件上传前的校验
  const beforeUpload = (file: File): boolean => {
    // 文件类型检查
    const isExcelOrCSV = 
      file.type === 'application/vnd.ms-excel' || 
      file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' ||
      file.type === 'text/csv' ||
      file.name.endsWith('.xlsx') ||
      file.name.endsWith('.xls') ||
      file.name.endsWith('.csv')
    
    if (!isExcelOrCSV) {
      ElMessage.error('只能上传Excel或CSV文件!')
      return false
    }
    
    // 文件大小限制：10MB
    const isLt10M = file.size / 1024 / 1024 < 10
    
    if (!isLt10M) {
      ElMessage.error('文件大小不能超过10MB!')
      return false
    }
    
    return true
  }
  
  // 超出文件数量限制
  const handleExceed = (): void => {
    ElMessage.warning('一次只能上传一个文件')
  }
  
  // 上传成功的回调
  const handleSuccess = (): void => {
    ElMessage.success('文件上传成功')
  }
  
  // 上传失败的回调
  const handleError = (): void => {
    ElMessage.error('文件上传失败')
  }
  
  // 上传选择题文件
  const uploadChoiceFile = async (request: UploadRequest): Promise<UploadResponse> => {
    try {
      const response = await importChoiceProblems(request.file)
      if (response.code === 0) {
        choiceImportResult.value = response.data
        return { status: 'success' }
      } else {
        ElMessage.error(response.message || '导入失败')
        return { status: 'error' }
      }
    } catch (error: any) {
      console.error('导入选择题出错:', error)
      // 检查是否是Excel格式错误
      if (error.message && error.message.includes('OOXML')) {
        ElMessage.error('文件格式错误：请确保上传正确格式的Excel文件或UTF-8编码的CSV文件')
      } else {
        ElMessage.error('导入失败，请稍后重试')
      }
      return { status: 'error' }
    }
  }
  
  // 上传判断题文件
  const uploadJudgeFile = async (request: UploadRequest): Promise<UploadResponse> => {
    try {
      const response = await importJudgeProblems(request.file)
      if (response.code === 0) {
        judgeImportResult.value = response.data
        return { status: 'success' }
      } else {
        ElMessage.error(response.message || '导入失败')
        return { status: 'error' }
      }
    } catch (error: any) {
      console.error('导入判断题出错:', error)
      // 检查是否是Excel格式错误
      if (error.message && error.message.includes('OOXML')) {
        ElMessage.error('文件格式错误：请确保上传正确格式的Excel文件或UTF-8编码的CSV文件')
      } else {
        ElMessage.error('导入失败，请稍后重试')
      }
      return { status: 'error' }
    }
  }
  
  // 上传编程题文件
  const uploadProgramFile = async (request: UploadRequest): Promise<UploadResponse> => {
    try {
      const response = await importProgramProblems(request.file)
      if (response.code === 0) {
        programImportResult.value = response.data
        return { status: 'success' }
      } else {
        ElMessage.error(response.message || '导入失败')
        return { status: 'error' }
      }
    } catch (error: any) {
      console.error('导入编程题出错:', error)
      // 检查是否是Excel格式错误
      if (error.message && error.message.includes('OOXML')) {
        ElMessage.error('文件格式错误：请确保上传正确格式的Excel文件或UTF-8编码的CSV文件')
      } else {
        ElMessage.error('导入失败，请稍后重试')
      }
      return { status: 'error' }
    }
  }
  
  // 获取导入结果标题
  const getResultTitle = (result: ImportResult): string => {
    if (!result) return ''
    
    if (result.successCount === result.totalCount) {
      return `导入成功: ${result.successCount}/${result.totalCount}`
    } else if (result.successCount > 0) {
      return `部分导入成功: ${result.successCount}/${result.totalCount}`
    } else {
      return `导入失败: 0/${result.totalCount}`
    }
  }
  
  // 获取导入结果类型
  const getResultType = (result: ImportResult): 'success' | 'warning' | 'error' | 'info' => {
    if (!result) return 'info'
    
    if (result.successCount === result.totalCount) {
      return 'success'
    } else if (result.successCount > 0) {
      return 'warning'
    } else {
      return 'error'
    }
  }
  
  // 获取导入结果描述
  const getResultDescription = (result: ImportResult): string => {
    if (!result) return ''
    
    if (result.successCount === result.totalCount) {
      return '所有题目导入成功'
    } else if (result.successCount > 0) {
      return `成功导入${result.successCount}题，失败${result.failCount}题，请查看错误详情`
    } else {
      return '所有题目导入失败，请查看错误详情'
    }
  }
  
  // 下载导入模板
  const downloadTemplate = (type: string): void => {
    let templateUrl = ''
    let fileName = ''
    
    switch (type) {
      case 'choice':
        templateUrl = '/templates/choice_template.csv'
        fileName = '选择题导入模板.csv'
        break
      case 'judge':
        templateUrl = '/templates/judge_template.csv'
        fileName = '判断题导入模板.csv'
        break
      case 'program':
        templateUrl = '/templates/program_template.csv'
        fileName = '编程题导入模板.csv'
        break
      default:
        ElMessage.error('未知的模板类型')
        return
    }
    
    // 创建模拟点击事件下载模板
    try {
      const a = document.createElement('a')
      a.href = templateUrl
      a.download = fileName
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      
      ElMessage.success('模板下载中...')
      
      // 提示用户如何使用模板
      ElMessage.info('请使用Excel打开模板，按照格式填写后保存为.csv或.xlsx格式')
    } catch (e) {
      console.error('模板下载失败:', e)
      ElMessage.error('模板下载失败，请确保服务器中存在模板文件')
    }
  }
  </script>
  
  <style scoped>
  .import-problems-container {
    padding: 20px;
  }
  
  .import-card {
    margin-bottom: 20px;
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .card-header h2 {
    margin: 0;
    font-size: 18px;
  }
  
  .upload-area {
    padding: 20px 0;
  }
  
  .upload-area h3 {
    margin-top: 0;
    margin-bottom: 10px;
  }
  
  .description {
    margin-bottom: 20px;
    color: #606266;
    font-size: 14px;
  }
  
  .upload {
    margin-bottom: 20px;
  }
  
  .import-result {
    margin-top: 20px;
  }
  
  .error-messages {
    margin-top: 10px;
    padding: 10px;
    background-color: #fef0f0;
    border-radius: 4px;
  }
  
  .error-title {
    font-weight: bold;
    margin-bottom: 5px;
    color: #f56c6c;
  }
  
  .error-item {
    margin-bottom: 5px;
    color: #f56c6c;
  }
  
  .format-tips {
    margin-bottom: 15px;
  }
  
  .format-tips p {
    margin: 5px 0;
  }
  
  .import-tips {
    margin-top: 30px;
  }
  
  .import-tips h3 {
    margin-bottom: 10px;
  }
  
  :deep(.el-collapse-item__header) {
    font-weight: bold;
  }
  
  .el-upload__tip {
    line-height: 1.5;
  }
  </style>