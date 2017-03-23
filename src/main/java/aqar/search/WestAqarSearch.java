package aqar.search;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.function.Function;

//@Service
public class WestAqarSearch implements AqarSearch {

    @Value("${aqar.base.url}")
    private String baseUrl;

    @Value("${aqar.search.west.url}")
    private String searchUrl;

    @Value("${aqar.max.west.longitude}")
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
