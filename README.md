# aqarme

Service to query https://sa.aqar.fm/ for certain criteria and notifies me back by Facebook messenger on the list of aprtements 
that matches my creiteria.


The list of criteria now includes, price, geo location, and has Images.

The main action is here:

```java
return list.stream()
	    .parallel()
	    .peek(it -> sleep())
	    .filter(this::notProcessed) // skipped already processed items
	    .filter(this::matchesPrice) // filter by price
	    .filter(this::hasImage)     // filter by image
	    .map(this::elementPage)
	    .filter(Objects::nonNull)
	    .filter(it -> matchesCoordinates(aqarSearch, it));  // and filter by certain cooridnates

```

And then the matched result is sent to me via facebook messagener using [Send API](https://developers.facebook.com/docs/messenger-platform/send-api-reference).
