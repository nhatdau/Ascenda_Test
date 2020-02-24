package com.nhat.service;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhat.task.ScheduledTask;
import java.util.List;
import org.awaitility.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

@SpringBootTest
public class ScheduledTaskTest {
    @SpyBean
    ScheduledTask tasks;
    @SpyBean
    StoringService storingService;

    @Test
    public void fetchData() {
        await().atMost(Duration.ONE_MINUTE).untilAsserted(() -> {
            verify(tasks, atLeast(1)).fetchDataFromSuppliers();
            verify(storingService, atLeast(1)).storeHotelsData(eq("Id"), any(List.class));
            verify(storingService, atLeast(1)).storeHotelsData(eq("id"), any(List.class));
            verify(storingService, atLeast(1)).storeHotelsData(eq("hotel_id"), any(List.class));
        });
    }
}
