package com.nhat.service;

import com.nhat.model.Hotel;
import java.util.List;

/**
 * QueryService for query hotel data from data of suppliers
 * @author nhatdau
 */
public interface QueryService {

    /**
     * Query hotel data bases on list of hotel Id, destination Id
     * @param hotelIds
     *  list of hotel Id
     * @param destinationId
     *  the destination Id
     * @return list of hotel data
     */
    List<Hotel> searchHotels(List<String> hotelIds, Integer destinationId);
}
