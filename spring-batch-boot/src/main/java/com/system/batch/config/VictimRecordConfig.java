package com.system.batch.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class VictimRecordConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;

    @Bean
    public Job processVictimJob() {
        return new JobBuilder("victimRecordJob", jobRepository)
                .start(processVictimStep())
                .build();
    }

    @Bean
    public Step processVictimStep() {
        return new StepBuilder("victimRecordStep", jobRepository)
                .<Victim, Victim> chunk(5, transactionManager)
                .reader(terminatedVictimReader())
                .writer(victimWriter())
                .build();
    }

    /*
     * 1. Cursor 방식
     */
//    @Bean
//    public JdbcCursorItemReader<Victim> terminatedVictimReader() {
//        return new JdbcCursorItemReaderBuilder<Victim>()
//                .name("terminatedVictimReader")
//                .dataSource(dataSource)
//                .sql("SELECT * FROM victims WHERE status = ? AND terminated_at <= ?")
//                .queryArguments(List.of("TERMINATED", LocalDateTime.now())) //  내부적으로 ArgumentPreparedStatementSetter가 사용되어 안전하게 값을 바인딩해줌
////                .beanRowMapper(Victim.class) // ResultSet->Java객체로 변환, RowMapper 구현체로 BeanPropertyRowMapper가 사용되어 컬럼명과 객체 필드를 자동으로 매핑해준다.
////                 커스텀 매핑, 람다로 RowMapper 구현
////                .rowMapper((rs, rowNum) -> {
////                    Victim victim = new Victim();
////                    victim.setId(rs.getLong("id"));
////                    victim.setName(rs.getString("name"));
////                    victim.setProcessId(rs.getString("process_id"));
////                    victim.setTerminatedAt(rs.getTimestamp("terminated_at").toLocalDateTime());
////                    victim.setStatus(rs.getString("status"));
////                    return victim;
////                })
//                .rowMapper(new DataClassRowMapper<>(Victim.class)) // record 사용시(불변객체)
////                .dataRowMapper(Victim.class) // Spring Batch 5.2 부터 사용가능
//                .build();
//    }

    /**
     * Paging 방식 추가 (keySet)
     */
    @Bean
    public JdbcPagingItemReader<Victim> terminatedVictimReader() {
        return new JdbcPagingItemReaderBuilder<Victim>()
                .name("terminatedVictimReader")
                .dataSource(dataSource)
                .pageSize(5)
                .selectClause("SELECT id, name, process_id, terminated_at, status")
                .fromClause("FROM victims")
                .whereClause("WHERE status = :status AND terminated_at <= :terminatedAt")
                .sortKeys(Map.of("id", Order.ASCENDING))
                .parameterValues(Map.of(
                        "status", "TERMINATED",
                        "terminatedAt", LocalDateTime.now()
                ))
                .beanRowMapper(Victim.class)
                .build();
    }


    @Bean
    public ItemWriter<Victim> victimWriter() {
        return items -> {
            for(Victim victim: items) {
                log.info("{}", victim);
            }
        };
    }

    @NoArgsConstructor
    @Data
    public static class Victim {
        private Long id;
        private String name;
        private String processId;
        private LocalDateTime terminatedAt;
        private String status;
    }

//    public record Victim(Long id, String name, String processId,
//                         LocalDateTime terminatedAt, String status) {}
}
