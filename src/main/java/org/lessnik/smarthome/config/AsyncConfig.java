package org.lessnik.smarthome.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * @author Sergey Pomelov
 * @since 22.05.2019
 */
// is not eligible for getting processed by all BeanPostProcessors (for example: not eligible for auto-proxying)
@Profile("!disabled-async")
@EnableAsync
@Configuration
@RequiredArgsConstructor
@ParametersAreNonnullByDefault
public class AsyncConfig implements AsyncConfigurer {

    private static final int QUEUE_CAPACITY = 256;

    @Override
    public Executor getAsyncExecutor() {
        final ThreadPoolExecutor executor =
                new ThreadPoolExecutor(2, 4, 0, MILLISECONDS,
                        new LinkedBlockingDeque<>(QUEUE_CAPACITY),
                        new ThreadPoolExecutor.DiscardOldestPolicy());

        executor.prestartCoreThread();
        return executor;
    }
}
