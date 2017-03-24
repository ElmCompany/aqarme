package aqar.search;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@ConditionalOnProperty(name = "aqar.search.north", havingValue = "active")
@Service
public class NorthAqarSearch implements AqarSearch {

    @Value("${aqar.base.url}")
    private String baseUrl;

    @Value("${aqar.search.north.url}")
    private String searchUrl;

    @Value("${aqar.max.north.latitude}")
    private double maxLatitude;

    @Override
    public String getSearchUrl() {
        return baseUrl + searchUrl;
    }

    @Override
    public Function<String, Double> getCoordinatesFromUrl() {
        return url -> Double.parseDouble(url.substring(url.indexOf(PLUS) + PLUS.length()));
    }

    @Override
    public Function<Double, Boolean> getCoordinatesMatcher() {
        return latitude -> latitude >= maxLatitude;
    }
}
