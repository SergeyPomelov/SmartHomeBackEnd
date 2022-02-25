package org.lessnik.smarthome.service;

import org.lessnik.smarthome.model.IAsyncTask;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author Sergey Pomelov
 * @since 28.03.2019
 */
@Service
public class AsyncService {

    @Async
    public void wake(IAsyncTask task) {
        task.wake();
    }
}
