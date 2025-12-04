package com.system.batch.tasklet;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@Slf4j
public class ZombieProcessCleanupTasklet implements Tasklet {
    private final int processesToKill = 10;
    private int killedProcesses = 0;

    /**
     * ✅ Spring Batch Tasklet 개요
     * - Tasklet은 Step 내에서 단일 작업을 수행하는 컴포넌트입니다.
     * - 로직은 execute() 메서드에서 순차적으로 처리됩니다.
     *
     * ✅ 반복 여부 결정 방식
     * - execute()의 반환 값으로 반복 여부를 제어합니다.
     *   - RepeatStatus.FINISHED → Step 종료
     *   - RepeatStatus.CONTINUABLE → Step 반복 실행
     *
     * ✅ 왜 반복문 대신 RepeatStatus로 반복을 제어할까?
     * - 반복문을 사용하면 하나의 큰 트랜잭션으로 처리되므로,
     *   실행 중 예외 발생 시 이전에 처리한 모든 데이터가 롤백됩니다.
     * - RepeatStatus.CONTINUABLE을 사용하면 매 건 처리 후 트랜잭션이 커밋되므로,
     *   예외 발생 시 이미 처리한 데이터는 안전하게 유지됩니다.
     * - 즉, 큰 트랜잭션 대신 작은 트랜잭션 단위로 나누어 안정성을 확보하는 방식입니다.
     */
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        killedProcesses++;
        log.info("☠️  프로세스 강제 종료... ({}/{})", killedProcesses, processesToKill);

        if (killedProcesses >= processesToKill) {
            log.info("💀 시스템 안정화 완료. 모든 좀비 프로세스 제거.");
            return RepeatStatus.FINISHED;  // 모든 프로세스 종료 후 작업 완료
        }

        return RepeatStatus.CONTINUABLE;  // 아직 더 종료할 프로세스가 남아있음
    }
}