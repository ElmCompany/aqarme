package aqar;

import aqar.db.ProcessedAds;
import aqar.db.ProcessedAdsRepository;
import com.sromku.polygon.Point;
import com.sromku.polygon.Polygon;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.HttpStatusException;
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

    @Value("${aqar.map.polygon.vertexes}")
    private String[] vertexes;

    private Polygon polygonOnMap;

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

        } catch (HttpStatusException ex) {
            log.error(ex.getStatusCode() + ", " + ex.getUrl() + ", " + ex.getMessage());
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        return Stream.empty();
    }

    private void markAsSuccess(Element pageElement) {
        if (pageElement.select("kbd").hasText()) {
            adsRepository.markAsSuccess(pageElement.select("kbd").text());
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
        return elementPage.select(".single-adcolum").text().contains(elevatorWord);
    }

    private boolean matchesCoordinates(Element elementPage) {
        return elementPage.select("tr td a")
                .stream()
                .filter(it -> it.attr("href").contains("maps.google.com"))
                .findFirst()
                .map(it -> it.attr("href"))
                .map(this::toPoint)
                .map(this::insidePolygon)
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

    private Point toPoint(String url) {
        float latitude = Float.parseFloat(url.substring(url.indexOf(LOC) + LOC.length(), url.indexOf(PLUS)));
        float longitude = Float.parseFloat(url.substring(url.indexOf(PLUS) + PLUS.length()));
        return new Point(latitude, longitude);
    }

    private boolean insidePolygon(Point point) {
        createPolygonIfRequired();
        return polygonOnMap.contains(point);
    }

    private void createPolygonIfRequired() {
        if (polygonOnMap == null) { // this doesn't achieve double check idom, but it is okay in my case instead of lock the entire method
            synchronized (this){
                Polygon.Builder builder = Polygon.Builder();
                Stream.of(vertexes)
                        .map(this::newPoint)
                        .forEach(builder::addVertex);
                this.polygonOnMap = builder.build();
            }
        }
    }

    private Point newPoint(String coordinates) {
        String[] v = coordinates.split(";");
        return new Point(Float.parseFloat(v[0]), Float.parseFloat(v[1]));
    }

    private void sleep() {
        try {
            Thread.sleep(sleepMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
