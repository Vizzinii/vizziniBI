package com.yupi.springbootinit.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 * 线程池配置
 *
 * @author <a href="https://github.com/Vizzinii">济楚</a>
 */
@Configuration
public class ThreadPoolExcutorConfig {

    /**
     * CorePoolSize 核心线程数（随时就绪的线程数）
     * MaximumPoolSize 最大线程数（最大可加到多少线程）
     * KeepAliveTime 空闲线程存活时间（非核心线程在没有执行任务时，被删除的时间阈值）
     * TimeUnit 空闲线程存活时间的单位
     * WorkQueue 任务队列（其中存放的是需要线程执行的任务）
     * ThreadFactory 线程工厂（控制每个线程的生成、线程的属性）
     * RejectdExecutionHandle 拒绝策略（当任务队列满时，对新任务执行的措施）
     * @return
     */
    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {

        // 建立一个线程工厂
        ThreadFactory threadFactory = new ThreadFactory() {

            // 第一个线程是1号
            private int count = 1;

            @Override
            public Thread newThread(@NotNull Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("线程"+(count++)+"号");
                return thread;
            }
        };

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2,4,100,
                TimeUnit.SECONDS,new ArrayBlockingQueue<>(4),threadFactory);

        return threadPoolExecutor;
    }
}
