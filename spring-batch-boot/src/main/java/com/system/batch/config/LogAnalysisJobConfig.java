package com.system.batch.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.transform.RegexLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@AllArgsConstructor
@Configuration
public class LogAnalysisJobConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job logAnalysisJob(Step logAnalysisStep){
        return new JobBuilder("logAnalysisJob", jobRepository)
                .start(logAnalysisStep)
                .build();
    }

    @Bean
    public Step logAnalysisStep(
            FlatFileItemReader<LogEntry> logItemReader,
            ItemWriter<LogEntry> logItemWriter
    ) {
        return new StepBuilder("logAnalysisStep", jobRepository)
                .<LogEntry, LogEntry>chunk(10, transactionManager)
                .reader(logItemReader)
                .writer(logItemWriter)
                .build();
    }

    /**
     * \\[\\\\w+\\]: 대괄호 안의 로그 레벨(예: WARNING, ERROR)을 패턴으로 매칭한다. 이 부분은 분석 대상에서 제외된다.
     * \\[Thread-(\\\\d+)\\]: 스레드 번호에 해당하는 두 번째 대괄호 안에서 Thread- 뒤에 나오는 숫자가 첫 번째 그룹으로 캡처된다.
     * \\[CPU: \\\\d+%\\]: CPU 사용량을 나타내는 부분이다. 이건 로그 메시지 파싱엔 필요 없으니 건너뛴다.
     * (.+): 마지막으로 로그 메시지를 전부 가져오는 부분이다. 이게 두 번째 그룹으로 캡처된다.
     *  ** 괄호로 감싼 것이 캡처 그룹
     * 로그파일 예시
     * [WARNING][Thread-156][CPU: 78%] Thread pool saturation detected - 45/50 threads in use...
     */
    @Bean
    @StepScope
    public FlatFileItemReader<LogEntry> logItemReader (
        @Value("#{jobParameters['inputFile']}") String inputFile ){
        RegexLineTokenizer tokenizer = new RegexLineTokenizer();
        tokenizer.setRegex("\\[\\w+\\]\\[Thread-(\\d+)\\]\\[CPU: \\d+%\\] (.+)");

        return new FlatFileItemReaderBuilder<LogEntry>()
                .name("logItemReader")
                .resource(new FileSystemResource(inputFile))
                .lineTokenizer(tokenizer)
                // fieldSetMapper사용 시 targetType() 메서드는 사용 안함, 지정할 경우  fieldSetMapper() 설정 한 내용이 무시됨
                .fieldSetMapper(fieldSet -> new LogEntry(fieldSet.readString(0), fieldSet.readString(1)))
                .build();
    }

    @Bean
    public ItemWriter<LogEntry> logItemWriter(){
        return items -> {
            for (LogEntry logEntry: items) {
                log.info(String.format("THD-%s: %s", logEntry.getThreadNum(), logEntry.getMessage()));
            }
        };
    }

    @Data
    @AllArgsConstructor
    public static class LogEntry {
        private String threadNum;
        private String message;
    }
}
