var pixelsPerGrado=10;
var gradiOffset=10;

// Funzione handleRefresh : 
// richiama il web service attraverso JSONP ogni volta ricreando nella DOM il tag script 
// sostituendolo all'ultimo creato se presente
function handleRefresh(url) {
	//var url ="http://192.34.62.109:8080/WeatherStation/MeteoServices/Readings/lastReading/updateMeteoReadings";
	var newScriptElement = document.createElement("script");
	newScriptElement.setAttribute("src",url);
	newScriptElement.setAttribute("id","jsonp");
		
	var oldScriptElement = document.getElementById("jsonp");
	var head = document.getElementsByTagName("head")[0];
	if (oldScriptElement == null) {
		head.appendChild(newScriptElement);
	}
	else {
		head.replaceChild(newScriptElement, oldScriptElement);	
	}
}

function degreesToRadians(degrees) {
	return (degrees * Math.PI)/180;
}
function disegnatermometro () {
	canvas = document.getElementById("termometro");
	context = canvas.getContext("2d");
	context.fillStyle = "lightblue";
	context.fillRect(50, 90, 600, 20);

	context.beginPath();
	context.arc(30,100,25,0,degreesToRadians(360),false);
	context.fillStyle = "red";
	context.fill();
	
	disegnaMarkers(context,-5,false);
	disegnaMarkers(context,0,true);
	disegnaMarkers(context,5,false);
	disegnaMarkers(context,10,true);
	disegnaMarkers(context,15,false);
	disegnaMarkers(context,20,true);
	disegnaMarkers(context,25,false);
	disegnaMarkers(context,30,true);
	disegnaMarkers(context,35,false);
	disegnaMarkers(context,40,true);
	disegnaMarkers(context,45,false);
}
function disegnaMarkers(context,gradi,grande) {
	context.fillStyle = "black";
	context.beginPath();
	context.moveTo(50+(gradi*pixelsPerGrado) + (gradiOffset*pixelsPerGrado),grande?70:80);
	context.lineTo(50+(gradi*pixelsPerGrado) + (gradiOffset*pixelsPerGrado),grande?130:120);
	context.stroke();
	if (grande) context.font = "10px Lucida Grande";
	else 	    context.font = "8px Lucida Grande";
	context.fillText(gradi, 50+(gradi*pixelsPerGrado) + (gradiOffset*pixelsPerGrado), 140);
}
function disegnaLayoutTermometro() {
	canvas = document.getElementById("termometro");
	context = canvas.getContext("2d");
	context.fillStyle = "lightblue";
	context.fillRect(50, 90, 600, 20);
	context.beginPath();
	context.arc(30,100,25,0,degreesToRadians(360),false);
	context.fillStyle = "red";
	context.fill();
}

function disegnaBarraGradi(gradi) {
	disegnaLayoutTermometro();
	canvas = document.getElementById("termometro");
	context = canvas.getContext("2d");
	context.fillStyle = "red";
	context.fillRect(50, 90, (gradi*pixelsPerGrado) + (gradiOffset*pixelsPerGrado), 20);
	
	context.fillStyle = "#EBEBEB";
	context.fillRect(50, 160, 650, 40);
	context.fillStyle = "red";
	context.fillText(gradi.toFixed(1)+"° gradi", 50, 175);
	
}
function updateMeteoReadings(sales) {
    var salesDiv = document.getElementById("sales");
    for (var i = 0; i < sales.length; i++) {
            var sale = sales[i];
            //salesDiv.innerHTML ="Ultima lettura :"+ + sale.temperatura_esterna+"° aggiornata al "+sale.data;
            salesDiv.innerHTML ="Lettura rilevata il "+sale.data;
            canvas = document.getElementById("termometro");
            context = canvas.getContext("2d");
            context.font = "10px Lucida Grande";
            animate(gradiUltimaLettura,sale.temperatura_esterna);
    }
}
function updateMeteo(meteo) {
	var salesDiv = document.getElementById("sales");
	for (var i = 0; i < sales.length; i++) {
		var sale = sales[i];
		salesDiv.innerHTML = sale.Temperatura;
		disegnaBarraGradi(sale.temperatura_esterna);
	}
}

var gradiDinamiciPartenza=0;
var gradiDinamiciDestinazione=0;
var ani;
var gradiUltimaLettura=-10;
var verso=false;
function animate(partenza,destinazione) {
	if (partenza<destinazione) verso=true;
	else verso=false;
	gradiDinamiciPartenza=partenza;
	gradiDinamiciDestinazione=destinazione;
	ani = setInterval(disegnaBarraGradiAnimata, 20);
}
function disegnaBarraGradiAnimata()
{
	var partenza=Math.round(gradiDinamiciPartenza*100)/100;
	var destinazione=Math.round(gradiDinamiciDestinazione*100)/100;

	if ((verso == true && (partenza>destinazione)) || (verso==false && (partenza<destinazione))) {
		clearInterval(ani);
		ani=null;
		gradiUltimaLettura=partenza;
		return;
	}
	disegnaBarraGradi(gradiDinamiciPartenza);
	if (verso) gradiDinamiciPartenza=gradiDinamiciPartenza+0.1;
	else gradiDinamiciPartenza=gradiDinamiciPartenza-0.1;
}
