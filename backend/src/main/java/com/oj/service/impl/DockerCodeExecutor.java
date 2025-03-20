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
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.github.dockerjava.core.command.WaitContainerResultCallback;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import com.oj.constant.JudgeConstant;
import com.oj.exception.BusinessException;
import com.oj.model.dto.CompileResult;
import com.oj.model.dto.ExecutionResult;
import com.oj.service.CodeExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 基于Docker的代码执行器
 * 注意：此实现需要系统安装Docker并开放API访问
 */
@Service
@Slf4j
public class DockerCodeExecutor implements CodeExecutor {

    @Value("${judge.docker.host}")
    private String dockerHost;

    @Value("${judge.workspace:/tmp/judge}")
    private String workspaceBase;

    private DockerClient dockerClient;
    private boolean dockerAvailable = false;

    public DockerCodeExecutor() {
    }

    @PostConstruct
    public void init() {
        try {
            log.info("Starting Docker initialization...");
            log.info("Docker host configuration: {}", dockerHost);
            log.info("Workspace base path: {}", workspaceBase);

            // 初始化Docker客户端配置
            DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                    .withDockerHost(dockerHost)
                    .build();

            log.info("Docker client config created successfully");

            // 使用httpclient5传输方式，增加超时时间
            DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                    .dockerHost(config.getDockerHost())
                    .sslConfig(config.getSSLConfig())
                    .maxConnections(100)
                    .connectionTimeout(Duration.ofSeconds(60))  // 增加连接超时时间
                    .responseTimeout(Duration.ofSeconds(60))    // 增加响应超时时间
                    .build();

            log.info("Docker HTTP client created successfully");

            this.dockerClient = DockerClientImpl.getInstance(config, httpClient);
            log.info("Docker client instance created successfully");

            // 测试连接
            try {
                dockerClient.pingCmd().exec();
                log.info("Successfully connected to Docker service");
                dockerAvailable = true;

                // 测试列出镜像
                dockerClient.listImagesCmd().exec().forEach(image -> {
                    log.info("Found Docker image: {}", image.getId());
                });
            } catch (Exception e) {
                log.error("Cannot connect to Docker service: {}", e.getMessage());
                log.error("Connection error details:", e);
                dockerAvailable = false;
            }
        } catch (Exception e) {
            log.error("Failed to initialize Docker client: {}", e.getMessage());
            log.error("Initialization error details:", e);
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
        
        // 生成包装类，包含main方法，用于处理输入和调用用户代码
        String wrapperClass = generateJavaWrapper(code);
        
        // 写入包装类的Main.java文件
        File mainFile = new File(workspacePath + "/Main.java");
        FileUtils.writeStringToFile(mainFile, wrapperClass, StandardCharsets.UTF_8);
        
        // 同时也保存用户原始代码文件，以便能够正确编译引用
        File sourceFile = new File(workspacePath + "/" + className + ".java");
        FileUtils.writeStringToFile(sourceFile, code, StandardCharsets.UTF_8);
        
        // 拉取Docker镜像
        dockerClient.pullImageCmd(JudgeConstant.DOCKER_IMAGE_JAVA)
                .exec(new PullImageResultCallback())
                .awaitCompletion(30, TimeUnit.SECONDS);
        
        // 创建Volume
        Volume volume = new Volume("/code");
        
        // 创建容器 - 编译两个文件
        CreateContainerResponse container = dockerClient.createContainerCmd(JudgeConstant.DOCKER_IMAGE_JAVA)
                .withHostConfig(HostConfig.newHostConfig()
                        .withBinds(new Bind(workspacePath, volume))
                        .withMemory(128 * 1024 * 1024L) // 128MB
                        .withCpuCount(1L))
                .withWorkingDir("/code")
                .withCmd("javac", "Main.java", className + ".java")
                .exec();
        
        // 启动容器
        dockerClient.startContainerCmd(container.getId()).exec();
        
        // 等待容器完成 - 使用回调
        WaitContainerResultCallback waitCallback = new WaitContainerResultCallback();
        dockerClient.waitContainerCmd(container.getId()).exec(waitCallback);
        waitCallback.awaitCompletion();
        
        // 检查编译结果 - 主要检查Main.class是否存在
        File mainClassFile = new File(workspacePath + "/Main.class");
        boolean success = mainClassFile.exists();
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
                .compiledFilePath(success ? workspacePath + "/Main.class" : null)
                .build();
    }
    
    /**
     * 生成Java包装类，包含main方法，处理输入并调用用户代码
     */
    private String generateJavaWrapper(String userCode) {
        // 分析用户代码，检测常见的方法签名
        boolean isTwoSum = userCode.contains("public int[] twoSum(int[] nums, int target)");
        boolean isPalindrome = userCode.contains("public boolean isPalindrome(");
        boolean isMaxSubArray = userCode.contains("public int maxSubArray(int[] nums)");
        boolean isReverseString = userCode.contains("public String reverse(String");
        boolean isFibonacci = userCode.contains("public int fibonacci(int n)") || userCode.contains("public int fib(int n)");
        boolean isSortArray = userCode.contains("public int[] sort") || userCode.contains("public void sort");
        
        StringBuilder wrapper = new StringBuilder();
        wrapper.append("import java.util.*;\n");
        wrapper.append("import java.io.*;\n\n");
        wrapper.append("public class Main {\n");
        wrapper.append("    public static void main(String[] args) {\n");
        wrapper.append("        try {\n");
        wrapper.append("            Scanner scanner = new Scanner(System.in);\n");
        wrapper.append("            String line = scanner.nextLine().trim();\n");
        wrapper.append("            Solution solution = new Solution();\n\n");
        
        // 根据不同的问题类型，生成相应的输入处理和方法调用代码
        if (isTwoSum) {
            // 处理两数之和问题
            wrapper.append("            // 解析输入格式：数组元素空格分隔，最后一个数为target\n");
            wrapper.append("            String[] parts = line.split(\" \");\n");
            wrapper.append("            int[] nums = new int[parts.length - 1];\n");
            wrapper.append("            for (int i = 0; i < parts.length - 1; i++) {\n");
            wrapper.append("                nums[i] = Integer.parseInt(parts[i]);\n");
            wrapper.append("            }\n");
            wrapper.append("            int target = Integer.parseInt(parts[parts.length - 1]);\n\n");
            wrapper.append("            // 调用用户实现的方法\n");
            wrapper.append("            int[] result = solution.twoSum(nums, target);\n\n");
            wrapper.append("            // 输出结果\n");
            wrapper.append("            if (result != null && result.length > 0) {\n");
            wrapper.append("                StringBuilder sb = new StringBuilder();\n");
            wrapper.append("                for (int i = 0; i < result.length; i++) {\n");
            wrapper.append("                    sb.append(result[i]);\n");
            wrapper.append("                    if (i < result.length - 1) sb.append(\" \");\n");
            wrapper.append("                }\n");
            wrapper.append("                System.out.println(sb.toString());\n");
            wrapper.append("            }\n");
        } else if (isPalindrome) {
            // 处理回文字符串或数字判断问题
            wrapper.append("            // 解析输入（可能是字符串或整数）\n");
            wrapper.append("            boolean result;\n");
            wrapper.append("            if (userCode.contains(\"isPalindrome(String\")) {\n");
            wrapper.append("                // 字符串回文\n");
            wrapper.append("                result = solution.isPalindrome(line);\n");
            wrapper.append("            } else {\n");
            wrapper.append("                // 数值回文\n");
            wrapper.append("                int num = Integer.parseInt(line);\n");
            wrapper.append("                result = solution.isPalindrome(num);\n");
            wrapper.append("            }\n");
            wrapper.append("            System.out.println(result);\n");
        } else if (isMaxSubArray) {
            // 处理最大子数组和问题
            wrapper.append("            // 解析输入：空格分隔的数组\n");
            wrapper.append("            String[] parts = line.split(\" \");\n");
            wrapper.append("            int[] nums = new int[parts.length];\n");
            wrapper.append("            for (int i = 0; i < parts.length; i++) {\n");
            wrapper.append("                nums[i] = Integer.parseInt(parts[i]);\n");
            wrapper.append("            }\n");
            wrapper.append("            int result = solution.maxSubArray(nums);\n");
            wrapper.append("            System.out.println(result);\n");
        } else if (isReverseString) {
            // 处理字符串反转问题
            wrapper.append("            // 直接使用输入行作为字符串\n");
            wrapper.append("            String result = solution.reverse(line);\n");
            wrapper.append("            System.out.println(result);\n");
        } else if (isFibonacci) {
            // 处理斐波那契数列问题
            wrapper.append("            // 解析输入为单个数字\n");
            wrapper.append("            int n = Integer.parseInt(line);\n");
            wrapper.append("            int result;\n");
            wrapper.append("            if (userCode.contains(\"fibonacci\")) {\n");
            wrapper.append("                result = solution.fibonacci(n);\n");
            wrapper.append("            } else {\n");
            wrapper.append("                result = solution.fib(n);\n");
            wrapper.append("            }\n");
            wrapper.append("            System.out.println(result);\n");
        } else if (isSortArray) {
            // 处理数组排序问题
            wrapper.append("            // 解析输入：空格分隔的数组\n");
            wrapper.append("            String[] parts = line.split(\" \");\n");
            wrapper.append("            int[] nums = new int[parts.length];\n");
            wrapper.append("            for (int i = 0; i < parts.length; i++) {\n");
            wrapper.append("                nums[i] = Integer.parseInt(parts[i]);\n");
            wrapper.append("            }\n");
            wrapper.append("            \n");
            wrapper.append("            int[] result;\n");
            wrapper.append("            if (userCode.contains(\"public int[] sort\")) {\n");
            wrapper.append("                // 返回新数组的排序方法\n");
            wrapper.append("                result = solution.sort(nums);\n");
            wrapper.append("            } else {\n");
            wrapper.append("                // void 排序方法，修改原数组\n");
            wrapper.append("                solution.sort(nums);\n");
            wrapper.append("                result = nums;\n");
            wrapper.append("            }\n");
            wrapper.append("            \n");
            wrapper.append("            // 输出结果\n");
            wrapper.append("            StringBuilder sb = new StringBuilder();\n");
            wrapper.append("            for (int i = 0; i < result.length; i++) {\n");
            wrapper.append("                sb.append(result[i]);\n");
            wrapper.append("                if (i < result.length - 1) sb.append(\" \");\n");
            wrapper.append("            }\n");
            wrapper.append("            System.out.println(sb.toString());\n");
        } else {
            // 通用处理逻辑 - 尝试解析方法名和参数
            wrapper.append("            // 通用处理逻辑\n");
            wrapper.append("            String[] inputs = line.split(\" \");\n");
            wrapper.append("            \n");
            wrapper.append("            // 解析用户代码，寻找主要方法\n");
            wrapper.append("            String methodCall = findMainMethod(userCode, inputs);\n");
            wrapper.append("            if (methodCall != null) {\n");
            wrapper.append("                // 使用反射调用用户方法\n");
            wrapper.append("                try {\n");
            wrapper.append("                    Class<?> solutionClass = Solution.class;\n");
            wrapper.append("                    Object result = solutionClass.getMethod(methodCall).invoke(solution);\n");
            wrapper.append("                    System.out.println(result);\n");
            wrapper.append("                } catch (Exception e) {\n");
            wrapper.append("                    System.err.println(\"无法调用方法: \" + e.getMessage());\n");
            wrapper.append("                }\n");
            wrapper.append("            } else {\n");
            wrapper.append("                // 无法确定方法，输出提示\n");
            wrapper.append("                System.out.println(\"无法确定要调用的方法，请提供具体测试代码\");\n");
            wrapper.append("            }\n");
        }
        
        wrapper.append("        } catch (Exception e) {\n");
        wrapper.append("            System.err.println(\"执行过程出错: \" + e.getMessage());\n");
        wrapper.append("            e.printStackTrace();\n");
        wrapper.append("        }\n");
        wrapper.append("    }\n\n");
        
        // 添加通用的方法查找辅助函数
        wrapper.append("    // 尝试从用户代码中找到主要方法\n");
        wrapper.append("    private static String findMainMethod(String code, String[] inputs) {\n");
        wrapper.append("        // 简单实现，实际应考虑更复杂的代码解析\n");
        wrapper.append("        // 在这里实现方法检测逻辑\n");
        wrapper.append("        return null; // 返回方法名\n");
        wrapper.append("    }\n");
        
        wrapper.append("}\n");
        
        return wrapper.toString();
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
        // 注意：现在我们使用Main类而不是Solution类
        String className = "Main";
        
        // 创建Volume
        Volume volume = new Volume("/code");
        
        // 记录开始时间
        long startTime = System.currentTimeMillis();
        
        // 创建容器 - 运行Main类
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
        
        // 计算执行时间
        long executeTime = System.currentTimeMillis() - startTime;
        
        // 获取容器统计信息以计算内存使用
        long memoryUsage = 0L;
        try {
            memoryUsage = getContainerMemoryUsage(container.getId());
        } catch (Exception e) {
            log.warn("获取容器内存使用统计信息失败: {}", e.getMessage());
        }
        
        // 获取结果
        ExecutionResult result = getExecutionResult(workspacePath, timedOut, executeTime, memoryUsage);
        
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
        
        // 记录开始时间
        long startTime = System.currentTimeMillis();
        
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
        
        // 计算执行时间
        long executeTime = System.currentTimeMillis() - startTime;
        
        // 获取容器统计信息以计算内存使用
        long memoryUsage = 0L;
        try {
            memoryUsage = getContainerMemoryUsage(container.getId());
        } catch (Exception e) {
            log.warn("获取容器内存使用统计信息失败: {}", e.getMessage());
        }
        
        // 获取结果
        ExecutionResult result = getExecutionResult(workspacePath, timedOut, executeTime, memoryUsage);
        
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
        
        // 记录开始时间
        long startTime = System.currentTimeMillis();
        
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
        
        // 计算执行时间
        long executeTime = System.currentTimeMillis() - startTime;
        
        // 获取容器统计信息以计算内存使用
        long memoryUsage = 0L;
        try {
            memoryUsage = getContainerMemoryUsage(container.getId());
        } catch (Exception e) {
            log.warn("获取容器内存使用统计信息失败: {}", e.getMessage());
        }
        
        // 获取结果
        ExecutionResult result = getExecutionResult(workspacePath, timedOut, executeTime, memoryUsage);
        
        // 移除容器
        dockerClient.removeContainerCmd(container.getId()).exec();
        
        return result;
    }
    
    /**
     * 获取容器内存使用
     */
    private long getContainerMemoryUsage(String containerId) {
        try {
            // 使用Docker命令行执行stats命令获取内存使用情况
            // 创建一个容器执行docker stats命令
            CreateContainerResponse statsContainer = dockerClient.createContainerCmd("docker:stable")
                    .withHostConfig(HostConfig.newHostConfig()
                            .withPrivileged(true)
                            .withBinds(new Bind("/var/run/docker.sock", new Volume("/var/run/docker.sock"))))
                    .withCmd("sh", "-c", "docker stats " + containerId + " --no-stream --format {{.MemUsage}} | awk '{print $1}'")
                    .exec();
            
            // 启动容器
            dockerClient.startContainerCmd(statsContainer.getId()).exec();
            
            // 等待容器完成
            WaitContainerResultCallback waitCallback = new WaitContainerResultCallback();
            dockerClient.waitContainerCmd(statsContainer.getId()).exec(waitCallback);
            waitCallback.awaitCompletion();
            
            // 获取容器输出
            LogContainerResultCallback logCallback = new LogContainerResultCallback();
            dockerClient.logContainerCmd(statsContainer.getId())
                    .withStdOut(true)
                    .exec(logCallback);
            logCallback.awaitCompletion();
            
            // 解析内存使用结果
            String memoryUsageOutput = logCallback.toString().trim();
            log.info("容器内存使用输出: {}", memoryUsageOutput);
            
            // 移除stats容器
            dockerClient.removeContainerCmd(statsContainer.getId()).exec();
            
            // 解析内存使用值（假设格式为"123.4MiB"或"456.7KiB"）
            if (memoryUsageOutput.isEmpty()) {
                return 0L; // 返回默认值
            }
            
            // 提取数字部分和单位
            String[] parts = memoryUsageOutput.split("[A-Za-z]");
            if (parts.length > 0) {
                double value = Double.parseDouble(parts[0]);
                
                // 根据单位转换为KB
                if (memoryUsageOutput.contains("MiB") || memoryUsageOutput.contains("MB")) {
                    return (long)(value * 1024); // MB转KB
                } else if (memoryUsageOutput.contains("GiB") || memoryUsageOutput.contains("GB")) {
                    return (long)(value * 1024 * 1024); // GB转KB
                } else if (memoryUsageOutput.contains("KiB") || memoryUsageOutput.contains("KB")) {
                    return (long)value; // 已经是KB
                } else if (memoryUsageOutput.contains("B")) {
                    return (long)(value / 1024); // B转KB
                }
            }
            
            return 0L; // 解析失败返回默认值
        } catch (Exception e) {
            log.error("获取容器内存使用失败", e);
            return 0L;
        }
    }
    
    /**
     * 从工作目录读取执行结果
     */
    private ExecutionResult getExecutionResult(String workspacePath, boolean timedOut, long executeTime, long memoryUsage) {
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
                        .executeTime(executeTime)
                        .memoryUsage(memoryUsage)
                        .build();
            }
            
            if (!errorMessage.isEmpty()) {
                return ExecutionResult.builder()
                        .success(false)
                        .status(JudgeConstant.STATUS_RUNTIME_ERROR)
                        .output(output)
                        .errorMessage(errorMessage)
                        .executeTime(executeTime)
                        .memoryUsage(memoryUsage)
                        .build();
            }
            
            return ExecutionResult.builder()
                    .success(true)
                    .status(JudgeConstant.STATUS_ACCEPTED)
                    .output(output)
                    .errorMessage("")
                    .executeTime(executeTime)
                    .memoryUsage(memoryUsage)
                    .build();
            
        } catch (IOException e) {
            log.error("读取执行结果失败", e);
            return ExecutionResult.builder()
                    .success(false)
                    .status(JudgeConstant.STATUS_SYSTEM_ERROR)
                    .errorMessage("读取执行结果失败: " + e.getMessage())
                    .executeTime(executeTime)
                    .memoryUsage(memoryUsage)
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