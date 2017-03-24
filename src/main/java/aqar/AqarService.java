package aqar;

import aqar.db.ProcessedAds;
import aqar.db.ProcessedAdsRepository;
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
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.IntStream.rangeClosed;

@Slf4j
@Service
public class AqarService {

    @Value("${aqar.base.url}")
    private String baseUrl;
    @Value("${aqar.max.price}")
    private int maxPrice;
    @Value("${aqar.num.pages}")
    private int toalPageNum;
    @Value("${aqar.sleepMillis}")
    private long sleepMillis;

    private List<AqarSearch> aqarSearchList;
    private ProcessedAdsRepository adsRepository;

    public AqarService(List<AqarSearch> aqarSearchList, ProcessedAdsRepository adsRepository) {
        this.aqarSearchList = aqarSearchList;
        this.adsRepository = adsRepository;
    }

    public Stream<String> run() {
        return aqarSearchList
                .stream()
                .flatMap(this::_run);
    }

    private Stream<String> _run(AqarSearch aqarSearch) {
        return rangeClosed(1, toalPageNum).boxed()
                .peek(it -> sleep())
                .flatMap(it -> _forPage(aqarSearch, it))
                .map(this::getShortUrl);
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
                    .filter(this::notProcessed)
                    .filter(this::matchesPrice)
                    .filter(this::hasImage)
                    .map(this::elementPage)
                    .filter(Objects::nonNull)
                    .filter(it -> matchesLatitude(aqarSearch, it));

        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
        return Stream.empty();
    }

    private boolean notProcessed(Element element) {
        String addNumber = element.id().replaceAll("[^\\d.]", "");
        if (adsRepository.addNumberExists(addNumber)) {
            System.out.printf("ad with id %s is already processed\n", addNumber);
            return false;
        } else {
            System.out.printf("start process ad: %s\n", addNumber);
            ProcessedAds ads = new ProcessedAds(addNumber);
            adsRepository.save(ads);
            return true;
        }
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

    private boolean matchesLatitude(AqarSearch aqarSearch, Element elementPage) {
        return elementPage.select("tr td a")
                .stream()
                .filter(it -> it.attr("href").contains("maps.google.com"))
                .findFirst()
                .map(it -> it.attr("href"))
                .map(aqarSearch.getCoordinatesFromUrl())
                .map(aqarSearch.getCoordinatesMatcher())
                .orElse(false);
    }

    private String getShortUrl(Element elementPage) {
        return elementPage.select("tr td a")
                .stream()
                .filter(it -> it.attr("href").contains("/ad/"))
                .map(Element::text)
                .findFirst()
                .orElse(null);
    }

    private Element elementPage(Element elementOnListPage) {
        try {
            return Jsoup.connect(baseUrl + elementOnListPage.select("a").attr("href")).get();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    private void sleep() {
        try {
            Thread.sleep(sleepMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
