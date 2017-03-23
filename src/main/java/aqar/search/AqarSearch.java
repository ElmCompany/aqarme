package aqar.search;

import java.util.function.Function;

public interface AqarSearch {

    String LOC = "loc:";
    String PLUS = "+";

    String getSearchUrl();

    Function<String, Double> getCoordinatesFromUrl();

    Function<Double, Boolean> getCoordinatesMatcher();
}
