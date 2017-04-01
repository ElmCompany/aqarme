package aqar.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class JobOutput {
    private String url;
    private String title;
    private String clientId;
}
