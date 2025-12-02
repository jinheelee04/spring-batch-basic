package com.system.batch.config.parameters;

import lombok.Data;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@StepScope
@Component
public class SystemInfiltrationParameters {
    @Value("#{jobParameters[missionName]}") // 1. 필드 직접 주입
    private String missionName;
    private int securityLevel;
    private final String operationCommander;

    // 2. 생성자 파라미터 주입
    public SystemInfiltrationParameters(@Value("#{jobParameters[operationCommander]}") String operationCommander){
        this.operationCommander = operationCommander;
    }

    // 3. 세터 메서드 주입
    @Value("#{jobParameters[securityLevel]}")
    public void setSecurityLevel(int securityLevel){
        this.securityLevel = securityLevel;
    }
}
