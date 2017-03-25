package aqar;

import aqar.db.ProcessedAds;
import aqar.db.ProcessedAdsRepository;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.stream.Stream;

import static java.util.stream.IntStream.rangeClosed;

@Slf4j
@Service
public class AqarService {

    private static final String LOC = "loc:";
    private static final String PLUS = "+";

    @Value("${aqar.base.url}")
    private String baseUrl;

    @Value("${aqar.search.url}")
    private String searchUrl;

    @Value("${aqar.max.price}")
    private int maxPrice;

    @Value("${aqar.num.pages}")
    private int toalPageNum;

    @Value("${aqar.sleepMillis}")
    private long sleepMillis;

    @Value("${aqar.words.elevator}")
    private CharSequence elevatorWord;

    @Value("${aqar.map.boundaries}")
    private String[] boundaries;

    private ProcessedAdsRepository adsRepository;

    public AqarService(ProcessedAdsRepository adsRepository) {
        this.adsRepository = adsRepository;
    }

    Stream<String> run() {
        return rangeClosed(1, toalPageNum)
//                .parallel()
                .boxed()
                .peek(it -> sleep())
                .flatMap(this::forPage)
                .peek(this::markAsSuccess)
                .map(this::shortUrlWithTitle);
    }

    private Stream<Element> forPage(int pageNumber) {
        try {
            String currentPageUrl = baseUrl + searchUrl + pageNumber;
            log.info("processing " + currentPageUrl);
            Document doc = Jsoup.connect(currentPageUrl).get();

            Elements aprtList = doc.select(".list-single-adcol");

            return aprtList.stream()
//                    .parallel()
                    .filter(this::notProcessed)
                    .peek(it -> sleep())
                    .filter(this::matchesPrice)
                    .filter(this::hasImage)
                    .map(this::detailsPage)
                    .filter(this::hasElevator)
                    .filter(this::matchesCoordinates);

        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        return Stream.empty();
    }

    private void markAsSuccess(Element element) {
        if (element.select("kbd").hasText()){
            adsRepository.markAsSuccess(element.select("kbd").text());
        }
    }

    private String shortUrlWithTitle(Element detailsPage) {
        String title = detailsPage.select(".title h3 a").text();
        return detailsPage.select("tr td a")
                .stream()
                .filter(it -> it.attr("href").contains("/ad/"))
                .map((element) -> title + " " + "https://" + element.text())
                .findFirst()
                .orElse(null);
    }

    private boolean notProcessed(Element element) {
        String addNumber = element.id().replaceAll("[^\\d.]", "");
        if (adsRepository.addNumberExists(addNumber)) {
            log.info("Ad with id {} is already processed", addNumber);
            return false;
        } else {
            log.info("start processing Ad: {}", addNumber);
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

    private boolean hasElevator(Element elementPage) {
        return elementPage.text().contains(elevatorWord);
    }

    private boolean matchesCoordinates(Element elementPage) {
        return elementPage.select("tr td a")
                .stream()
                .filter(it -> it.attr("href").contains("maps.google.com"))
                .findFirst()
                .map(it -> it.attr("href"))
                .map(this::location)
                .map(this::insideBoundaries)
                .orElse(false);
    }

    private Element detailsPage(Element listPage) {
        try {
            return Jsoup.connect(baseUrl + listPage.select("a").attr("href")).get();
        } catch (IOException e) {
            log.error(e.getMessage());
            return new Element("dummy");
        }
    }

    private Location location(String url) {
        Location loc = new Location();
        loc.latitude = Double.parseDouble(url.substring(url.indexOf(LOC) + LOC.length(), url.indexOf(PLUS)));
        loc.longitude = Double.parseDouble(url.substring(url.indexOf(PLUS) + PLUS.length()));
        return loc;
    }

    private boolean insideBoundaries(Location loc) {
        return Stream.of(boundaries)
                .map(Rectangle::new)
                .anyMatch(it -> it.contains(loc));
    }

    private void sleep() {
        try {
            Thread.sleep(sleepMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @ToString
    private static class Location {
        double latitude, longitude;
    }

    @ToString
    private static class Rectangle {

        double left, bottom, right, top;

        Rectangle(String boundaries) {
            String[] split = boundaries.split(";");
            this.left = Double.parseDouble(split[0]);
            this.bottom = Double.parseDouble(split[1]);
            this.right = Double.parseDouble(split[2]);
            this.top = Double.parseDouble(split[3]);
        }

        boolean contains(Location loc) {
            boolean ret = loc.longitude >= this.left && loc.longitude <= this.right
                    && loc.latitude >= this.bottom && loc.latitude <= this.top;
            log.debug("comparing loc {} against: {} => {}", loc, this, ret);
            return ret;
        }
    }
}
