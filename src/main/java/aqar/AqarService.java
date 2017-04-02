package aqar;

import aqar.model.Advertise;
import aqar.model.Job;
import aqar.model.JobElement;
import aqar.model.JobOutput;
import aqar.model.repo.AdvertiseRepository;
import aqar.model.repo.JobRepository;
import com.sromku.polygon.Point;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;
import static java.util.stream.IntStream.rangeClosed;

@Slf4j
@Service
class AqarService {

    private static final String LOC = "loc:";
    private static final String PLUS = "+";
    private static final Map<String, Document> CACHE = Util.lruCache(100);

    @Value("${aqar.base.url}")
    private String baseUrl;
    @Value("${aqar.search.url}")
    private String searchUrl;
    @Value("${aqar.num.pages}")
    private int totalPageNum;
    @Value("${aqar.sleepMillis}")
    private long sleepMillis;
    @Value("${aqar.words.elevator}")
    private String elevatorWord;
    @Value("${aqar.words.sleep}")
    private String sleepWord;
    @Value("${aqar.words.floor}")
    private String floorWord;
    @Value("{aqar.words.2_rooms}")
    private String twoRooms;

    private AdvertiseRepository adsRepository;
    private JobRepository jobRepository;

    public AqarService(AdvertiseRepository adsRepository, JobRepository jobRepository) {
        this.adsRepository = adsRepository;
        this.jobRepository = jobRepository;
    }

    Stream<JobOutput> run() {
        long jobsCount = jobRepository.count();
        log.info("jobs count is : {} ", jobsCount);

        if (jobsCount > 0) {
            return rangeClosed(1, totalPageNum)
                    .boxed()
                    .peek(it -> sleep())
                    .flatMap(this::getMatched)
                    .peek(this::markAsSuccess)
                    .map(this::shortUrlWithTitle);
        } else {
            return Stream.empty();
        }
    }

    private Stream<JobElement> getMatched(int pageNumber) {
        try {
            String currentPageUrl = baseUrl + searchUrl + pageNumber;
            log.info("processing " + currentPageUrl);

            Elements adsList = Jsoup.connect(currentPageUrl).get().select(".list-single-adcol");
            List<Job> jobs = jobRepository.findAll();

            log.info("found {} active jobs in db", jobs.size());

            return adsList.stream()
                    .flatMap(it -> multiplex(jobs, it))
                    .filter(this::notProcessed)
                    .peek(it -> sleep())
                    .filter(this::matchesPrice)
                    .filter(this::hasImage)
                    .map(this::detailsPage)
                    .filter(this::insideSelectedArea)
                    .filter(this::hasElevator)
                    .filter(this::matchesRooms)
                    .filter(this::matchesFloor);

        } catch (HttpStatusException ex) {
            log.error(ex.getStatusCode() + ", " + ex.getUrl() + ", " + ex.getMessage());
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        return Stream.empty();
    }

    private void markAsSuccess(JobElement je) {
        if (je.element().select("kbd").hasText()) {
            adsRepository.markAsSuccess(je.element().select("kbd").text(), je.jobId());
        }
    }

    private JobOutput shortUrlWithTitle(JobElement je) {
        String title = je.element().select(".title h3 a").text();
        return je.element().select("tr td a")
                .stream()
                .filter(it -> it.attr("href").contains("/ad/"))
                .map(it -> new JobOutput("https://" + it.text(), title, je.clientId(), je.senders()))
                .findFirst()
                .orElse(null);
    }

    private Stream<JobElement> multiplex(List<Job> jobs, Element e) {
        return jobs.stream().map(it -> new JobElement(it, e));
    }

    private boolean notProcessed(JobElement je) {
        String number = extractNumber(je.element().id());

        if (adsRepository.alreadyProcessed(number, je.jobId())) {
            log.info("Ad with number: {} and job id: {} is already processed", number, je.jobId());
            return false;
        } else {
            log.info("start processing Ad: {} for job: {}", number, je.jobId());
            Advertise ads = new Advertise(number, je.job());
            adsRepository.save(ads);
            return true;
        }
    }

    private boolean matchesPrice(JobElement je) {
        try {
            int price = parseInt(extractNumber(je.element().select(".price").text()));
            boolean ret = true;
            if (je.maxPrice() != null) {
                ret = price <= je.maxPrice();
            }
            if (ret && je.minPrice() != null) {
                ret = price >= je.minPrice();
            }
            return ret;
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean hasImage(JobElement je) {
        return !je.hasImages() || !je.element().select(".adcol-imgs").isEmpty();
    }

    private boolean matchesRooms(JobElement je) {
        return !je.hasRooms() || je.numRooms().anyMatch(it -> {
            String rooms = je.element().select(".small-12 table")
                    .last().getElementsContainingOwnText(sleepWord).text();
            rooms = rooms.replace(twoRooms, "2");
            return it.equals(Integer.parseInt(extractNumber(rooms)));
        });
    }

    private boolean matchesFloor(JobElement je) {
        return !je.hasFloor() || je.floorNumber().anyMatch(it -> {
            String floor = je.element().select(".small-12 table")
                    .last().getElementsContainingOwnText(floorWord).text();
            return it.equals(Integer.parseInt(extractNumber(floor)));
        });
    }

    private boolean hasElevator(JobElement je) {
        return !je.hasElevator() || je.element().select(".single-adcolum").text().contains(elevatorWord);
    }

    private boolean insideSelectedArea(JobElement je) {
        return je.element().select("tr td a")
                .stream()
                .filter(it -> it.attr("href").contains("maps.google.com"))
                .findFirst()
                .map(it -> it.attr("href"))
                .map(this::toPoint)
                .map(point -> je.polygon().contains(point))
                .orElse(false);
    }

    private JobElement detailsPage(JobElement je) {
        try {
            String urlPart = je.element().select("a").attr("href");
            Document document = CACHE.computeIfAbsent(urlPart, it -> fromUrl(baseUrl + it));
            return new JobElement(je.job(), document);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new JobElement(je.job(), new Element("NULL"));
        }
    }

    private Point toPoint(String url) {
        float latitude = Float.parseFloat(url.substring(url.indexOf(LOC) + LOC.length(), url.indexOf(PLUS)));
        float longitude = Float.parseFloat(url.substring(url.indexOf(PLUS) + PLUS.length()));
        return new Point(latitude, longitude);
    }

    private void sleep() {
        try {
            Thread.sleep(sleepMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String extractNumber(String text) {
        return text.replaceAll("[^\\d.]", "");
    }

    private Document fromUrl(String url) {
        try {
            return Jsoup.connect(url).get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
