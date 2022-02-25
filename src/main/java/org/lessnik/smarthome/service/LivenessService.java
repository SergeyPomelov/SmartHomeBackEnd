package org.lessnik.smarthome.service;

import lombok.RequiredArgsConstructor;
import org.lessnik.smarthome.model.IAsyncTask;
import org.lessnik.smarthome.model.InternetSpeedProbeTask;
import org.lessnik.smarthome.model.LivenessProbeTask;
import org.lessnik.smarthome.model.WebpageProbeTask;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Set;

/**
 * @author Sergey Pomelov
 * @since 28.03.2019
 */
@Service
@RequiredArgsConstructor
@ParametersAreNonnullByDefault
public class LivenessService {

    private final AsyncService async;

    private final Set<LivenessProbeTask> fastTasks = SecretsHolder.fastTasks;
    private final Set<LivenessProbeTask> tasks = SecretsHolder.tasks;
    private final IAsyncTask webpageProbeTask = new WebpageProbeTask(206, 5000);
    private final IAsyncTask internetSpeedProbeTask = new InternetSpeedProbeTask(67, 5000);

    @Scheduled(cron = "0 0 0/6 * * *")
    public void checkInternetSpeed() {
        async.wake(internetSpeedProbeTask);
    }

    @Scheduled(fixedDelay = 900_000)
    public void checkWebUI() {
        async.wake(webpageProbeTask);
    }

    @Scheduled(fixedDelay = 5_000)
    public void updateFast() {
        fastTasks.forEach(async::wake);
    }

    @Scheduled(fixedDelay = 15_000)
    public void update() {
        tasks.forEach(async::wake);
    }
}
