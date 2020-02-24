package com.nhat.service.impl;

import com.nhat.model.Hotel;
import com.nhat.service.ParsingService;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * Implementation class for {@link ParsingService}
 * @author nhatdau
 */
@Service
public class ParsingServiceImpl implements ParsingService {

    @Override
    public void parseHotelData(String jsonData, Long destinationId, List<String> hotelIds,
            Set<Hotel> hotels) {

    }
}
