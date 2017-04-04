package aqar;

import static java.lang.Integer.*;
import static java.util.Collections.*;
import static java.util.stream.Collectors.*;
import static java.util.stream.IntStream.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sromku.polygon.Point;

import aqar.model.Advertise;
import aqar.model.JobElement;
import aqar.model.repo.AdvertiseRepository;
import aqar.model.repo.JobRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
class AqarService {

    private static final String LOC = "loc:";
    private static final String PLUS = "+";

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
    @Value("${aqar.words.2_rooms}")
    private String twoRooms;

    private AdvertiseRepository adsRepository;
    private JobRepository jobRepository;

    public AqarService(AdvertiseRepository adsRepository, JobRepository jobRepository) {
        this.adsRepository = adsRepository;
        this.jobRepository = jobRepository;
    }

    Map<String, List<String>> run() {
        long jobsCount = jobRepository.count();
        log.info("jobs count is : {} ", jobsCount);

        if (jobsCount > 0) {
            return rangeClosed(1, totalPageNum)
                    .boxed()
                    .flatMap(this::getMatchedJobElements)
                    .peek(this::markAsSuccess)
                    .collect(groupingBy(JobElement::clientId,
                            mapping(this::adNumber, toList())));
        } else {
            return emptyMap();
        }
    }
    
    // ------------------------------------------------------------------------------------

    private Stream<JobElement> getMatchedJobElements(int pageNumber) {
        Elements adsList = getfromUrl(baseUrl + searchUrl + pageNumber).select(".list-single-adcol");
        
        return adsList.stream()
                .filter(this::notProcessed)
                .map(this::detailsPage)
                .flatMap(this::multiplexByJobs)
                .parallel()
                .filter(this::notProcessed)
                .filter(this::matchesPrice)
                .filter(this::hasImage)
                .filter(this::insideSelectedArea)
                .filter(this::hasElevator)
                .filter(this::matchesRooms)
                .filter(this::matchesFloor);
    }

    private void markAsSuccess(JobElement je) {
        if (je.element().select("kbd").hasText()) {
            adsRepository.markAsSuccess(je.element().select("kbd").text(), je.jobId());
        }
    }
    
    // ------------------------------------------------------------------------------------    
    
    private boolean notProcessed(Element element) {
        String number = extractNumber(element.id());
        if (adsRepository.countOfJobsProcessedAdvertise(number) == jobRepository.count()) {
            log.info("Ad with number: {} is already processed by all jobs", number);
            return false;
        }
        return true;
    }
    
    private Element detailsPage(Element element) {
        String urlPart = element.select("a").attr("href");
        return getfromUrl(baseUrl + urlPart);
    }

    private Stream<JobElement> multiplexByJobs(Element e) {
        return jobRepository.findAll().stream().map(it -> new JobElement(it, e));
    }

    private boolean notProcessed(JobElement je) {
        String number = adNumber(je);
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
            int price = parseInt(extractNumber(je.element().select(".label ,radius").text())) * 1000;
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
        return !je.hasImages() || !je.element().select("#adcol-images").isEmpty();
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
    
    private boolean hasElevator(JobElement je) {
        return !je.hasElevator() || je.element().select(".single-adcolum").text().contains(elevatorWord);
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
            String floorNum = extractNumber(floor);
            return floorNum.trim().length() <= 0 || it.equals(Integer.parseInt(floorNum));
        });
    }
    // ------------------------------------------------------------------------------------------------
    
    private Point toPoint(String url) {
        float latitude = Float.parseFloat(url.substring(url.indexOf(LOC) + LOC.length(), url.indexOf(PLUS)));
        float longitude = Float.parseFloat(url.substring(url.indexOf(PLUS) + PLUS.length()));
        return new Point(latitude, longitude);
    }

	/**
	 * sleeps for {@link #sleepMillis} before get the document
	 */
	private Document getfromUrl(String url) {
		try {
			Thread.sleep(sleepMillis);
			log.info("connect to: {}", url);
			return Jsoup.connect(url).get();
		} catch (Exception ex) {
			if (ex.getCause() instanceof HttpStatusException) {
				HttpStatusException hse = ((HttpStatusException) ex.getCause());
				log.error(hse.getStatusCode() + ", " + hse.getUrl() + ", " + hse.getMessage());
			} else {
				log.error(ex.getMessage());
			}
			return new Document(url);
		}
	}

    private String extractNumber(String text) {
        return text.replaceAll("[^\\d.]", "");
    }
    
    private String adNumber(JobElement je) {
        return je.element().select("kbd").text();
    }
}
