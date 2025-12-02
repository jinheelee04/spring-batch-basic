package com.system.batch.config;

import com.system.batch.tasklet.ZombieProcessCleanupTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class ZombieBatchConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    public ZombieBatchConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    /**
     * Tasklet Bean
     */
    @Bean
    public Tasklet zombieProcessCleanupTasklet(){
        return new ZombieProcessCleanupTasklet();
    }

    /**
     * Step 정의
     * -> Step을 생성할 때 가장 중요한 것은 "어떤 처리 방식으로 사용할 것인가"를 결정하는 것
     */
    @Bean
    public Step zombieCleanupStep(){
        return new StepBuilder("zombieCleanupStep", jobRepository)
                // Tasklet과 TransactionManager 설정
                .tasklet(zombieProcessCleanupTasklet(), transactionManager)
                .build();
    }

    /**
     * Job 정의
     */
    @Bean
    public Job zombieCleanJob() {
        return new JobBuilder("zombieCleanupJob", jobRepository)
                .start(zombieCleanupStep()) // Step 등록
                .build();
    }
}
