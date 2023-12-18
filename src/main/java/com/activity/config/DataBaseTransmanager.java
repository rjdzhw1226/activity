package com.activity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

import javax.annotation.Resource;

@Configuration
@EnableTransactionManagement
public class DataBaseTransmanager implements TransactionManagementConfigurer {
  @Resource
  private DruidConfig druidConfig;

  @Override
  @Bean(name = "transactionManager")
  public PlatformTransactionManager annotationDrivenTransactionManager() {
    DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
    //设置事务管理器管理的数据源
    transactionManager.setDataSource(druidConfig.druidDataSource());
    return transactionManager;
  }
}
