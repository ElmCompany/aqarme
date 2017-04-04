# aqarme

Service to query https://sa.aqar.fm/ for certain criteria and notifies me back by Facebook messenger on the list of aprtements 
that matches my creiteria.


The list of criteria now includes, price, geo location, and has Images.

The main action is here:

```java
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
```

And then the matched result is sent to me via facebook messagener using [Send API](https://developers.facebook.com/docs/messenger-platform/send-api-reference).

<p align="center">
<img src="https://github.com/mhewedy/aqarme/raw/master/img.PNG" width="800">
</p>


Add job using

```bash
curl -H "Content-Type: application/json" -d '{"clientId": "client-id-1", "name": "my first job", "senders": "966593642012,00201095771359", "jobDetail": {"vertexes": "24.675722;46.556282,24.669794;46.542549,24.617369;46.568298,24.60426;46.588898,24.621115;46.656532,24.63984;46.647949,24.645145;46.625633,24.677906;46.640396,24.710034;46.618767,24.735606;46.590614", "maxPrice": "25000", "hasImages": "true", "hasElevator": "true", "numRooms": "2,3", "floorNumber": "1,2"}}' http://localhost:8080/api/job
```
