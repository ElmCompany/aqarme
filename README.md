# aqarme

Service to query https://sa.aqar.fm/ for certain criteria and notifies me back by Facebook messenger on the list of aprtements 
that matches my creiteria.


The list of criteria now includes, price, geo location, and has Images.

The main action is here:

```java
aprtList.stream()
    .filter(this::notProcessed)         // skipped already processed items
    .peek(it -> sleep())                // sleep 5 seconds so not be blocked
    .filter(this::matchesPrice)         // filter by price
    .filter(this::hasImage)             // filter by having image
    .map(this::detailsPage)             // get the details page of the ad
    .filter(this::hasElevator)          // check to has elavator
    .filter(this::matchesCoordinates);  // and finally check the lat&long to be inside the polygon of the dresired places
```

And then the matched result is sent to me via facebook messagener using [Send API](https://developers.facebook.com/docs/messenger-platform/send-api-reference).
