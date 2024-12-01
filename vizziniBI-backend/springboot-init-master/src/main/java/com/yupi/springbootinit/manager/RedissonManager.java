package com.yupi.springbootinit.manager;

import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 限流操作
 *
 * @author <a href="https://github.com/Vizzinii">济楚</a>
 *
 */
/**
 * Manager 是自定义的通用代码，用来提供可公用的服务模块，是可以加入基础模板的高可复用性模块，无具体逻辑
 * Service 是结合项目而编写的逻辑，具有较强的不可复用性
 */
@Component
public class RedissonManager {

    @Resource
    private RedissonClient redissonClient;

    public boolean doRateLimit(String key) {
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        //限流方式： 令牌限流
        // RateType.OVERALL，表示这个速率是全局的，即所有请求共享这个速率
        // 单位时间产生2个令牌，单位时间检查一次，单位时间为秒
        rateLimiter.trySetRate(RateType.OVERALL, 2, 1, RateIntervalUnit.SECONDS);
        // 设置每个请求消耗1个令牌
        // 若成功获取，则说明没有超过速率限制，返回 true ；若获取失败（即没有足够的令牌），说明此时请求超过了速率限制，返回 false
        // TODO 可以让普通用户每次请求消耗2个令牌，VIP用户每次请求消耗一个令牌，这样就能实现基本的分级
        boolean tried = rateLimiter.tryAcquire(1);
        if (!tried) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST);
        }
        return tried;
    }

}