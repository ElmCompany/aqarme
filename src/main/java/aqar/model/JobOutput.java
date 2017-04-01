package aqar.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.stream.Stream;

@Getter
@ToString
@AllArgsConstructor
public class JobOutput {
    private String url;
    private String title;
    private String clientId;
    private Stream<String> senders;
}
