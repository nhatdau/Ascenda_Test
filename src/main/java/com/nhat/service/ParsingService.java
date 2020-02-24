package com.nhat.service;

import com.nhat.model.Hotel;
import java.util.List;
import java.util.Set;

/**
 * Service for parsing Json data from suppliers
 * @author nhatdau
 */
public interface ParsingService {

    /**
     * Parse hotels data bases on destination, list of hotels
     * @param jsonData
     *      input Json data
     * @param destinationId
     *      destination Id
     * @param hotelIds
     *      list of hotel Ids
     * @param hotels
     *      list of hotels data
     */
    void parseHotelData(String jsonData, Long destinationId, List<String> hotelIds, Set<Hotel> hotels);
}
