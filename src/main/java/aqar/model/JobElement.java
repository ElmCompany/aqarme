package aqar.model;

import com.sromku.polygon.Point;
import com.sromku.polygon.Polygon;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Element;

import java.util.stream.Stream;

@RequiredArgsConstructor
public class JobElement {

    private final Job job;
    private final Element element;

    private Polygon cachedPolygon;

    public Long jobId() {
        return job.id;
    }

    public Integer minPrice() {
        return job.jobDetail.minPrice;
    }

    public Integer maxPrice() {
        return job.jobDetail.maxPrice;
    }

    public boolean hasImages() {
        return job.jobDetail.hasImages != null ? job.jobDetail.hasImages : false;
    }

    public boolean hasElevator() {
        return job.jobDetail.hasElevator != null ? job.jobDetail.hasElevator : false;
    }

    public Stream<Integer> numRooms() {
        return integersFromCSV(job.jobDetail.numRooms);
    }

    public Stream<Integer> floorNumber() {
        return integersFromCSV(job.jobDetail.floorNumber);
    }

    public Polygon polygon() {
        if (cachedPolygon == null) {
            synchronized (this) {
                Polygon.Builder builder = Polygon.Builder();
                getFromCSV(job.jobDetail.vertexes)
                        .map(this::newPoint)
                        .forEach(builder::addVertex);
                cachedPolygon = builder.build();
            }
        }
        return cachedPolygon;
    }

    public Element element() {
        return element;
    }

    public Job job() {
        return job;
    }

    // ----- private helpers

    private Point newPoint(String coordinates) {
        String[] v = coordinates.split(";");
        return new Point(Float.parseFloat(v[0]), Float.parseFloat(v[1]));
    }

    private Stream<String> getFromCSV(String csv) {
        return csv == null ?
                Stream.empty() :
                Stream.of(csv.split(","))
                        .map(String::trim);
    }

    private Stream<Integer> integersFromCSV(String csv) {
        return getFromCSV(csv).map(Integer::parseInt);
    }
}
