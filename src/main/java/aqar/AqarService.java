package aqar;

import aqar.search.AqarSearch;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.*;

@Slf4j
@Service
public class AqarService {

    @Value("${aqar.base.url}")
    private String baseUrl;
    @Value("${aqar.max.price}")
    private int maxPrice;

    private List<AqarSearch> aqarSearchList;

    public AqarService(List<AqarSearch> aqarSearch) {
        this.aqarSearchList = aqarSearch;
    }

    public List<String> run() {
        return aqarSearchList
                .stream()
                .flatMap(this::_run)
                .collect(toList());
    }

     public Stream<String> _run(AqarSearch aqarSearch) {
        return rangeClosed(1, 1).boxed()
                .peek(it -> sleep())
                .flatMap(it -> _forPage(aqarSearch, it))
                .map(it -> it.select("a").attr("href"));
    }

    private Stream<Element> _forPage(AqarSearch aqarSearch, int pageNumber) {
        try {
            System.out.println(aqarSearch.getSearchUrl());
            Document doc = Jsoup
                    .connect(aqarSearch.getSearchUrl() + pageNumber)
                    .get();

            Elements list = doc.select(".list-single-adcol");

            return list.stream()
                    .parallel()
                    .peek(it -> sleep())
                    .filter(this::matchesPrice)
                    .filter(this::hasImage)
                    .filter(it -> matchesLatitude(aqarSearch, it));

        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
        return Stream.empty();
    }

    private boolean matchesPrice(Element element) {
        try {
            return Integer.parseInt(element.select(".price").text().replaceAll("[^\\d.]", "")) <= maxPrice;
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean hasImage(Element element) {
        return !element.select(".adcol-imgs").isEmpty();
    }

    private boolean matchesLatitude(AqarSearch aqarSearch, Element element) {
        String href = element.select("a").attr("href");
        try {
            Document page = Jsoup.connect(baseUrl + href).get();
            return page.select("tr td a")
                    .stream()
                    .filter(it -> it.attr("href").contains("maps.google.com"))
                    .findFirst()
                    .map(it -> it.attr("href"))
                    .map(aqarSearch.getCoordinatesFromUrl())
                    .map(aqarSearch.getCoordinatesMatcher())
                    .orElse(false);

        } catch (IOException e) {
            return false;
        }
    }

    private void sleep() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
