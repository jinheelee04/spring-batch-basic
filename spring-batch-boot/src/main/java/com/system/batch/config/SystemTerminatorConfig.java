package com.system.batch.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * JobParameters
 * -> 배치 작업에 전달되는 입력 값, Job을 동적이고 유연하게 만들어주는 도구
 * 표기법
 * parameterName=parameterValue,parameterType,identificationFlag
 * parameterName: 배치 Job에서 파라미터를 찾을 때 사용할 key 값
 * parameterValue: 파라미터의 실제 값
 * parameterType: 파라미터의 타입(java.lang.String와 같은 fully qualified name 사용). 디폴트 String
 * identificationFlag: 해당 파라미터가 JobInstance 식별(identification)에 사용될 파라미터인지 여부를 전달하는 값(디폴트 true)
 *
 * 커맨드라인 실행 방법
 * gradlew bootRun --args='--spring.batch.job.name=processTerminatorJob terminatorId=KILL-9,java.lang.String targetCount=5,java.lang.Integer'
 */
@Slf4j
@Configuration
public class SystemTerminatorConfig {
    @Bean
    public Job processTerminatorJob(JobRepository jobRepository, Step terminationStep) {
        return new JobBuilder("processTerminatorJob", jobRepository)
                .start(terminationStep)
                .build();
    }

    @Bean
    public Step terminationStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, Tasklet terminatorTasklet) {
        return new StepBuilder("terminationStep", jobRepository)
                .tasklet(terminatorTasklet, transactionManager)
                .build();
    }

    /**
     * @Value를 사용해 잡 파라미터를 전달받으려면 @StepScope 어노테이션을 선언해야 한다.
     */
    @Bean
    @StepScope
    public Tasklet terminatorTasklet(
            @Value("#{jobParameters['terminatorId']}") String terminatorId,
            @Value("#{jobParameters['targetCount']}") Integer targetCount
    ) {
        return (contribution, chunkContext) -> {
            log.info("시스템 종결자 정보:");
            log.info("ID: {}", terminatorId);
            log.info("제거 대상 수: {}", targetCount);
            log.info("⚡ SYSTEM TERMINATOR {} 작전을 개시합니다.", terminatorId);
            log.info("☠️ {}개의 프로세스를 종료합니다.", targetCount);
            for(int i = 1; i <= targetCount; i++){
                log.info("💀 프로세스 {} 종료 완료!", i);
            }

            log.info("🎯 임무 완료: 모든 대상 프로세스가 종료되었습니다.");
            return RepeatStatus.FINISHED;
        };
    }
}
