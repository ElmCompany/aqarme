# aqarme

Service to query https://sa.aqar.fm/ for certain criteria and notifies me back by Facebook messenger on the list of aprtements 
that matches my creiteria.


The list of criteria now includes, price, geo location, and has Images.

The main action is here:

```java
aprtList.stream()
	.filter(this::notProcessed)         // skipped already processed items
	.peek(it -> sleep())                // sleep 5 seconds so not to be blocked by aqar.fm
	.filter(this::matchesPrice)         // filter by price
	.filter(this::hasImage)             // filter by having image
	.map(this::detailsPage)             // get the details page of the advertise
	.filter(this::insideSelectedArea)   // check the lat&long to be inside the selected area on map
	.filter(this::hasElevator)          // check to has elavator
	.filter(this::hasMoreThanOneRoom)   // check to has more than 1 room
```

And then the matched result is sent to me via facebook messagener using [Send API](https://developers.facebook.com/docs/messenger-platform/send-api-reference).

The `insideSelectedArea()` checks the coordinates according to the selected area on maps like the following one:
<p align="center">
<img src="https://github.com/mhewedy/aqarme/raw/master/src/main/resources/polygon.png" width="500">
</p>
