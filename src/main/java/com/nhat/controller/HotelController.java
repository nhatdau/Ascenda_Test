package com.nhat.controller;

import com.nhat.model.Hotel;
import com.nhat.service.QueryService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for hotel Apis
 * @author nhatdau
 */
@RestController
@RequestMapping("/hotel")
public class HotelController {

    private QueryService queryService;

    public HotelController(QueryService queryService) {
        this.queryService = queryService;
    }
    @PostMapping("/search")
    public List<Hotel> searchHotels(@RequestParam(required = false) String[] hotelIds, @RequestParam(required = false) Integer destinationId) {
        return queryService.searchHotels(hotelIds != null ? Arrays.asList(hotelIds) : Collections.emptyList(), destinationId);
    }
}
