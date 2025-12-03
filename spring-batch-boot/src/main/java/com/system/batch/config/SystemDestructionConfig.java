package com.system.batch.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
public class SystemDestructionConfig {
    
    @Bean
    public Job systemDestructionJob(
            JobRepository jobRepository,
            Step systemDestructionStep
    ){
        return new JobBuilder("systemDestructionJob", jobRepository)
                .validator(new DefaultJobParametersValidator(
                        new String[]{"destructionPower"}, // 필수 파라미터
                        new String[]{"targetSystem"}   // 선택적 파라미터
                ))
                .start(systemDestructionStep)
                .build();
    }
    
    @Bean
    public Step systemDestructionStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, Tasklet systemDestructionTasklet){
        return new StepBuilder("systemDestructionStep", jobRepository)
                .tasklet(systemDestructionTasklet, transactionManager)
                .build();
    }
    
    @Bean
    @StepScope
    public Tasklet systemDestructionTasklet(
            @Value("#{jobParameters['destructionPower']}") long destructionPower
    ){
        return (contribution, chunkContext) -> {
            log.info("파괴력 수준: {}", destructionPower);
            return RepeatStatus.FINISHED;
        };
    }
}
