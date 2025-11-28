package com.system.batch.killbatchsystem.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * SystemTerminationConfig
 * - Spring Batch Job 설정 클래스
 *
 * @Import(BatchConfig.class)
 * - BatchConfig에서 정의한 배치 관련 Bean(JobRepository, TransactionManager 등)을 가져와 사용.
 */
@Import(BatchConfig.class)
public class SystemTerminationConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private AtomicInteger processKilled = new AtomicInteger(0);
    private final int TERMINATION_TARGET = 5;


    /**
     * 생성자
     * - JobRepository: Job 실행 이력 및 메타데이터 관리
     * - PlatformTransactionManager: Step 트랜잭션 관리
     */
    public SystemTerminationConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }


    /**
     * Job 설정
     * - @Bean: Spring 컨테이너에 Job 등록
     * - JobBuilder: Job 이름과 JobRepository 지정
     * - Step 실행 순서: enterWorld → meetNPC → defeatProcess → completeQuest
     */
    @Bean
    public Job systemTerminationSimulationJob() {
        return new JobBuilder("systemTerminationSimulationJob", jobRepository)
                .start(enterWorldStep())
                .next(meetNPCStep())
                .next(defeatProcessStep())
                .next(completeQuestStep())
                .build();
    }


    /**
     * Step: 시뮬레이션 세계 접속
     * - StepBuilder: Step 이름과 JobRepository 지정
     * - tasklet: Step 동작 정의, RepeatStatus로 종료 여부 결정
     */

    @Bean
    private Step enterWorldStep() {
        return new StepBuilder("enterWorldStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("System Termination 시뮬레이션 세계에 접속했습니다.");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    /**
     * Step: NPC 만남 및 미션 안내
     */
    @Bean
    private Step meetNPCStep() {
        return new StepBuilder("meetNPCStep", jobRepository)
                .tasklet((contribution, chunkContext)->{
                    System.out.println("시스템 관리자 NPC를 만났습니다.");
                    System.out.println("첫 번쨰 미션: 좀비 프로세스" + TERMINATION_TARGET + "개 처형하기");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }


    /**
     * Step: 좀비 프로세스 처형
     * - 목표 개수 도달 전까지 반복 실행
     */
    @Bean
    private Step defeatProcessStep() {
        return new StepBuilder("defeatProcessStep", jobRepository)
                .tasklet((contribution, chunkContext) ->{
                    int terminated = processKilled.incrementAndGet();
                    System.out.println("좀비 프로세스 처형 완료! (현재 " + terminated + "/" + TERMINATION_TARGET + ")");
                    if(terminated < TERMINATION_TARGET){
                        return RepeatStatus.CONTINUABLE;
                    }else{
                        return RepeatStatus.FINISHED;
                    }
                }, transactionManager)
                .build();
    }


    /**
     * Step: 미션 완료 및 보상 안내
     */
    @Bean
    private Step completeQuestStep() {
        return new StepBuilder("completeQuestStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("미션 완료! 좀비 프로세스 " + TERMINATION_TARGET + "개 처형 성공!");
                    System.out.println("보상: kill -9 권한 획득, 시스템 제어 레벨 1 달성");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

}
