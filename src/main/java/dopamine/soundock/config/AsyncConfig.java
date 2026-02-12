package dopamine.soundock.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 비동기 작업 시 일꾼(스레드)를 서버가 감당할 수 있는 만큼만 모아둘 수 있도록 대기소 역할
     */
    @Bean(name = "threadPoolTaskExecutor")
    public Executor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(5); // 기본적으로 유지할 일꾼 수
        executor.setMaxPoolSize(10); // 최대 뽑을 수 있는 일꾼 수
        executor.setQueueCapacity(50); // 일꾼이 꽉 찼을 때 대기할 수 있는 작업 공간
        executor.setThreadNamePrefix("AsyncThread-"); // 로그 확인 용
        executor.initialize();

        return executor;
    }
}
