package com.system.batch.config;


import com.system.batch.listener.ServerRackControlListener;
import com.system.batch.listener.ServerRoomInfiltrationListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
public class ServerRoomInfiltrationConfig {
    
    @Bean
    public Job serverRoomControlJob(JobRepository jobRepository, Step serverRackControlStep){
        return new JobBuilder("serverRoomControlJob", jobRepository)
                .listener(new ServerRoomInfiltrationListener())
                .start(serverRackControlStep)
                .build();
    }
    
    @Bean
    public Step serverRackControlStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, Tasklet destructiveTasklet){
        return new StepBuilder("serverRackControlStep", jobRepository)
                .tasklet(destructiveTasklet(), transactionManager)
                .listener(new ServerRackControlListener())
                .build();
        
    }
    
    @Bean
    public Tasklet destructiveTasklet(){
        return (contribution, chunkContext)->{
            log.info("destructive tasklet 실행중");
            return RepeatStatus.FINISHED;
        };
    }

}
