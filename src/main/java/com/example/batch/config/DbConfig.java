package com.example.batch.config;

import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@MapperScan(value = "com.example.batch", sqlSessionFactoryRef = "mainSqlSessionFactory")
@Profile("main")
public class DbConfig {
	
    @Value("${spring.datasource.mapper-locations}")
    String mPath;

    // mybatis 설정 파일을 따로 작성해서 임포트할 예정 - snake_case -> camelCase 를 위함
    @Value("${mybatis.config-location}")
    String mybatisConfigPath;

    @Bean(name = "mainHikariConfig")
    @ConfigurationProperties(prefix = "spring.datasource")
    public HikariConfig hikariConfig() {
        return new HikariConfig();
    }

    @Bean(name = "mainDataSource")
    public DataSource DataSource() {
        DataSource dataSource = new HikariDataSource(hikariConfig());
        return dataSource;
    }


    @Bean(name = "mainSqlSessionFactory")
    public SqlSessionFactory SqlSessionFactory(@Qualifier("mainDataSource") DataSource DataSource, ApplicationContext applicationContext) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(DataSource);
        sqlSessionFactoryBean.setMapperLocations(applicationContext.getResources(mPath));
        sqlSessionFactoryBean.setConfigLocation(applicationContext.getResource(mybatisConfigPath));
        return sqlSessionFactoryBean.getObject();
    }

    @Bean(name = "mainSessionTemplate")
    public SqlSessionTemplate SqlSessionTemplate(@Qualifier("mainSqlSessionFactory") SqlSessionFactory firstSqlSessionFactory) {
        return new SqlSessionTemplate(firstSqlSessionFactory);
    }
    
    /**
     * transaction manager
     */
    @Bean(name= "mainTxManager")
    // 트랜잭션 관리
    public PlatformTransactionManager txManager(@Qualifier("mainDataSource") DataSource dataSource) {
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager(dataSource);
        dataSourceTransactionManager.setNestedTransactionAllowed(true); // nested
        return dataSourceTransactionManager;
    }

}