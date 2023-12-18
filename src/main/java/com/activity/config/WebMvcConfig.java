package com.activity.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .addResourceLocations("classpath:/META-INF/resources/");
    }

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    //设置访问路径为 “/” 跳转到指定页面
    registry.addViewController("/").setViewName("forward:/index.html");
    //设置为最高优先级
    registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
  }
}
