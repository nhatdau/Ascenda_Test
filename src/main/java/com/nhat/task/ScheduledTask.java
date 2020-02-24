package com.nhat.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhat.service.StoringService;
import com.nhat.utils.Constants;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

/**
 * Schedule task class to schedule fetch data from suppliers task with interval is 1 minute
 *
 * @author nhatdau
 */
@Component
@Profile("!test")
public class ScheduledTask {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTask.class);
    private ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(3);
    private RestTemplate restTemplate;
    private StoringService storingService;
    @Value("${supplier.api.url}")
    private String supplierApiUrls;
    @Value("#{${hotelId.table.map}}")
    private Map<String, String> hotelIdTableMap;

    public ScheduledTask(RestTemplateBuilder restTemplateBuilder, StoringService storingService) {
        this.restTemplate = restTemplateBuilder.build();
        this.storingService = storingService;
    }

    @Scheduled(fixedDelay = 180000)
    public void fetchDataFromSuppliers() {
        if (!StringUtils.isEmpty(supplierApiUrls)) {
            String[] apiUrls = supplierApiUrls.split(Constants.SPLITTOR);
            Arrays.asList(apiUrls).forEach(url ->
                    threadPoolExecutor.submit(() -> {
                        try {
                            fetchData(url, hotelIdTableMap.keySet());
                        } catch (JsonProcessingException e) {
                            logger.error("Have error in processing supplier url: " + url, e);
                        }
                    })
            );
//            threadPoolExecutor.shutdown();
        }
    }

    private void fetchData(String url, Set<String> hotelIds) throws JsonProcessingException {
        logger.info("Start processing and storing data from supplier url: " + url);
        String response = restTemplate.getForObject(url, String.class);
        ObjectMapper objectMapper = new ObjectMapper();
        List<JsonNode> jsonNodes = objectMapper.readValue(response, new TypeReference<List<JsonNode>>() {
        });
        Optional<String> id = hotelIds.stream().filter(jsonNodes.get(0)::has)
                .findFirst();
        if (id.isPresent()) {
            storingService.storeHotelsData(id.get(), jsonNodes);
        }
        logger.info("End processing and storing data from supplier url: " + url);
    }
}

