var drawingManager;
var selectedShape;
var colors = ['#1E90FF'];
var selectedColor;
var colorButtons = {};
var pathstr = "";

function clearSelection() {
    if (selectedShape) {
        if (typeof selectedShape.setEditable == 'function') {
            selectedShape.setEditable(false);
        }
        selectedShape = null;
    }
//    curseldiv.innerHTML = "<b>cursel</b>:";
}

function updateCurSelText(shape) {
    posstr = "" + selectedShape.position;
    if (typeof selectedShape.position == 'object') {
        posstr = selectedShape.position.toUrlValue();
    }
    pathstr = "" + selectedShape.getPath;
    if (typeof selectedShape.getPath == 'function') {
        pathstr = "";
        for (var i = 0; i < selectedShape.getPath().getLength(); i++) {
            // .toUrlValue(5) limits number of decimals, default is 6 but can do more
            pathstr += selectedShape.getPath().getAt(i).toUrlValue().replace(",", ";") + ",";
        }
        pathstr = pathstr.slice(0, -1)
        pathstr += "";
    }
    console.log(pathstr);
    bndstr = "" + selectedShape.getBounds;
    cntstr = "" + selectedShape.getBounds;
    if (typeof selectedShape.getBounds == 'function') {
        var tmpbounds = selectedShape.getBounds();
        cntstr = "" + tmpbounds.getCenter().toUrlValue();
        bndstr = "[NE: " + tmpbounds.getNorthEast().toUrlValue() + " SW: " + tmpbounds.getSouthWest().toUrlValue() + "]";
    }
    cntrstr = "" + selectedShape.getCenter;
    if (typeof selectedShape.getCenter == 'function') {
        cntrstr = "" + selectedShape.getCenter().toUrlValue();
    }
    radstr = "" + selectedShape.getRadius;
    if (typeof selectedShape.getRadius == 'function') {
        radstr = "" + selectedShape.getRadius();
    }
    //curseldiv.innerHTML = "<b>cursel</b>: " + selectedShape.type + " " + selectedShape + "; <i>pos</i>: " + posstr + " ; <i>path</i>: " + pathstr + " ; <i>bounds</i>: " + bndstr + " ; <i>Cb</i>: " + cntstr + " ; <i>radius</i>: " + radstr + " ; <i>Cr</i>: " + cntrstr ;

    //curseldiv.innerHTML = "<b>" + pathstr + "</b>";

    var jobInputs = document.getElementById("job-input");
    jobInputs.style.display = 'block';
}

function setSelection(shape, isNotMarker) {
    clearSelection();
    selectedShape = shape;
    if (isNotMarker)
        shape.setEditable(true);
    selectColor(shape.get('fillColor') || shape.get('strokeColor'));
    updateCurSelText(shape);
}

function deleteSelectedShape() {
    if (selectedShape) {
        selectedShape.setMap(null);
    }
}

function selectColor(color) {
    selectedColor = color;
    for (var i = 0; i < colors.length; ++i) {
        var currColor = colors[i];
//        colorButtons[currColor].style.border = currColor == color ? '2px solid #789' : '2px solid #fff';
    }

    // Retrieves the current options from the drawing manager and replaces the
    // stroke or fill color as appropriate.
    var polylineOptions = drawingManager.get('polylineOptions');
    polylineOptions.strokeColor = color;
    drawingManager.set('polylineOptions', polylineOptions);

    var rectangleOptions = drawingManager.get('rectangleOptions');
    rectangleOptions.fillColor = color;
    drawingManager.set('rectangleOptions', rectangleOptions);

    var circleOptions = drawingManager.get('circleOptions');
    circleOptions.fillColor = color;
    drawingManager.set('circleOptions', circleOptions);

    var polygonOptions = drawingManager.get('polygonOptions');
    polygonOptions.fillColor = color;
    drawingManager.set('polygonOptions', polygonOptions);
}

function setSelectedShapeColor(color) {
    if (selectedShape) {
        if (selectedShape.type == google.maps.drawing.OverlayType.POLYLINE) {
            selectedShape.set('strokeColor', color);
        } else {
            selectedShape.set('fillColor', color);
        }
    }
}

function makeColorButton(color) {
    var button = document.createElement('span');
    button.className = 'color-button';
    button.style.backgroundColor = color;
    google.maps.event.addDomListener(button, 'click', function() {
        selectColor(color);
        setSelectedShapeColor(color);
    });

    return button;
}

function buildColorPalette() {
    var colorPalette = document.getElementById('color-palette');
    for (var i = 0; i < colors.length; ++i) {
        var currColor = colors[i];
        var colorButton = makeColorButton(currColor);
        colorPalette.appendChild(colorButton);
        colorButtons[currColor] = colorButton;
    }
    selectColor(colors[0]);
}

/////////////////////////////////////
var map; //= new google.maps.Map(document.getElementById('map'), {
// these must have global refs too!:
var placeMarkers = [];
var input;
var searchBox;
var curposdiv;
var curseldiv;

function deletePlacesSearchResults() {
    for (var i = 0, marker; marker = placeMarkers[i]; i++) {
        marker.setMap(null);
    }
    placeMarkers = [];
    input.value = ''; // clear the box too
}

/////////////////////////////////////
function initialize() {
    map = new google.maps.Map(document.getElementById('map'), { //var
        zoom: 12, //10,
        center: new google.maps.LatLng(24.702228, 46.750634), //(22.344, 114.048),
        mapTypeId: google.maps.MapTypeId.ROADMAP,
        disableDefaultUI: false,
        zoomControl: true
    });
    curposdiv = document.getElementById('curpos');
    curseldiv = document.getElementById('cursel');

    var polyOptions = {
        strokeWeight: 0,
        fillOpacity: 0.45,
        editable: true
    };
    // Creates a drawing manager attached to the map that allows the user to draw
    // markers, lines, and shapes.
    drawingManager = new google.maps.drawing.DrawingManager({
        drawingMode: google.maps.drawing.OverlayType.POLYGON,
        markerOptions: {
            draggable: true,
            editable: true,
        },
        polylineOptions: {
            editable: true
        },
        rectangleOptions: polyOptions,
        circleOptions: polyOptions,
        polygonOptions: polyOptions,
         drawingControlOptions: {
            drawingModes: ['polygon']
         },
        map: map
    });

    google.maps.event.addListener(drawingManager, 'overlaycomplete', function(e) {
        //~ if (e.type != google.maps.drawing.OverlayType.MARKER) {
        var isNotMarker = (e.type != google.maps.drawing.OverlayType.MARKER);
        // Switch back to non-drawing mode after drawing a shape.
        drawingManager.setDrawingMode(null);

        // Add an event listener that selects the newly-drawn shape when the user
        // mouses down on it.
        var newShape = e.overlay;
        newShape.type = e.type;
        google.maps.event.addListener(newShape, 'click', function() {
            setSelection(newShape, isNotMarker);
        });
        google.maps.event.addListener(newShape, 'drag', function() {
            updateCurSelText(newShape);
        });
        google.maps.event.addListener(newShape, 'dragend', function() {
            updateCurSelText(newShape);
        });
        setSelection(newShape, isNotMarker);
        //~ }// end if
    });

    // Clear the current selection when the drawing mode is changed, or when the
    // map is clicked.
    google.maps.event.addListener(drawingManager, 'drawingmode_changed', clearSelection);
    google.maps.event.addListener(map, 'click', clearSelection);
    google.maps.event.addDomListener(document.getElementById('delete-button'), 'click', deleteSelectedShape);

    buildColorPalette();

    //~ initSearch();
    // Create the search box and link it to the UI element.
    input = /** @type {HTMLInputElement} */ ( //var
        document.getElementById('pac-input'));
    map.controls[google.maps.ControlPosition.TOP_RIGHT].push(input);
    //
    var DelPlcButDiv = document.createElement('div');
    //~ DelPlcButDiv.style.color = 'rgb(25,25,25)'; // no effect?
    DelPlcButDiv.style.backgroundColor = '#fff';
    DelPlcButDiv.style.cursor = 'pointer';
//    DelPlcButDiv.innerHTML = 'DEL';
    map.controls[google.maps.ControlPosition.TOP_RIGHT].push(DelPlcButDiv);
    google.maps.event.addDomListener(DelPlcButDiv, 'click', deletePlacesSearchResults);

    searchBox = new google.maps.places.SearchBox( //var
        /** @type {HTMLInputElement} */
        (input));

    // Listen for the event fired when the user selects an item from the
    // pick list. Retrieve the matching places for that item.
    google.maps.event.addListener(searchBox, 'places_changed', function() {
        var places = searchBox.getPlaces();

        if (places.length == 0) {
            return;
        }
        for (var i = 0, marker; marker = placeMarkers[i]; i++) {
            marker.setMap(null);
        }

        // For each place, get the icon, place name, and location.
        placeMarkers = [];
        var bounds = new google.maps.LatLngBounds();
        for (var i = 0, place; place = places[i]; i++) {
            var image = {
                url: place.icon,
                size: new google.maps.Size(71, 71),
                origin: new google.maps.Point(0, 0),
                anchor: new google.maps.Point(17, 34),
                scaledSize: new google.maps.Size(25, 25)
            };

            // Create a marker for each place.
            var marker = new google.maps.Marker({
                map: map,
                icon: image,
                title: place.name,
                position: place.geometry.location
            });

            placeMarkers.push(marker);

            bounds.extend(place.geometry.location);
        }

        map.fitBounds(bounds);
    });

    // Bias the SearchBox results towards places that are within the bounds of the
    // current map's viewport.
    google.maps.event.addListener(map, 'bounds_changed', function() {
        var bounds = map.getBounds();
        searchBox.setBounds(bounds);
        //curposdiv.innerHTML = "<b>curpos</b> Z: " + map.getZoom() + " C: " + map.getCenter().toUrlValue();
    }); //////////////////////
}

google.maps.event.addDomListener(window, 'load', initialize);

function addJob(){

    var job = {
        clientId: gevbi('clientId'),
        jobDetail: {
            vertexes: pathstr,
            minPrice: gevbi('minPrice'),
            maxPrice: gevbi('maxPrice'),
            hasImages: document.getElementById('hasImages').checked,
            hasElevator: document.getElementById('hasElevator').checked,
            numRooms: gevbi('numRooms'),
            floorNumber: gevbi('floorNumber')
        }
    }

    console.log(job);

    var xhr = new XMLHttpRequest();
    xhr.open('POST', 'api/jobs');
    xhr.setRequestHeader('Content-Type', 'application/json');
    xhr.onload = function() {
        if (xhr.status !== 200) {
            window.alert('error : ' + xhr.responseText)
        }else{
            window.alert('success')
        }
    };
    xhr.send(JSON.stringify(job));
}

function gevbi(eid){
    return document.getElementById(eid).value;
}
