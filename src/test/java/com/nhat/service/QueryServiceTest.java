package com.nhat.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhat.model.Hotel;
import com.nhat.task.ScheduledTask;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
//@TestPropertySource(properties = "app.scheduling.enable=false")
public class QueryServiceTest {

    public static final String HOTELS_1 = "hotels_1";
    public static final String HOTELS_2 = "hotels_2";
    public static final String HOTELS_3 = "hotels_3";

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private QueryService queryService;

    @MockBean
    private ScheduledTask scheduledTask;

    @BeforeEach
    public void prepareEachTest() {
        mongoTemplate.getCollection(HOTELS_1).drop();
        mongoTemplate.getCollection(HOTELS_2).drop();
        mongoTemplate.getCollection(HOTELS_3).drop();
        String jsonData = "{\n"
                + "    \"Id\": \"iJhz\",\n"
                + "    \"DestinationId\": 5432,\n"
                + "    \"Name\": \"Beach Villas Singapore\",\n"
                + "    \"Latitude\": 1.264751,\n"
                + "    \"Longitude\": 103.824006,\n"
                + "    \"Address\": \" 8 Sentosa Gateway, Beach Villas \",\n"
                + "    \"City\": \"Singapore\",\n"
                + "    \"Country\": \"SG\",\n"
                + "    \"PostalCode\": \"098269\",\n"
                + "    \"Description\": \"  This 5 star hotel is located on the coastline of Singapore.\",\n"
                + "    \"Facilities\": [\"Pool\", \"BusinessCenter\", \"WiFi \", \"DryCleaning\", \" Breakfast\"]\n"
                + "}";
        mongoTemplate.insert(Document.parse(jsonData), HOTELS_1);
        jsonData = "{\n"
                + "    \"Id\": \"SjyX\",\n"
                + "    \"DestinationId\": 5432,\n"
                + "    \"Name\": \"InterContinental Singapore Robertson Quay\",\n"
                + "    \"Latitude\": null,\n"
                + "    \"Longitude\": null,\n"
                + "    \"Address\": \" 1 Nanson Road\",\n"
                + "    \"City\": \"Singapore\",\n"
                + "    \"Country\": \"SG\",\n"
                + "    \"PostalCode\": \"238909\",\n"
                + "    \"Description\": \"Enjoy sophisticated waterfront living at the new InterContinental® Singapore Robertson Quay, luxury's preferred address nestled in the heart of Robertson Quay along the Singapore River, with the CBD just five minutes drive away. Magnifying the comforts of home, each of our 225 studios and suites features a host of thoughtful amenities that combine modernity with elegance, whilst maintaining functional practicality. The hotel also features a chic, luxurious Club InterContinental Lounge.\",\n"
                + "    \"Facilities\": [\"Pool\", \"WiFi \", \"Aircon\", \"BusinessCenter\", \"BathTub\", \"Breakfast\", \"DryCleaning\", \"Bar\"]\n"
                + "}";
        mongoTemplate.insert(Document.parse(jsonData), HOTELS_1);
        jsonData = "{\n"
                + "    \"hotel_id\": \"iJhz\",\n"
                + "    \"destination_id\": 5432,\n"
                + "    \"hotel_name\": \"Beach Villas Singapore\",\n"
                + "    \"location\": {\n"
                + "        \"address\": \"8 Sentosa Gateway, Beach Villas, 098269\",\n"
                + "        \"country\": \"Singapore\"\n"
                + "    },\n"
                + "    \"details\": \"Surrounded by tropical gardens, these upscale villas in elegant Colonial-style buildings are part of the Resorts World Sentosa complex and a 2-minute walk from the Waterfront train station. Featuring sundecks and pool, garden or sea views, the plush 1- to 3-bedroom villas offer free Wi-Fi and flat-screens, as well as free-standing baths, minibars, and tea and coffeemaking facilities. Upgraded villas add private pools, fridges and microwaves; some have wine cellars. A 4-bedroom unit offers a kitchen and a living room. There's 24-hour room and butler service. Amenities include posh restaurant, plus an outdoor pool, a hot tub, and free parking.\",\n"
                + "    \"amenities\": {\n"
                + "        \"general\": [\"outdoor pool\", \"indoor pool\", \"business center\", \"childcare\"],\n"
                + "        \"room\": [\"tv\", \"coffee machine\", \"kettle\", \"hair dryer\", \"iron\"]\n"
                + "    },\n"
                + "    \"images\": {\n"
                + "        \"rooms\": [{\n"
                + "            \"link\": \"https://d2ey9sqrvkqdfs.cloudfront.net/0qZF/2.jpg\",\n"
                + "            \"caption\": \"Double room\"\n"
                + "        }, {\n"
                + "            \"link\": \"https://d2ey9sqrvkqdfs.cloudfront.net/0qZF/3.jpg\",\n"
                + "            \"caption\": \"Double room\"\n"
                + "        }],\n"
                + "        \"site\": [{\n"
                + "            \"link\": \"https://d2ey9sqrvkqdfs.cloudfront.net/0qZF/1.jpg\",\n"
                + "            \"caption\": \"Front\"\n"
                + "        }]\n"
                + "    },\n"
                + "    \"booking_conditions\": [\"All children are welcome. One child under 12 years stays free of charge when using existing beds. One child under 2 years stays free of charge in a child's cot/crib. One child under 4 years stays free of charge when using existing beds. One older child or adult is charged SGD 82.39 per person per night in an extra bed. The maximum number of children's cots/cribs in a room is 1. There is no capacity for extra beds in the room.\", \"Pets are not allowed.\", \"WiFi is available in all areas and is free of charge.\", \"Free private parking is possible on site (reservation is not needed).\", \"Guests are required to show a photo identification and credit card upon check-in. Please note that all Special Requests are subject to availability and additional charges may apply. Payment before arrival via bank transfer is required. The property will contact you after you book to provide instructions. Please note that the full amount of the reservation is due before arrival. Resorts World Sentosa will send a confirmation with detailed payment information. After full payment is taken, the property's details, including the address and where to collect keys, will be emailed to you. Bag checks will be conducted prior to entry to Adventure Cove Waterpark. === Upon check-in, guests will be provided with complimentary Sentosa Pass (monorail) to enjoy unlimited transportation between Sentosa Island and Harbour Front (VivoCity). === Prepayment for non refundable bookings will be charged by RWS Call Centre. === All guests can enjoy complimentary parking during their stay, limited to one exit from the hotel per day. === Room reservation charges will be charged upon check-in. Credit card provided upon reservation is for guarantee purpose. === For reservations made with inclusive breakfast, please note that breakfast is applicable only for number of adults paid in the room rate. Any children or additional adults are charged separately for breakfast and are to paid directly to the hotel.\"]\n"
                + "}";
        mongoTemplate.insert(Document.parse(jsonData), HOTELS_2);
        jsonData = "{\n"
                + "    \"hotel_id\": \"SjyX\",\n"
                + "    \"destination_id\": 5432,\n"
                + "    \"hotel_name\": \"InterContinental\",\n"
                + "    \"location\": {\n"
                + "        \"address\": \"1 Nanson Rd, Singapore 238909\",\n"
                + "        \"country\": \"Singapore\"\n"
                + "    },\n"
                + "    \"details\": \"InterContinental Singapore Robertson Quay is luxury's preferred address offering stylishly cosmopolitan riverside living for discerning travelers to Singapore. Prominently situated along the Singapore River, the 225-room inspiring luxury hotel is easily accessible to the Marina Bay Financial District, Central Business District, Orchard Road and Singapore Changi International Airport, all located a short drive away. The hotel features the latest in Club InterContinental design and service experience, and five dining options including Publico, an Italian landmark dining and entertainment destination by the waterfront.\",\n"
                + "    \"amenities\": {\n"
                + "        \"general\": [\"outdoor pool\", \"business center\", \"childcare\", \"parking\", \"bar\", \"dry cleaning\", \"wifi\", \"breakfast\", \"concierge\"],\n"
                + "        \"room\": [\"aircon\", \"minibar\", \"tv\", \"bathtub\", \"hair dryer\"]\n"
                + "    },\n"
                + "    \"images\": {\n"
                + "        \"rooms\": [{\n"
                + "            \"link\": \"https://d2ey9sqrvkqdfs.cloudfront.net/Sjym/i93_m.jpg\",\n"
                + "            \"caption\": \"Double room\"\n"
                + "        }, {\n"
                + "            \"link\": \"https://d2ey9sqrvkqdfs.cloudfront.net/Sjym/i94_m.jpg\",\n"
                + "            \"caption\": \"Bathroom\"\n"
                + "        }],\n"
                + "        \"site\": [{\n"
                + "            \"link\": \"https://d2ey9sqrvkqdfs.cloudfront.net/Sjym/i1_m.jpg\",\n"
                + "            \"caption\": \"Restaurant\"\n"
                + "        }, {\n"
                + "            \"link\": \"https://d2ey9sqrvkqdfs.cloudfront.net/Sjym/i2_m.jpg\",\n"
                + "            \"caption\": \"Hotel Exterior\"\n"
                + "        }, {\n"
                + "            \"link\": \"https://d2ey9sqrvkqdfs.cloudfront.net/Sjym/i5_m.jpg\",\n"
                + "            \"caption\": \"Entrance\"\n"
                + "        }, {\n"
                + "            \"link\": \"https://d2ey9sqrvkqdfs.cloudfront.net/Sjym/i24_m.jpg\",\n"
                + "            \"caption\": \"Bar\"\n"
                + "        }]\n"
                + "    },\n"
                + "    \"booking_conditions\": []\n"
                + "}";
        mongoTemplate.insert(Document.parse(jsonData), HOTELS_2);
        jsonData = "{\n"
                + "    \"id\": \"iJhz\",\n"
                + "    \"destination\": 5432,\n"
                + "    \"name\": \"Beach Villas Singapore\",\n"
                + "    \"lat\": 1.264751,\n"
                + "    \"lng\": 103.824006,\n"
                + "    \"address\": \"8 Sentosa Gateway, Beach Villas, 098269\",\n"
                + "    \"info\": \"Located at the western tip of Resorts World Sentosa, guests at the Beach Villas are guaranteed privacy while they enjoy spectacular views of glittering waters. Guests will find themselves in paradise with this series of exquisite tropical sanctuaries, making it the perfect setting for an idyllic retreat. Within each villa, guests will discover living areas and bedrooms that open out to mini gardens, private timber sundecks and verandahs elegantly framing either lush greenery or an expanse of sea. Guests are assured of a superior slumber with goose feather pillows and luxe mattresses paired with 400 thread count Egyptian cotton bed linen, tastefully paired with a full complement of luxurious in-room amenities and bathrooms boasting rain showers and free-standing tubs coupled with an exclusive array of ESPA amenities and toiletries. Guests also get to enjoy complimentary day access to the facilities at Asia’s flagship spa – the world-renowned ESPA.\",\n"
                + "    \"amenities\": [\"Aircon\", \"Tv\", \"Coffee machine\", \"Kettle\", \"Hair dryer\", \"Iron\", \"Tub\"],\n"
                + "    \"images\": {\n"
                + "        \"rooms\": [{\n"
                + "            \"url\": \"https://d2ey9sqrvkqdfs.cloudfront.net/0qZF/2.jpg\",\n"
                + "            \"description\": \"Double room\"\n"
                + "        }, {\n"
                + "            \"url\": \"https://d2ey9sqrvkqdfs.cloudfront.net/0qZF/4.jpg\",\n"
                + "            \"description\": \"Bathroom\"\n"
                + "        }],\n"
                + "        \"amenities\": [{\n"
                + "            \"url\": \"https://d2ey9sqrvkqdfs.cloudfront.net/0qZF/0.jpg\",\n"
                + "            \"description\": \"RWS\"\n"
                + "        }, {\n"
                + "            \"url\": \"https://d2ey9sqrvkqdfs.cloudfront.net/0qZF/6.jpg\",\n"
                + "            \"description\": \"Sentosa Gateway\"\n"
                + "        }]\n"
                + "    }\n"
                + "}";
        mongoTemplate.insert(Document.parse(jsonData), HOTELS_3);
        jsonData = "{\n"
                + "    \"id\": \"f8c9\",\n"
                + "    \"destination\": 1122,\n"
                + "    \"name\": \"Hilton Tokyo Shinjuku\",\n"
                + "    \"lat\": 35.6926,\n"
                + "    \"lng\": 139.690965,\n"
                + "    \"address\": null,\n"
                + "    \"info\": null,\n"
                + "    \"amenities\": null,\n"
                + "    \"images\": {\n"
                + "        \"rooms\": [{\n"
                + "            \"url\": \"https://d2ey9sqrvkqdfs.cloudfront.net/YwAr/i10_m.jpg\",\n"
                + "            \"description\": \"Suite\"\n"
                + "        }, {\n"
                + "            \"url\": \"https://d2ey9sqrvkqdfs.cloudfront.net/YwAr/i11_m.jpg\",\n"
                + "            \"description\": \"Suite - Living room\"\n"
                + "        }],\n"
                + "        \"amenities\": [{\n"
                + "            \"url\": \"https://d2ey9sqrvkqdfs.cloudfront.net/YwAr/i57_m.jpg\",\n"
                + "            \"description\": \"Bar\"\n"
                + "        }]\n"
                + "    }\n"
                + "}";
        mongoTemplate.insert(Document.parse(jsonData), HOTELS_3);
    }

    @DisplayName("Test query hotel data bases on list of hotel Ids")
    @Test
    public void testQueryHotelUsingHotelIds() {

        List<Hotel> hotels = queryService.searchHotels(Arrays.asList("iJhz", "SjyX"), null);
        assertThat(hotels.size()).isEqualTo(2);
        hotels.forEach(hotel -> {
            if ("iJhz".equals(hotel.getHotelId())) {
                assertThat(hotel.getLocation().getAddress()).isEqualTo("8 Sentosa Gateway, Beach Villas, 098269");
                assertThat(hotel.getAmenities().size()).isEqualTo(2);
            }else if ("SjyX".equals(hotel.getHotelId())) {
                assertThat(hotel.getLocation().getAddress()).isEqualTo("1 Nanson Rd, Singapore 238909");
                assertThat(hotel.getAmenities().size()).isEqualTo(2);
            }
        });
    }

    @DisplayName("Test query hotel data bases on destination Id")
    @Test
    public void testQueryHotelUsingDestinationId() {
        List<Hotel> hotels = queryService.searchHotels(Collections.emptyList(), 1122);
        assertThat(hotels.size()).isEqualTo(1);
        Hotel hotel = hotels.get(0);
        assertThat(hotel.getHotelId()).isEqualTo("f8c9");
        assertThat(hotel.getLocation().getAddress()).isNull();
        assertThat(hotel.getAmenities()).isEmpty();
    }
}
