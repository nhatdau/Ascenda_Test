package com.nhat.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

/**
 * Service for storing suppliers's data to MongoDb
 * @author nhatdau
 */
public interface StoringService {

    /**
     * Store hotels data to database
     * @param idColumnName
     *      name of Id column of data table
     * @param jsonData
     *      json data
     */
    void storeHotelsData(String idColumnName, List<JsonNode> jsonData);
}
