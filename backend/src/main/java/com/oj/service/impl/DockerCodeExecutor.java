package com.oj.service.impl;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.github.dockerjava.core.command.WaitContainerResultCallback;
import com.oj.constant.JudgeConstant;
import com.oj.exception.BusinessException;
import com.oj.model.dto.CompileResult;
import com.oj.model.dto.ExecutionResult;
import com.oj.service.CodeExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 基于Docker的代码执行器
 * 注意：此实现需要系统安装Docker并开放API访问
 */
@Service
@Slf4j
public class DockerCodeExecutor implements CodeExecutor {
    
    @Value("${judge.docker.host:unix:///var/run/docker.sock}")
    private String dockerHost;
    
    @Value("${judge.workspace:/tmp/judge}")
    private String workspaceBase;
    
    private DockerClient dockerClient;
    private boolean dockerAvailable = false;
    
    public DockerCodeExecutor() {
        try {
            // 初始化Docker客户端 - 添加更多判断和错误处理
            DockerClientConfig config;
            
            // 适配Windows环境下的Docker Desktop
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                // Windows环境下Docker Desktop默认使用named pipe
                config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                        .withDockerHost("npipe:////./pipe/docker_engine")
                        .build();
            } else {
                // Unix/Linux环境默认使用Unix socket
                config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                        .withDockerHost(dockerHost)
                        .build();
            }
            
            this.dockerClient = DockerClientBuilder.getInstance(config).build();
            
            // 测试连接
            try {
                dockerClient.pingCmd().exec();
                log.info("成功连接到Docker服务");
                dockerAvailable = true;
            } catch (Exception e) {
                log.error("无法连接到Docker服务: {}", e.getMessage());
                // 不抛出异常，允许服务启动，但评测功能将不可用
                dockerAvailable = false;
            }
        } catch (Exception e) {
            log.error("初始化Docker客户端失败: {}", e.getMessage());
            // 初始化失败但不抛出异常，允许服务启动（但评测功能不可用）
            dockerAvailable = false;
        }
    }
    
    @Override
    public CompileResult compile(String code, String language) {
        // 如果Docker不可用，抛出异常
        if (!dockerAvailable) {
            log.warn("Docker不可用，无法编译代码");
            throw new BusinessException(50000, "当前环境未配置Docker服务，无法编译运行代码");
        }
        
        // 创建工作目录
        String workspaceId = UUID.randomUUID().toString();
        String workspacePath = workspaceBase + "/" + workspaceId;
        File workspace = new File(workspacePath);
        if (!workspace.exists() && !workspace.mkdirs()) {
            throw new BusinessException(50000, "无法创建评测工作目录");
        }
        
        try {
            // 根据语言选择编译方式
            switch (language) {
                case JudgeConstant.LANGUAGE_JAVA:
                    return compileJava(code, workspacePath);
                case JudgeConstant.LANGUAGE_PYTHON:
                    return compilePython(code, workspacePath);
                case JudgeConstant.LANGUAGE_CPP:
                case JudgeConstant.LANGUAGE_C:
                    return compileCpp(code, language, workspacePath);
                default:
                    throw new BusinessException(40000, "不支持的编程语言: " + language);
            }
        } catch (Exception e) {
            log.error("编译代码失败", e);
            return CompileResult.builder()
                    .success(false)
                    .errorMessage("编译失败: " + e.getMessage())
                    .build();
        }
    }
    
    @Override
    public ExecutionResult execute(String compiledCode, String input, Long timeLimit, Long memoryLimit) {
        // 如果Docker不可用，抛出异常
        if (!dockerAvailable) {
            log.warn("Docker不可用，无法执行代码");
            throw new BusinessException(50000, "当前环境未配置Docker服务，无法执行代码");
        }
        
        // 获取工作目录
        File workspace = new File(compiledCode).getParentFile();
        String workspacePath = workspace.getAbsolutePath();
        
        try {
            // 将输入数据写入文件
            File inputFile = new File(workspacePath + "/input.txt");
            FileUtils.writeStringToFile(inputFile, input, StandardCharsets.UTF_8);
            
            // 创建输出文件
            File outputFile = new File(workspacePath + "/output.txt");
            
            // 检测语言类型并执行
            if (compiledCode.endsWith(".class")) {
                return executeJava(compiledCode, workspacePath, timeLimit, memoryLimit);
            } else if (compiledCode.endsWith(".py")) {
                return executePython(compiledCode, workspacePath, timeLimit, memoryLimit);
            } else if (compiledCode.endsWith(".out") || compiledCode.endsWith(".exe")) {
                return executeCpp(compiledCode, workspacePath, timeLimit, memoryLimit);
            } else {
                throw new BusinessException(40000, "无法识别的编译结果文件: " + compiledCode);
            }
        } catch (Exception e) {
            log.error("执行代码失败", e);
            return ExecutionResult.builder()
                    .success(false)
                    .status(JudgeConstant.STATUS_RUNTIME_ERROR)
                    .errorMessage("执行失败: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * 判断Docker是否可用
     */
    public boolean isDockerAvailable() {
        return dockerAvailable;
    }
    
    /**
     * 编译Java代码
     */
    private CompileResult compileJava(String code, String workspacePath) throws IOException, InterruptedException {
        // 写入代码文件
        String className = "Solution";
        File sourceFile = new File(workspacePath + "/" + className + ".java");
        FileUtils.writeStringToFile(sourceFile, code, StandardCharsets.UTF_8);
        
        // 拉取Docker镜像
        dockerClient.pullImageCmd(JudgeConstant.DOCKER_IMAGE_JAVA)
                .exec(new PullImageResultCallback())
                .awaitCompletion(30, TimeUnit.SECONDS);
        
        // 创建Volume
        Volume volume = new Volume("/code");
        
        // 创建容器
        CreateContainerResponse container = dockerClient.createContainerCmd(JudgeConstant.DOCKER_IMAGE_JAVA)
                .withHostConfig(HostConfig.newHostConfig()
                        .withBinds(new Bind(workspacePath, volume))
                        .withMemory(128 * 1024 * 1024L) // 128MB
                        .withCpuCount(1L))
                .withWorkingDir("/code")
                .withCmd("javac", className + ".java")
                .exec();
        
        // 启动容器
        dockerClient.startContainerCmd(container.getId()).exec();
        
        // 等待容器完成 - 使用回调
        WaitContainerResultCallback waitCallback = new WaitContainerResultCallback();
        dockerClient.waitContainerCmd(container.getId()).exec(waitCallback);
        waitCallback.awaitCompletion();
        
        // 检查编译结果
        File classFile = new File(workspacePath + "/" + className + ".class");
        boolean success = classFile.exists();
        String errorMessage = "";
        
        if (!success) {
            // 获取容器日志
            LogContainerResultCallback logCallback = new LogContainerResultCallback();
            dockerClient.logContainerCmd(container.getId())
                    .withStdErr(true)
                    .withStdOut(true)
                    .exec(logCallback);
            logCallback.awaitCompletion(10, TimeUnit.SECONDS);
            errorMessage = logCallback.toString();
        }
        
        // 移除容器
        dockerClient.removeContainerCmd(container.getId()).exec();
        
        return CompileResult.builder()
                .success(success)
                .errorMessage(errorMessage)
                .compiledFilePath(success ? workspacePath + "/" + className + ".class" : null)
                .build();
    }
    
    /**
     * 编译Python代码（Python无需编译，仅进行语法检查）
     */
    private CompileResult compilePython(String code, String workspacePath) throws IOException, InterruptedException {
        // 写入代码文件
        File sourceFile = new File(workspacePath + "/solution.py");
        FileUtils.writeStringToFile(sourceFile, code, StandardCharsets.UTF_8);
        
        // 拉取Docker镜像
        dockerClient.pullImageCmd(JudgeConstant.DOCKER_IMAGE_PYTHON)
                .exec(new PullImageResultCallback())
                .awaitCompletion(30, TimeUnit.SECONDS);
        
        // 创建Volume
        Volume volume = new Volume("/code");
        
        // 创建容器
        CreateContainerResponse container = dockerClient.createContainerCmd(JudgeConstant.DOCKER_IMAGE_PYTHON)
                .withHostConfig(HostConfig.newHostConfig()
                        .withBinds(new Bind(workspacePath, volume))
                        .withMemory(128 * 1024 * 1024L) // 128MB
                        .withCpuCount(1L))
                .withWorkingDir("/code")
                .withCmd("python", "-m", "py_compile", "solution.py")
                .exec();
        
        // 启动容器
        dockerClient.startContainerCmd(container.getId()).exec();
        
        // 等待容器完成 - 使用回调
        WaitContainerResultCallback waitCallback = new WaitContainerResultCallback();
        dockerClient.waitContainerCmd(container.getId()).exec(waitCallback);
        waitCallback.awaitCompletion();
        
        // 获取容器日志
        LogContainerResultCallback logCallback = new LogContainerResultCallback();
        dockerClient.logContainerCmd(container.getId())
                .withStdErr(true)
                .withStdOut(true)
                .exec(logCallback);
        logCallback.awaitCompletion(10, TimeUnit.SECONDS);
        String errorMessage = logCallback.toString();
        
        boolean success = errorMessage.isEmpty() || !errorMessage.contains("Error");
        
        // 移除容器
        dockerClient.removeContainerCmd(container.getId()).exec();
        
        return CompileResult.builder()
                .success(success)
                .errorMessage(success ? "" : errorMessage)
                .compiledFilePath(success ? workspacePath + "/solution.py" : null)
                .build();
    }
    
    /**
     * 编译C/C++代码
     */
    private CompileResult compileCpp(String code, String language, String workspacePath) throws IOException, InterruptedException {
        // 写入代码文件
        String filename = language.equals(JudgeConstant.LANGUAGE_CPP) ? "solution.cpp" : "solution.c";
        File sourceFile = new File(workspacePath + "/" + filename);
        FileUtils.writeStringToFile(sourceFile, code, StandardCharsets.UTF_8);
        
        // 拉取Docker镜像
        dockerClient.pullImageCmd(JudgeConstant.DOCKER_IMAGE_CPP)
                .exec(new PullImageResultCallback())
                .awaitCompletion(30, TimeUnit.SECONDS);
        
        // 创建Volume
        Volume volume = new Volume("/code");
        
        // 准备编译命令
        String compiler = language.equals(JudgeConstant.LANGUAGE_CPP) ? "g++" : "gcc";
        String outputFile = "solution.out";
        
        // 创建容器
        CreateContainerResponse container = dockerClient.createContainerCmd(JudgeConstant.DOCKER_IMAGE_CPP)
                .withHostConfig(HostConfig.newHostConfig()
                        .withBinds(new Bind(workspacePath, volume))
                        .withMemory(128 * 1024 * 1024L) // 128MB
                        .withCpuCount(1L))
                .withWorkingDir("/code")
                .withCmd(compiler, "-o", outputFile, filename, "-O2", "-Wall")
                .exec();
        
        // 启动容器
        dockerClient.startContainerCmd(container.getId()).exec();
        
        // 等待容器完成 - 使用回调
        WaitContainerResultCallback waitCallback = new WaitContainerResultCallback();
        dockerClient.waitContainerCmd(container.getId()).exec(waitCallback);
        waitCallback.awaitCompletion();
        
        // 检查编译结果
        File outFile = new File(workspacePath + "/" + outputFile);
        boolean success = outFile.exists();
        String errorMessage = "";
        
        if (!success) {
            // 获取容器日志
            LogContainerResultCallback logCallback = new LogContainerResultCallback();
            dockerClient.logContainerCmd(container.getId())
                    .withStdErr(true)
                    .withStdOut(true)
                    .exec(logCallback);
            logCallback.awaitCompletion(10, TimeUnit.SECONDS);
            errorMessage = logCallback.toString();
        }
        
        // 移除容器
        dockerClient.removeContainerCmd(container.getId()).exec();
        
        return CompileResult.builder()
                .success(success)
                .errorMessage(errorMessage)
                .compiledFilePath(success ? workspacePath + "/" + outputFile : null)
                .build();
    }
    
    /**
     * 执行Java代码
     */
    private ExecutionResult executeJava(String compiledFilePath, String workspacePath, Long timeLimit, Long memoryLimit) throws InterruptedException {
        String className = new File(compiledFilePath).getName().replace(".class", "");
        
        // 创建Volume
        Volume volume = new Volume("/code");
        
        // 创建容器
        CreateContainerResponse container = dockerClient.createContainerCmd(JudgeConstant.DOCKER_IMAGE_JAVA)
                .withHostConfig(HostConfig.newHostConfig()
                        .withBinds(new Bind(workspacePath, volume))
                        .withMemory(memoryLimit * 1024) // KB转字节
                        .withCpuCount(1L))
                .withWorkingDir("/code")
                .withCmd("sh", "-c", "java -Xmx" + (memoryLimit / 1024) + "m " + className + " < input.txt > output.txt 2>error.txt")
                .exec();
        
        // 启动容器
        dockerClient.startContainerCmd(container.getId()).exec();
        
        // 等待容器完成（带超时）
        boolean timedOut = false;
        try {
            WaitContainerResultCallback waitCallback = new WaitContainerResultCallback();
            dockerClient.waitContainerCmd(container.getId()).exec(waitCallback);
            timedOut = !waitCallback.awaitCompletion(timeLimit, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            timedOut = true;
        }
        
        // 获取结果
        ExecutionResult result = getExecutionResult(workspacePath, timedOut);
        
        // 移除容器
        dockerClient.removeContainerCmd(container.getId()).exec();
        
        return result;
    }
    
    /**
     * 执行Python代码
     */
    private ExecutionResult executePython(String compiledFilePath, String workspacePath, Long timeLimit, Long memoryLimit) throws InterruptedException {
        // 创建Volume
        Volume volume = new Volume("/code");
        
        // 创建容器
        CreateContainerResponse container = dockerClient.createContainerCmd(JudgeConstant.DOCKER_IMAGE_PYTHON)
                .withHostConfig(HostConfig.newHostConfig()
                        .withBinds(new Bind(workspacePath, volume))
                        .withMemory(memoryLimit * 1024) // KB转字节
                        .withCpuCount(1L))
                .withWorkingDir("/code")
                .withCmd("sh", "-c", "python solution.py < input.txt > output.txt 2>error.txt")
                .exec();
        
        // 启动容器
        dockerClient.startContainerCmd(container.getId()).exec();
        
        // 等待容器完成（带超时）
        boolean timedOut = false;
        try {
            WaitContainerResultCallback waitCallback = new WaitContainerResultCallback();
            dockerClient.waitContainerCmd(container.getId()).exec(waitCallback);
            timedOut = !waitCallback.awaitCompletion(timeLimit, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            timedOut = true;
        }
        
        // 获取结果
        ExecutionResult result = getExecutionResult(workspacePath, timedOut);
        
        // 移除容器
        dockerClient.removeContainerCmd(container.getId()).exec();
        
        return result;
    }
    
    /**
     * 执行C/C++代码
     */
    private ExecutionResult executeCpp(String compiledFilePath, String workspacePath, Long timeLimit, Long memoryLimit) throws InterruptedException {
        // 创建Volume
        Volume volume = new Volume("/code");
        
        // 创建容器
        CreateContainerResponse container = dockerClient.createContainerCmd(JudgeConstant.DOCKER_IMAGE_CPP)
                .withHostConfig(HostConfig.newHostConfig()
                        .withBinds(new Bind(workspacePath, volume))
                        .withMemory(memoryLimit * 1024) // KB转字节
                        .withCpuCount(1L))
                .withWorkingDir("/code")
                .withCmd("sh", "-c", "./solution.out < input.txt > output.txt 2>error.txt")
                .exec();
        
        // 启动容器
        dockerClient.startContainerCmd(container.getId()).exec();
        
        // 等待容器完成（带超时）
        boolean timedOut = false;
        try {
            WaitContainerResultCallback waitCallback = new WaitContainerResultCallback();
            dockerClient.waitContainerCmd(container.getId()).exec(waitCallback);
            timedOut = !waitCallback.awaitCompletion(timeLimit, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            timedOut = true;
        }
        
        // 获取结果
        ExecutionResult result = getExecutionResult(workspacePath, timedOut);
        
        // 移除容器
        dockerClient.removeContainerCmd(container.getId()).exec();
        
        return result;
    }
    
    /**
     * 从工作目录读取执行结果
     */
    private ExecutionResult getExecutionResult(String workspacePath, boolean timedOut) {
        try {
            File outputFile = new File(workspacePath + "/output.txt");
            File errorFile = new File(workspacePath + "/error.txt");
            
            String output = "";
            String errorMessage = "";
            
            if (outputFile.exists()) {
                output = FileUtils.readFileToString(outputFile, StandardCharsets.UTF_8);
            }
            
            if (errorFile.exists()) {
                errorMessage = FileUtils.readFileToString(errorFile, StandardCharsets.UTF_8);
            }
            
            if (timedOut) {
                return ExecutionResult.builder()
                        .success(false)
                        .status(JudgeConstant.STATUS_TIME_LIMIT_EXCEEDED)
                        .output(output)
                        .errorMessage("执行超时")
                        .executionTime(0L)
                        .memoryUsage(0L)
                        .build();
            }
            
            if (!errorMessage.isEmpty()) {
                return ExecutionResult.builder()
                        .success(false)
                        .status(JudgeConstant.STATUS_RUNTIME_ERROR)
                        .output(output)
                        .errorMessage(errorMessage)
                        .executionTime(0L)
                        .memoryUsage(0L)
                        .build();
            }
            
            return ExecutionResult.builder()
                    .success(true)
                    .status(JudgeConstant.STATUS_ACCEPTED)
                    .output(output)
                    .errorMessage("")
                    .executionTime(0L) // 实际应该从容器获取，这里简化处理
                    .memoryUsage(0L)   // 实际应该从容器获取，这里简化处理
                    .build();
            
        } catch (IOException e) {
            log.error("读取执行结果失败", e);
            return ExecutionResult.builder()
                    .success(false)
                    .status(JudgeConstant.STATUS_SYSTEM_ERROR)
                    .errorMessage("读取执行结果失败: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * 容器日志回调
     */
    private static class LogContainerResultCallback extends com.github.dockerjava.core.command.LogContainerResultCallback {
        private StringBuilder log = new StringBuilder();
        
        @Override
        public void onNext(com.github.dockerjava.api.model.Frame item) {
            log.append(new String(item.getPayload()));
            super.onNext(item);
        }
        
        @Override
        public String toString() {
            return log.toString();
        }
    }
} 