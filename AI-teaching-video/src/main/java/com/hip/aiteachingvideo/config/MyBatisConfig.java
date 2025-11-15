package com.hip.aiteachingvideo.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * mybatis-plus 配置
 */
@Configuration
public class MyBatisConfig {
    @Bean
    public MybatisPlusInterceptor paginationInterceptor() {

        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        //这是分页拦截器
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();

        paginationInnerInterceptor.setOverflow(false);
        paginationInnerInterceptor.setMaxLimit(500L);
        mybatisPlusInterceptor.addInnerInterceptor(paginationInnerInterceptor);
        //设置请求的页面大于最大页后操作，true调回到首页，false继续请求默认false
        // paginationInterceptor.setOverflow(false);//设置最大单页限制数量，默认500条，-1不受限制
        //paginationInterceptor.setLimit(500);
        //开启 count 的 join 优化,只针对部分 left join
        return mybatisPlusInterceptor;
    }

}