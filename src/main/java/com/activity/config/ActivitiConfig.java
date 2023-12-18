package com.activity.config;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.boot.AbstractProcessEngineAutoConfiguration;
import org.activiti.spring.boot.ProcessEngineConfigurationConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;

@Configuration
public class ActivitiConfig extends AbstractProcessEngineAutoConfiguration {

  @Bean
  public SpringProcessEngineConfiguration springProcessEngineConfiguration(DataSource dataSource, DataSourceTransactionManager transactionManager) {
    SpringProcessEngineConfiguration spec = new SpringProcessEngineConfiguration();
    spec.setDataSource(dataSource);
    transactionManager.setDataSource(dataSource);
    spec.setTransactionManager(transactionManager);
    spec.setDatabaseSchemaUpdate("true");
    Resource[] resources = null;
    // 启动自动部署流程
    try {
      resources = new PathMatchingResourcePatternResolver().getResources("classpath*:processes/*.bpmn");
    } catch (IOException e) {
      e.printStackTrace();
    }
    spec.setDeploymentResources(resources);
    return spec;
  }

  @Bean
  public ProcessEngineFactoryBean processEngine(SpringProcessEngineConfiguration engineConfiguration) {
    ProcessEngineFactoryBean processEngineFactoryBean = new ProcessEngineFactoryBean();
    engineConfiguration.setActivityFontName("宋体");
    engineConfiguration.setAnnotationFontName("宋体");
    engineConfiguration.setLabelFontName("宋体");
    processEngineFactoryBean.setProcessEngineConfiguration(engineConfiguration);
    return processEngineFactoryBean;
  }


  @Bean
  public RepositoryService repositoryService(ProcessEngineFactoryBean engineFactoryBean) throws Exception {
    return engineFactoryBean.getObject().getRepositoryService();
  }

  @Bean
  public RuntimeService runtimeService(ProcessEngineFactoryBean engineFactoryBean) throws Exception {
    return engineFactoryBean.getObject().getRuntimeService();
  }

  @Bean
  public TaskService taskService(ProcessEngineFactoryBean engineFactoryBean) throws Exception {
    return engineFactoryBean.getObject().getTaskService();
  }

  @Bean
  public HistoryService historyService(ProcessEngineFactoryBean engineFactoryBean) throws Exception {
    return engineFactoryBean.getObject().getHistoryService();
  }
}
