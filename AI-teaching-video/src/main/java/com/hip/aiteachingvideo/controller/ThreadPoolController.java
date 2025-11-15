package com.hip.aiteachingvideo.controller;

import com.hip.aiteachingvideo.config.TaskPoolConfig;
import com.hip.aiteachingvideo.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/thread-pool")
public class ThreadPoolController {

    @Autowired
    @Qualifier("taskExecutor1")
    private ThreadPoolTaskExecutor executor;

    /**
     * 获取线程池状态
     *
     * @return 线程池状态
     */
    @GetMapping("/status")
    public Result getThreadPoolStatus() {
        Map<String, Object> status = new HashMap<>();

        status.put("poolName", executor.getThreadNamePrefix());
        status.put("corePoolSize", executor.getCorePoolSize());
        status.put("maxPoolSize", executor.getMaxPoolSize());
        status.put("activeThreads", executor.getActiveCount());
        status.put("currentPoolSize", executor.getPoolSize());
        status.put("completedTasks", executor.getThreadPoolExecutor().getCompletedTaskCount());
        status.put("queueSize", executor.getThreadPoolExecutor().getQueue().size());
        status.put("queueCapacity", executor.getThreadPoolExecutor().getQueue().remainingCapacity() +
                executor.getThreadPoolExecutor().getQueue().size());
        status.put("keepAliveSeconds", executor.getKeepAliveSeconds());

        return Result.ok(status);
    }
}