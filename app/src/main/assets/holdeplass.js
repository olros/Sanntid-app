const now = new Date();

const resultText = document.getElementById("resultText");
const btnBack = document.getElementById('back-button');
var favBtn = document.getElementById('favBtn');
var favImg = document.getElementById('favImg');
var departureDetails = document.getElementById('departureDetails');
var departureDetailsContent = document.getElementById('departureDetailsContent');
var departureDetailsAll = document.getElementById('departureDetailsAll');
var departureDetailsAllContent = document.getElementById('departureDetailsAllContent');
var departureDetailsExtend = document.getElementById('departureDetailsExtend');
var closeDetailsButton = document.getElementById('close-details-button');
var closeDetailsAllButton = document.getElementById('close-details-all-button');
var refreshDetailsButton = document.getElementById('refresh-details-button');
var snackbar = document.getElementById('snackbar');
var snackbarContainer = document.getElementById('snackbarContainer');
var moreDepartures = document.getElementById('moreDepartures');
var noMoreDepartures = document.getElementById('noMoreDepartures');
var setCenter = false;
var idIndex;
var latlngHoldeplass;

var numberOfDepartures = 20;
let closestStops = [];
var favoritter = [];
let stopData = [];
const entur_graphql_endpoint = "https://api.entur.io/journey-planner/v2/graphql";
var updateInterval = 6000;

function getParameterByName(name, url) {
    if (!url) url = window.location.href;
    name = name.replace(/[\[\]]/g, "\\$&");
    var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
    		results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}
var h = getParameterByName('h');
if (getParameterByName('t')) {
  var t = getParameterByName('t');
  if (isNaN(t)) {
    //
  } else {
    if (1999 < t && t < 20000) {
      updateInterval = t;
    }
  }
}

// When page is loaded
window.addEventListener('load', function() {
    changeHTML();
    setInterval(function(){ getNextDepartureForStop(h, numberOfDepartures); }, updateInterval);
});

// Changes the HTML when all data is ready
function changeHTML() {
    let stopsTable = "<table id='avganger'><tr><th>Linje</th> <th>Destinasjon</th> <th>Avgang</th></tr> </table></div>";
    resultText.innerHTML = stopsTable;
    getNextDepartureForStop(h, numberOfDepartures);
}

// Fetches next departure from GraphQL entur API
function getNextDepartureForStop(stopID, numberOfDepartures) {
    // graphQL request for next departures
    fetch(entur_graphql_endpoint, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'ET-Client-Name': 'https://sanntid.ga'
        },
        body: JSON.stringify({ query: `
        {
            stopPlace(id: "NSR:StopPlace:` + stopID + `") {
                id
                name
                latitude
                longitude
                quays {
                  publicCode
                }
                tariffZones {
                  id
                }
                estimatedCalls(timeRange: 86400, numberOfDepartures: ` + numberOfDepartures + `, omitNonBoarding:true) {
                    realtime
                    aimedDepartureTime
                    expectedDepartureTime
                    destinationDisplay {
                        frontText
                    }
                    quay {
                      id
                      publicCode
                      description
                    }
                    serviceJourney {
                        id
                        operator {
                          id
                        }
                        journeyPattern {
                            line {
                                publicCode
                                id
                                transportMode
                            }
                        }
                    }
                }
            }
        }
        `}),
    })
    .then(res => res.json())
    .then(res => updateNextDepartures(res.data))
}
// Inserts next departures into Stop array
function updateNextDepartures(data) {
    console.log(data);
    stopData[0] = data["stopPlace"];
        const stopIDfromData = getStopID(data["stopPlace"]["id"]);
    closestStops[0] = data["stopPlace"]["estimatedCalls"];
    closestStops[3] = [];
    var antallAvganger = closestStops[0].length;
    if (numberOfDepartures > antallAvganger) {
      moreDepartures.style.display = "none";
      noMoreDepartures.style.display = "block";
    } else {
      moreDepartures.style.display = "block";
      noMoreDepartures.style.display = "none";
    }


    var avganger = document.getElementById('avganger');
    if (data["stopPlace"].quays[0].publicCode != "" && data["stopPlace"].quays[0].publicCode != null) {
      var update = "<tr><th>Linje</th> <th style='text-align:left'>Destinasjon</th> <th></th> <th>Avgang</th> <th>Plf</th></tr>";
    } else {
      var update = "<tr><th>Linje</th> <th style='text-align:left'>Destinasjon</th> <th></th> <th>Avgang</th></tr>";
    }

    //Write a next departures to innerHTML
    for (let a = 0; a < antallAvganger; a++) {
      var transportMode = closestStops[0][a].serviceJourney.journeyPattern.line.transportMode;
      var linje = closestStops[0][a].serviceJourney.journeyPattern.line.publicCode;
      if (linje == "" && transportMode == "air") {
        var linje = "Fly";
      }
      var serviceJourneyId = closestStops[0][a].serviceJourney.id;
      var lineId = closestStops[0][a].serviceJourney.journeyPattern.line.id;
      var operator = closestStops[0][a].serviceJourney.operator.id;
      var expectedDepartureTime = closestStops[0][a].expectedDepartureTime;
      var stopQuayId = closestStops[0][a].quay.id;
      if (lineId == "AKT:Line:395_3003") {
        var destinasjon = getFrontText(stopQuayId, a);
      } else if (serviceJourneyId.substring(0, 29) == "AKT:ServiceJourney:395_1012_1") {
        var destinasjon = "Justvik - Jærnesheia";
      } else {
        var destinasjon = closestStops[0][a].destinationDisplay.frontText;
      }
      closestStops[3][a] = destinasjon;
      var tid = timeUntilDeparture(a);
      if (closestStops[0][a].realtime == true) {
        var color = "var(--realtime-color)";
        var ca = "<div class='RealtimeIconAnimation'></div>";
      } else {
        var color = "var(--normal-color)";
        var ca = "ca.";
      }"stopPlace"
      if (data["stopPlace"].quays.length > 1) {
        if (data["stopPlace"].quays[0].publicCode != "" && data["stopPlace"].quays[0].publicCode != null) {
          update += "<tr class='tblRow' onclick=getLineDetails('" + serviceJourneyId + "','" + stopQuayId + "','" + a + "');><td class='tblLine tblCell'><div class='lineSign' style='" + getLineStyle(linje, operator) + "'>" + linje + "</div></td><td class='tblDest tblCell'>" + destinasjon + "</td><td class='tblCa tblCell'>" + ca + "</td><td style='color:" + color + ";' class='tblTime tblCell'>" + tid + "</td><td class='tblPlf tblCell'>" + closestStops[0][a].quay.publicCode + "</td></tr>";
        } else if (data["stopPlace"].quays[1].publicCode != "" && data["stopPlace"].quays[1].publicCode != null) {
          update += "<tr class='tblRow' onclick=getLineDetails('" + serviceJourneyId + "','" + stopQuayId + "','" + a + "');><td class='tblLine tblCell'><div class='lineSign' style='" + getLineStyle(linje, operator) + "'>" + linje + "</div></td><td class='tblDest tblCell'>" + destinasjon + "</td><td class='tblCa tblCell'>" + ca + "</td><td style='color:" + color + ";' class='tblTime tblCell'>" + tid + "</td><td class='tblPlf tblCell'>" + closestStops[0][a].quay.publicCode + "</td></tr>";
        } else {
          update += "<tr class='tblRow' onclick=getLineDetails('" + serviceJourneyId + "','" + stopQuayId + "','" + a + "');><td class='tblLine tblCell'><div class='lineSign' style='" + getLineStyle(linje, operator) + "'>" + linje + "</div></td><td class='tblDest tblCell'>" + destinasjon + "</td><td class='tblCa tblCell'>" + ca + "</td><td style='color:" + color + ";' class='tblTime tblCell'>" + tid + "</td></tr>";
        }
      } else {
        update += "<tr class='tblRow' onclick=getLineDetails('" + serviceJourneyId + "','" + stopQuayId + "','" + a + "');><td class='tblLine tblCell'><div class='lineSign' style='" + getLineStyle(linje, operator) + "'>" + linje + "</div></td><td class='tblDest tblCell'>" + destinasjon + "</td><td class='tblCa tblCell'>" + ca + "</td><td style='color:" + color + ";' class='tblTime tblCell'>" + tid + "</td></tr>";
      }

    }
    avganger.innerHTML = update;
}

function getFrontText(quayId, a) {
  var quayTab = ["NSR:Quay:38099","NSR:Quay:44625","NSR:Quay:40287","NSR:Quay:44603","NSR:Quay:44608","NSR:Quay:44629","NSR:Quay:44637","NSR:Quay:44623","NSR:Quay:44615","NSR:Quay:44627","NSR:Quay:41440","NSR:Quay:39769","NSR:Quay:43506","NSR:Quay:38000","NSR:Quay:43010","NSR:Quay:42112","NSR:Quay:38798","NSR:Quay:38188","NSR:Quay:42289","NSR:Quay:41293","NSR:Quay:40104","NSR:Quay:41177","NSR:Quay:38111","NSR:Quay:42135","NSR:Quay:43403","NSR:Quay:39370","NSR:Quay:39113","NSR:Quay:38012","NSR:Quay:41171","NSR:Quay:41165","NSR:Quay:40733","NSR:Quay:40739","NSR:Quay:40749","NSR:Quay:44728","NSR:Quay:44611","NSR:Quay:38242","NSR:Quay:40771","NSR:Quay:42274","NSR:Quay:38428","NSR:Quay:38432","NSR:Quay:38472","NSR:Quay:38438","NSR:Quay:41682","NSR:Quay:38417","NSR:Quay:38457","NSR:Quay:42824","NSR:Quay:40751","NSR:Quay:40738","NSR:Quay:40734","NSR:Quay:41164","NSR:Quay:41172","NSR:Quay:39962","NSR:Quay:38014","NSR:Quay:39115","NSR:Quay:39371","NSR:Quay:43404","NSR:Quay:38108","NSR:Quay:41176","NSR:Quay:40103","NSR:Quay:41294","NSR:Quay:42290","NSR:Quay:38186","NSR:Quay:42136","NSR:Quay:39050","NSR:Quay:40118","NSR:Quay:43011","NSR:Quay:101501","NSR:Quay:37999","NSR:Quay:43507","NSR:Quay:39770","NSR:Quay:41438","NSR:Quay:44626","NSR:Quay:44620","NSR:Quay:44636","NSR:Quay:38099","NSR:Quay:44625","NSR:Quay:40287","NSR:Quay:44603","NSR:Quay:44608"];

  var stopId = quayTab.indexOf(quayId);
  if (stopId > 33) {
    return "Slettheia";
  } else {
    return "Søm";
  }
}

function getLineDetails (sjId, quayId, a) {
  var destinasjon = a;
  fetch(entur_graphql_endpoint, {
      method: 'POST',
      headers: {
          'Content-Type': 'application/json',
          'ET-Client-Name': 'https://sanntid.ga'
      },
      body: JSON.stringify({ query: `
        {
          serviceJourney(id: "` + sjId + `") {
            id
            publicCode
            operator {
              id
            }
            line {
              publicCode
              name
              transportMode
            }
            estimatedCalls {
              quay {
                stopPlace {
                  name
                  id
                }
              }
              destinationDisplay {
                frontText
              }
              aimedDepartureTime
              expectedDepartureTime
              realtime
            }
            passingTimes {
              departure {
                time
              }
              quay {
                id
                stopPlace {
                  name
                  id
                }
              }
            }
          }
        }
      `}),
  })
  .then(res => res.json())
  .then(res => showLineDetails(res.data, quayId, destinasjon, sjId))
}

function showLineDetails (data, quayId, a, sjId) {
  // console.log(data);
  closestStops[1] = data["serviceJourney"];
  if (closestStops[1].line.publicCode) {
    var publicCode = closestStops[1].line.publicCode;
  } else {
    var publicCode = closestStops[1].publicCode;
  }
  var operator = closestStops[1].operator.id;
  var name = closestStops[1].line.name;
  var transportMode = closestStops[1].line.transportMode;
  var index = closestStops[1].passingTimes.findIndex( s => s.quay.id == quayId );
  var frontText = closestStops[3][a];
  var antallHoldeplasser = (closestStops[1].estimatedCalls).length;
  var antallPassingTimes = (closestStops[1].passingTimes).length;
  if (closestStops[1].estimatedCalls === undefined || closestStops[1].estimatedCalls.length == 0) {
    var realtime = false;
  } else {
    var realtime = closestStops[1].estimatedCalls[index].realtime;
  }
  if (realtime == false) {
    var departureDetailsMain = "<div class='departureDetailsMain'><div class='departureDetailsMainStopPlace'>" +
        "<div>Her: " + closestStops[1].passingTimes[index].quay.stopPlace.name + "</div><div>" + (closestStops[1].passingTimes[index].departure.time).slice(0,5) + "</div></div>";
    if (antallPassingTimes - index > 1) {
      departureDetailsMain += "<div class='departureDetailsMainStopPlace'><div>Neste: " + closestStops[1].passingTimes[index + 1].quay.stopPlace.name + "</div><div>" + (closestStops[1].passingTimes[index + 1].departure.time).slice(0,5) + "</div></div>";
    }
    if (antallPassingTimes - index > 2) {
      departureDetailsMain += "<div class='departureDetailsMainStopPlace'><div>Deretter: " + closestStops[1].passingTimes[index + 2].quay.stopPlace.name + "</div><div>" + (closestStops[1].passingTimes[index + 2].departure.time).slice(0,5) + "</div></div>";
    }
    departureDetailsMain += "</div>";
  } else {
    var departureDetailsMain = "<div class='departureDetailsMain'><div class='departureDetailsMainStopPlace'>" +
        "<div>Her: " + closestStops[1].estimatedCalls[index].quay.stopPlace.name + "</div><div>" + timeUntilDeparture((closestStops[1].estimatedCalls[index].expectedDepartureTime), false) +
        "<span style='margin-left:10px; text-decoration:line-through;'>" + (closestStops[1].estimatedCalls[index].aimedDepartureTime).slice(11,16) + "</span></div>" +
      "</div>";
    if (antallHoldeplasser - index > 1) {
      departureDetailsMain += "<div class='departureDetailsMainStopPlace'>" +
        "<div>Neste: " + closestStops[1].estimatedCalls[index + 1].quay.stopPlace.name + "</div><div>" + timeUntilDeparture((closestStops[1].estimatedCalls[index + 1].expectedDepartureTime), false) +
        "<span style='margin-left:10px; text-decoration:line-through;'>" + (closestStops[1].estimatedCalls[index + 1].aimedDepartureTime).slice(11,16) + "</span></div>" +
      "</div>";
    }
    if (antallHoldeplasser - index > 2) {
      departureDetailsMain += "<div class='departureDetailsMainStopPlace'>" +
        "<div>Deretter: " + closestStops[1].estimatedCalls[index + 2].quay.stopPlace.name + "</div><div>" + timeUntilDeparture((closestStops[1].estimatedCalls[index + 2].expectedDepartureTime), false) +
        "<span style='margin-left:10px; text-decoration:line-through;'>" + (closestStops[1].estimatedCalls[index + 2].aimedDepartureTime).slice(11,16) + "</span></div>" +
      "</div>";
    }
  }

  var inner = "<div class='departureDetailsHeader' style='width:calc(100% - 100px)'>" +
    "<div class='departureDetailsHeaderStart'>" +
      "<div><div class='departureDetailsHeaderSign' style='" + getLineStyle(publicCode, operator) + "'>" + publicCode + "</div></div>" +
      "<div class='departureDetailsHeaderDest'>" + frontText + "</div>" +
    "</div>" +
  "</div>" +
  "<button type='button' id='refresh-details-button' onclick=getLineDetails('" + sjId + "','" + quayId + "','" + a + "');showSnackbar('Oppdatert','1000') class='button' onclick='closeDepartureDetails()' style='margin-right: 10px;'>" +
    "<img src='refresh.svg' height='28px' width='28px' alt='Oppdater' style='padding: 4px -0px;'>" +
  "</button>" +
  "<button type='button' id='close-details-button' class='button' onclick='closeDepartureDetails()'>" +
    "<img src='close.svg' height='28px' width='28px' alt='Tilbake' style='padding: 4px -0px;'>" +
  "</button>" +
  "<div style='clear:both;font-size: 16px;border-bottom: 1px solid;margin-bottom: 5px;padding-bottom: 2px;'>Rutedetaljer:</div>" + departureDetailsMain +
  "<div onclick='showAllLineDetails(" + a + ", " + index + ")' style='clear:both;font-size: 18px;/*border: 1px solid var(--third-color);*/margin-top: 15px;padding: 7px;background-color: var(--secondary-color);border-radius: 5px;cursor: pointer;text-align:center'>Alle holdeplasser</div>";
  departureDetailsContent.innerHTML = inner;
  departureDetailsExtend.style.display = "block";
  departureDetails.style.display = "block";
}

function showAllLineDetails(a, index) {
  if (closestStops[1].line.publicCode) {
    var publicCode = closestStops[1].line.publicCode;
  } else {
    var publicCode = closestStops[1].publicCode;
  }
  var operator = closestStops[1].operator.id;
  var name = closestStops[1].line.name;
  var transportMode = closestStops[1].line.transportMode;
  var frontText = closestStops[3][a];
  var antallHoldeplasser = (closestStops[1].estimatedCalls).length;
  var antallPassingTimes = (closestStops[1].passingTimes).length;
  if (closestStops[1].estimatedCalls === undefined || closestStops[1].estimatedCalls.length == 0) {
    var realtime = false;
  } else {
    var realtime = closestStops[1].estimatedCalls[index].realtime;
  }
  if (realtime == true) {
    var departureDetailsMain = "<div class='departureDetailsMain'><table id='tableAll' style='margin-top:0px;'>";
    for (var i = 0; i < antallHoldeplasser; i++) {
      if (i == index) {
        var textColor = "var(--text-thisStopPlace)";
      } else {
        var textColor = "var(--text-primary)";
      }
      departureDetailsMain += "<tr style='border-bottom:1px solid'><td style='color:" + textColor + ";white-space: nowrap; text-overflow:ellipsis; overflow: hidden; max-width:1px;font-size:17px;padding:4px 0px;'><a style='color:" + textColor + ";text-decoration: none;' href='holdeplass.html?h=" + (closestStops[1].estimatedCalls[i].quay.stopPlace.id).slice(14) + "'>" + closestStops[1].estimatedCalls[i].quay.stopPlace.name + "</a></td><td style='color:" + textColor + ";width:1px;font-size:17px;padding:4px 0px;'>" + (closestStops[1].estimatedCalls[i].expectedDepartureTime).slice(11,16) +
        "<span style='margin-left:10px; text-decoration:line-through;'>" + (closestStops[1].estimatedCalls[i].aimedDepartureTime).slice(11,16) + "</span></td></tr>";
    }
    departureDetailsMain += "</table></div>";
  } else {
    var departureDetailsMain = "<div class='departureDetailsMain'><table id='tableAll' style='margin-top:0px;'>";
    for (var i = 0; i < antallPassingTimes; i++) {
      if (i == index) {
        var textColor = "var(--text-thisStopPlace)";
      } else {
        var textColor = "var(--text-primary)";
      }
      departureDetailsMain += "<tr style='border-bottom:1px solid'><td style='color:" + textColor + ";white-space: nowrap; text-overflow:ellipsis; overflow: hidden; max-width:1px;font-size:17px;padding:4px 0px;'><a style='color:" + textColor + ";text-decoration: none;' href='holdeplass.html?h=" + (closestStops[1].passingTimes[i].quay.stopPlace.id).slice(14) + "'>" + closestStops[1].passingTimes[i].quay.stopPlace.name + "</a></td><td style='color:" + textColor + ";width:1px;font-size:17px;padding:4px 0px;'>" + (closestStops[1].passingTimes[i].departure.time).slice(0,5) + "</td></tr>";
    }
    departureDetailsMain += "</table></div>";
  }

  var inner = "<div class='main' style='position: fixed;top: 0px;left: 0px;background-color: var(--primary-color);'><div style='padding: 20px 20px 0px 20px;'><div class='departureDetailsHeader' style='width:calc(100% - 50px)'>" +
    "<div class='departureDetailsHeaderStart'>" +
      "<div><div class='departureDetailsHeaderSign' style='" + getLineStyle(publicCode, operator) + "'>" + publicCode + "</div></div>" +
      "<div class='departureDetailsHeaderDest'>" + frontText + "</div>" +
    "</div>" +
  "</div>" +
  "<button type='button' id='close-details-all-button' class='button' onclick='closeDepartureDetailsAll()' style='float:right'>" +
    "<img src='close.svg' height='28px' width='28px' alt='Tilbake' style='padding: 4px -0px;'>" +
  "</button></div></div>" +
  "<div style='clear:both;font-size: 16px;border-bottom: 1px solid;margin-bottom: 5px;padding-bottom: 2px;margin-top: 55px;'>Rutedetaljer:</div>" + departureDetailsMain;

  departureDetailsAllContent.innerHTML = inner;
  departureDetailsAll.style.display = "block";
  var row = document.getElementById('tableAll').getElementsByTagName("tr")[index];
  document.getElementById('departureDetailsAll').scrollTop = (row.offsetTop - document.getElementById('departureDetailsAll').offsetHeight/3);
}

closeDetailsButton.onclick = closeDepartureDetails;

function closeDepartureDetails() {
  departureDetails.style.display = "none";
  departureDetailsExtend.style.display = "none";
}

function closeDepartureDetailsAll() {
  departureDetailsAll.style.display = "none";
}

function getLineStyle(linje, operator) {
  linje += "";

  var b;
  var c;

  if (operator == "AVI:Operator:SK") {
    b = "#000066"; c = "#ffffff";
  } else if (operator == "AVI:Operator:DY") {
    b = "#d81939"; c = "#ffffff";
  } else if (operator == "RUT:Operator:210") {
    b = "#ec700c"; c = "#ffffff";
  } else if (operator == "RUT:Operator:220") {
    b = "#0b91ef"; c = "#ffffff";
  } else if (operator == "RUT:Operator:160") {
    b = "#e60000"; c = "#ffffff";
  } else if (operator == "RUT:Operator:120" || operator == "RUT:Operator:130" || operator == "RUT:Operator:130a" || operator == "RUT:Operator:130b" || operator == "RUT:Operator:130c" || operator == "RUT:Operator:140") {
    b = "#76a300"; c = "#ffffff";
  } else {
    switch (linje) {
      // Kristiansand - metrobuss
      case "M1": b = "#61f230"; c = "#000000"; break;
      case "M2": b = "#ff3366"; c = "#000000"; break;
      case "M3": b = "#33ffff"; c = "#000000"; break;
      // Østlandet - tog
      case "F1": b = "#949494"; c = "#ffffff"; break;
      case "F1x": b = "#949494"; c = "#ffffff"; break;
      case "F2": b = "#949494"; c = "#ffffff"; break;
      case "L1": b = "#d72d82"; c = "#ffffff"; break;
      case "L2": b = "#64c8ff"; c = "#000000"; break;
      case "L2x": b = "#ffffff"; c = "#64c8ff; border: 1px solid; padding: 5.5px;"; break;
      case "L3": b = "##8cbe23"; c = "#000000"; break;
      case "L3x": b = "#ffffff"; c = "#8cbe23; border: 1px solid; padding: 5.5px;"; break;
      case "L12": b = "#dc0000"; c = "#ffffff"; break;
      case "L13": b = "#f0871e"; c = "#000000"; break;
      case "L13x": b = "#ffffff"; c = "#f0871e; border: 1px solid; padding: 5.5px;"; break;
      case "L14": b = "#fcc414"; c = "#000000"; break;
      case "L14x": b = "#ffffff"; c = "#fcc414; border: 1px solid; padding: 5.5px;"; break;
      case "L21": b = "#8c69aa"; c = "#ffffff"; break;
      case "L22": b = "#005aaf"; c = "#ffffff"; break;
      case "L22x": b = "#ffffff"; c = "#005aaf; border: 1px solid; padding: 5.5px;"; break;
      case "R10": b = "#a0001e"; c = "#ffffff"; break;
      case "R11": b = "#ee1c25"; c = "#ffffff"; break;
      case "R11E": b = "#ffffff"; c = "#ee1c25; border: 1px solid; padding: 5.5px;"; break;
      case "R11x": b = "#ffffff"; c = "#ee1c25; border: 1px solid; padding: 5.5px;"; break;
      case "R20": b = "#873287"; c = "#ffffff"; break;
      case "R20x": b = "#ffffff"; c = "#873287; border: 1px solid; padding: 5.5px;"; break;
      case "R30": b = "#05782d"; c = "#ffffff"; break;
      case "R30x": b = "#ffffff"; c = "#05782d; border: 1px solid; padding: 5.5px;"; break;
      case "": b = ""; c = ""; break;
      case "Fly": b = "#ffffff"; c = "#000000; border: 1px solid; padding: 5.5px;"; break;
      // default: b = "#000000"; c = "#ffff00"; break;
      default: b = "#ffffff"; c = "#000000; border: 1px solid; padding: 5.5px;"; break;
    }
  }
  return "color:" + c + "; background-color:" + b;
}

function getTransportMode(mode) {
  mode += "";

  switch (mode) {
    case "bus":
      return "buss";
    case "tram":
      return "trikk";
    case "metro":
      return "t-bane";
    case "air":
      return "fly";
    case "rail":
      return "tog";
    case "water":
      return "bat";
    default:
      return mode;
  }
}

// Translates keyword into form of transportation
function getMode(mode) {
    // Convert to string
    mode += "";

    switch (mode) {
        case "onstreetBus":
            return "buss";
        case "railStation":
            return "tog";
        case "metroStation":
            return "t-bane";
        case "busStation":
            return "bussterminal";
        case "coachStation":
            return "bussterminal";
        case "onstreetTram":
            return "trikk";
        case "tramStation":
            return "trikk";
        case "harbourPort":
            return "bat";
        case "ferryPort":
            return "ferje";
        case "ferryStop":
            return "ferje";
        case "lift":
            return "heis";
        case "airport":
            return "fly";
        case "multimodal":
            return "knutepunkt";
        default:
            return "anna";
    }
}


// Translates minutes until departure to readable text
function timeUntilDeparture(a, b) {
    // Checking what format recieved
    if (b == false) {
      var c = a;
    } else {
      var c = closestStops[0][a].expectedDepartureTime;
    }
    var d = new Date(c.slice(0,4), c.slice(5,7)-1, c.slice(8,10), c.slice(11,13), c.slice(14,16), c.slice(17,19));
    var n = new Date();
    let minutesUntil = ((d.getTime() - n.getTime()) / 60000).toFixed(0);

    switch (Number(minutesUntil)) {
        case 0:
            return "Nå";
        case 1:
            return "1 min";
        case 2:
            return "2 min";
        case 3:
            return "3 min";
        case 4:
            return "4 min";
        case 5:
            return "5 min";
        case 6:
            return "6 min";
        case 7:
            return "7 min";
        case 8:
            return "8 min";
        case 9:
            return "9 min";
        default:
            // return closestStops[0][a].expectedDepartureTime.slice(11,16);
            return c.slice(11,16);
    }
}


function getStationName(station) {
    return station;
}


// Removes NSR:StopID:
function getStopID(IdString) {
    return (IdString.slice(14));
}

moreDepartures.onclick = function () {
  numberOfDepartures = numberOfDepartures + 25;
  getNextDepartureForStop(h, numberOfDepartures);
}

function showSnackbar(melding, tid) {
  if (departureDetails.style.display == "block") {
    var height = departureDetails.offsetHeight;
    snackbarContainer.style.bottom = height + "px";
  } else {
    snackbarContainer.style.bottom = "0px";
  }
  snackbar.innerHTML = melding;
  snackbarContainer.style.opacity = 1;
  setTimeout( function() {
    snackbarContainer.style.opacity = 0;
    snackbarContainer.style.bottom = "-150px";
  }, tid);
}
