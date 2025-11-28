package com.system.batch.killbatchsystem.config;

import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * BatchConfig
 * Spring Batch 기본 설정 클래스.
 *
 * DefaultBatchConfiguration:
 * - Spring Batch의 핵심 컴포넌트(JobRepository, JobLauncher 등)를 자동 구성.
 * - 별도의 JobBuilderFactory, StepBuilderFactory 없이 Job/Step을 정의할 수 있음.
 */

@Configuration
public class BatchConfig extends DefaultBatchConfiguration {

    /**
     * DataSource 설정
     * Spring Batch는 Job과 Step 실행 정보를 메타데이터 테이블에 저장하므로
     * 데이터베이스가 반드시 필요하다.
     * 여기서는 H2 인메모리 DB를 사용하며, Spring Batch에서 제공하는
     * 기본 스키마(schema-h2.sql)를 로드한다.
     *
     * @return H2 Embedded Database
     */
    @Bean
    public DataSource dataSource(){
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .addScript("org/springframework/batch/core/schema-h2.sql")
                .build();
    }

    /**
     * 트랜잭션 매니저 설정
     * Spring Batch의 모든 작업(Job, Step)은 트랜잭션 내에서 실행된다.
     * 따라서 DataSource 기반의 PlatformTransactionManager를 등록하여
     * JobRepository 및 Step 실행 시 트랜잭션을 관리한다.
     *
     * @return DataSourceTransactionManager
     */

    @Bean
    public PlatformTransactionManager transactionManager(){
        return new DataSourceTransactionManager(dataSource());
    }
}
