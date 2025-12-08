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
     * FlatFileItemReader 구성
     * .name() : Reader 식별자 설정
     * .resource() : 처리 대상 파일 지정
     * .delimited() : 구분자 기반 파일 읽기 모드 활성화
     * .delimiter(",") : 쉼표로 데이터 구분
     * .names() : 각 필드 식별자(SystemFailure의 프로퍼티 이름) 매핑
     * .targetType() : 변환 대상 객체(SystemFailure) 지정
     * .linesToSkip() : 헤더 라인 제거
     * .strict(): 엄격한 규율 적용
     */
    @Bean
    @StepScope
    public FlatFileItemReader<SystemFailure> systemFailureReader(
            @Value("#{jobParameters['inputFile']}") String inputFile // 타겟 파일 경로
    ) {
        return new FlatFileItemReaderBuilder<SystemFailure>() // 생성될 FlatFileItemReader가 파일에서 읽은 한 줄의 데이터를 SystemFailure 객체로 변환하도록 지정
                .name("systemFailureItemReader") // FlatFileItemReader를 식별하기 위한 고유 이름
                .resource(new FileSystemResource(inputFile)) 
                .delimited()// FlatFileItemReader에게 읽어들일 파일이 구분자로 분리된 형식임을 알리는 설정, DefaultLineMapper가 사용할 LineTokenizer 구현체로 DelimitedLineTokenizer가 지정됨
                .delimiter(",") // 구분자 문자 지정 (DelimitedLineTokenizer의 기본 구분자가 쉼표이기 때문에, CSV 파일을 처리할 때는 생략 가능하지만 코드의 명시성을 위해 구분자를 직접 지정하는 것을 권장)
                .names("errorId",
                        "errorDateTime",
                        "severity",
                        "processId",
                        "errorMessage"
                ) // FieldSet의 names 필드에 사용할 객체의 프로퍼티 이름을 전달(데이터의 각 토큰과 순서대로 1:1 매핑됨)
                .targetType(SystemFailure.class) // 매핑 대상 클래스 지정, 기본으로 사용되는 FieldSetMapper 구현체인 BeanWrapperFieldSetMapper에서 FieldSet을 객체로 매핑할 대상 도메인 클래스를 지정
                .linesToSkip(1) // 파일 헤더 라인 건너뛰기(첫 번째 줄 건너뛰고 두 번째 줄부터 실제 데이터로 처리한다)
//                .comments("#") // 특정 문자로 시작하는 라인을 주석으로 처리, 기본값 '#'
//                .strict(true)
// 파일 검증 강도 설정, 기본값 true
// true: 파일 누락 or tokens의 길이가 names()에 전달된 객체 프로퍼티 이름의 길이와 다를 경우 예외를 발생,
// false: 파일이 존재하지 않아도 경고만 남기고 진행, 토큰수를 자동 보정
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
