package com.system.batch.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
public class SystemFailureJobConfig {
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public Job SystemFailureJob(Step systemFailureStep) {
        return new JobBuilder("systemFailureJob", jobRepository)
                .start(systemFailureStep)
                .build();
    }

    @Bean
    public Step systemFailureStep(
            FlatFileItemReader<SystemFailure> systemFailureItemReader,
            SystemFailureStdoutItemWriter systemFailureStdoutItemWriter
    ) {
        return new StepBuilder("systemFailureStep", jobRepository)
                .<SystemFailure, SystemFailure>chunk(10, transactionManager)
                .reader(systemFailureItemReader)
                .writer(systemFailureStdoutItemWriter)
                .build();
    }


    /**
     * CSV 파일을 읽어 한 줄씩 SystemFailure 객체로 변환하는 FlatFileItemReader 구성
     *
     * 구성 요약
     * - name           : Reader 식별용 이름
     * - resource       : 입력 파일 경로 (jobParameter로 주입)
     * - delimited      : 구분자 기반(line tokenizer) 모드 활성화
     * - delimiter(",") : 필드 구분자(쉼표). CSV면 기본이지만 명시적 지정 권장
     * - names(...)     : CSV 컬럼 순서를 객체 필드명과 매핑 (1:1 순서 대응)
     * - targetType     : 매핑 대상 타입 지정(BeanWrapperFieldSetMapper 사용)
     * - linesToSkip(1) : 첫 줄(헤더) 스킵
     * - strict         : 파일/컬럼 일치성 검사 강도(기본 true, 필요 시 설정)
     */
//    @Bean
//    @StepScope
//    public FlatFileItemReader<SystemFailure> systemFailureItemReader(
//            @Value("#{jobParameters['inputFile']}") String inputFile // 입력 CSV 파일 경로
//    ) {
//        return new FlatFileItemReaderBuilder<SystemFailure>()
//                .name("systemFailureItemReader")               // 배치 모니터링 시 식별 가능한 이름
//                .resource(new FileSystemResource(inputFile))   // 파일 리소스 지정
//                .delimited()                                   // 구분자 기반 토크나이저 사용
//                .delimiter(",")                                // 구분자: ',' (CSV)
//                .names(                                        // CSV 컬럼 → 객체 프로퍼티 매핑 (순서 중요)
//                        "errorId",
//                        "errorDateTime",
//                        "severity",
//                        "processId",
//                        "errorMessage"
//                )
//                .targetType(SystemFailure.class)               // FieldSet → SystemFailure 변환 대상 타입
//                .linesToSkip(1)                                // 헤더 라인 제거(1줄)
//                // .comments("#")                              // '#'로 시작하는 줄을 주석으로 간주(선택 사항)
//                // .strict(true)                               // 기본 true: 파일 없거나 컬럼 수 불일치 시 예외
//                .build();
//    }

    @Bean
    @StepScope
    public FlatFileItemReader<SystemFailure> systemFailureItemReader(
            @Value("#{jobParameters['inputFile']}") String inputFile){
        return new FlatFileItemReaderBuilder<SystemFailure>()
                .name("systemFailureItemReader")
                .resource(new FileSystemResource(inputFile))
                .fixedLength() // DefaultLineMapper 가 사용할 LineTokenizer 구현체로 FixedLengthTokenizer 지정
                .columns(new Range[]{
                        new Range(1, 8),    // errorId: ERR001 + 공백 2칸
                        new Range(9, 29),   // errorDateTime: 날짜시간 + 공백 2칸
                        new Range(30, 39),  // severity: CRITICAL/FATAL + 패딩
                        new Range(40, 45),  // processId: 1234 + 공백 2칸
                        new Range(46, 66)   // errorMessage: 메시지 + \n
                })
                .names("errorId", "errorDateTime", "severity" , "processId", "errorMessage")
                .targetType(SystemFailure.class)
//                .strict(true)  // true: 파일에서 읽은 랑니의 길이가 Range에 지정된 최대 길이와 다를 경우 예외 발생함
                 .build();
    }

    @Bean
    public SystemFailureStdoutItemWriter systemFailureStdoutItemWriter() {
        return new SystemFailureStdoutItemWriter();
    }

    public static class SystemFailureStdoutItemWriter implements ItemWriter<SystemFailure> {
        @Override
        public void write(Chunk<? extends SystemFailure> chunk) throws Exception {
            for (SystemFailure failure : chunk) {
                log.info("Processing system failure: {}", failure);
            }
        }
    }

    @Data
    public static class SystemFailure {
        private String errorId;
        private String errorDateTime;
        private String severity;
        private String processId;
        private String errorMessage;
    }
}
