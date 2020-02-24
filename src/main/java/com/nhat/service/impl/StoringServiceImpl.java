package com.nhat.service.impl;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.util.JSON;
import com.nhat.service.StoringService;
import com.nhat.task.ScheduledTask;
import com.nhat.utils.Constants;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Setter;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.BulkOperations.BulkMode;
import org.springframework.data.mongodb.core.FindAndReplaceOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class StoringServiceImpl implements StoringService {

    private static final Logger logger = LoggerFactory.getLogger(StoringServiceImpl.class);
    private MongoTemplate mongoTemplate;

    @Value("#{${hotelId.table.map}}")
    private Map<String, String> hotelIdTableMap;

    public StoringServiceImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void storeHotelsData(String idColumnName, List<JsonNode> jsonData) {
        String tableName = hotelIdTableMap.get(idColumnName);
        if (Objects.nonNull(tableName)) {
            storeToCollection(idColumnName, jsonData, tableName);
        }
    }

    private void storeToCollection(String idColumnName, List<JsonNode> jsonData,
            String collectionName) {
        BulkOperations bulkOperations = mongoTemplate.bulkOps(BulkMode.UNORDERED, collectionName);
        jsonData.forEach(jsonNode -> {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                bulkOperations
                        .replaceOne(
                                query(where(idColumnName).is(jsonNode.get(idColumnName).asText())),
                                Document.parse(objectMapper.writeValueAsString(jsonNode)),
                                FindAndReplaceOptions
                                        .options().upsert());
            } catch (JsonProcessingException e) {
                logger.error("Error in processing data: " + idColumnName + "=" + jsonNode
                        .get(idColumnName).asText(), e);
            }
        });
        bulkOperations.execute();
    }
}
