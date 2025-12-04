package com.system.batch.config;

import com.system.batch.listener.BigBrotherJobExecutionListener;
import com.system.batch.listener.BigBrotherStepExecutionListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
public class systemMonitoringConfig {

    @Bean
    public Job systemMonitoryJob(JobRepository jobRepository, Step monitoringStep){
        return new JobBuilder("systemMonitoringJob", jobRepository)
                .listener(new BigBrotherJobExecutionListener())
                .start(monitoringStep)
                .build();
    }

    @Bean
    public Step monitoringStep(JobRepository jobRepository, PlatformTransactionManager transactionManager){
        return new StepBuilder("monitoringStep", jobRepository)
                .listener(new BigBrotherStepExecutionListener())
                .tasklet((contribution, chunkContext)->{
                    log.info("monitoring step 실행중");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
