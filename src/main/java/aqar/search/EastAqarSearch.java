package aqar.search;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@ConditionalOnProperty(name = "aqar.search.east", havingValue = "active")
@Service
public class EastAqarSearch implements AqarSearch {

    @Value("${aqar.base.url}")
    private String baseUrl;

    @Value("${aqar.search.east.url}")
    private String searchUrl;

    @Value("${aqar.max.east.longitude}")
    private double maxLongitude;

    @Override
    public String getSearchUrl() {
        return baseUrl + searchUrl;
    }

    @Override
    public Function<String, Double> getCoordinatesFromUrl() {
        return url -> Double.parseDouble(url.substring(url.indexOf(LOC) + LOC.length(), url.indexOf(PLUS)));
    }

    @Override
    public Function<Double, Boolean> getCoordinatesMatcher() {
        return longitude -> longitude <= maxLongitude;
    }
}
