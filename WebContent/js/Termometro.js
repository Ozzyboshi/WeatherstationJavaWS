var parametri={
	"margineX" : 145,			   // Input
	"margineY" : 60,			   // Input
	"width"	   : 20,			   // Input
	"height"   : 600,			   // Input
	"nomeDivTermometro" : "",		   // Input
	"orientation" : false,			   // Input
	"gradiOffset" : 10,			   // Input
	"pixelsPerGrado" : 10, 			   //Input
	"raggioBaseTermometro" : 25, 		   //Input
	"dimensioni_marcatore_gradi_piccolo" : 10, // questo parametro indica la lunghezza in pixel della barra trasversale disegnata sopra il termometro
	"dimensioni_marcatore_gradi_grande"  : 20, // questo parametro indica la lunghezza in pixel della barra trasversale disegnata sopra il termometro

	"barragradi": 	  {x:0,y:0,width:0,height:0},   //calcolati
	"basetermometro": {x:0,y:0,raggio:0},       	//calcolati
	"context"	: null
};

// Funzione handleRefresh : 
// richiama il web service attraverso JSONP ogni volta ricreando nella DOM il tag script 
// sostituendolo all'ultimo creato se presente
function handleRefresh(url) {
	var newScriptElement = document.createElement("script");
	newScriptElement.setAttribute("src",url);
	newScriptElement.setAttribute("id","jsonp");

	var oldScriptElement = document.getElementById("jsonp");
	var head = document.getElementsByTagName("head")[0];
	if (oldScriptElement == null)
		head.appendChild(newScriptElement);
	else
		head.replaceChild(newScriptElement, oldScriptElement);	
}

function degreesToRadians(degrees) {
	return (degrees * Math.PI)/180;
}
function disegnatermometro (nome_termometro_div,orientation_local) {
	parametri.nomeDivTermometro=nome_termometro_div;
	parametri.orientation=orientation_local;
	parametri.basetermometro.raggio=parametri.raggioBaseTermometro;

	if (parametri.orientation==false) {
		parametri.barragradi.x=parametri.margineX;
		parametri.barragradi.y=parametri.margineY;
		parametri.barragradi.width=parametri.width;
		parametri.barragradi.height=parametri.height;
		parametri.basetermometro.x=parametri.barragradi.x;
		parametri.basetermometro.y=parametri.barragradi.y+parametri.barragradi.height/2;
	}
	else {
		parametri.barragradi.x=parametri.margineX;
		var canvas = document.getElementById(nome_termometro_div);
		parametri.barragradi.y=canvas.height-parametri.margineY;
		parametri.barragradi.width=parametri.width;
		parametri.barragradi.height=parametri.height*-1;
		parametri.basetermometro.y=parametri.barragradi.y;
		parametri.basetermometro.x=parametri.barragradi.x+parametri.barragradi.width/2;
	}
	
	disegnaLayoutTermometro();

	canvas = document.getElementById(nome_termometro_div);
	parametri.context = canvas.getContext("2d");
}

function disegnaMarkers() {
	disegnaMarker(parametri.context,-5,false);
	disegnaMarker(parametri.context,0,true);
	disegnaMarker(parametri.context,5,false);
	disegnaMarker(parametri.context,10,true);
	disegnaMarker(parametri.context,15,false);
	disegnaMarker(parametri.context,20,true);
	disegnaMarker(parametri.context,25,false);
	disegnaMarker(parametri.context,30,true);
	disegnaMarker(parametri.context,35,false);
	disegnaMarker(parametri.context,40,true);
	disegnaMarker(parametri.context,45,false);
}

function disegnaMarker(context,gradi,grande) {
	canvas = document.getElementById(parametri.nomeDivTermometro);
	context = canvas.getContext("2d");
	var marcatoreGradi;
	if(grande) marcatoreGradi=parametri.dimensioni_marcatore_gradi_grande;
	else	   marcatoreGradi=parametri.dimensioni_marcatore_gradi_piccolo;
	context.fillStyle = "black";
	context.beginPath();
	
	if (parametri.orientation==false) {
		context.moveTo(parametri.barragradi.x+(gradi*parametri.pixelsPerGrado) + (parametri.gradiOffset*parametri.pixelsPerGrado),parametri.barragradi.y-marcatoreGradi);
		context.lineTo(parametri.barragradi.x+(gradi*parametri.pixelsPerGrado) + (parametri.gradiOffset*parametri.pixelsPerGrado),parametri.barragradi.y+parametri.barragradi.height+marcatoreGradi);
	}
	else {
		context.moveTo(parametri.barragradi.x-marcatoreGradi,parametri.barragradi.y-(gradi*parametri.pixelsPerGrado) - (parametri.gradiOffset*parametri.pixelsPerGrado));
		context.lineTo(parametri.barragradi.x-marcatoreGradi+parametri.barragradi.width+(marcatoreGradi*2),parametri.barragradi.y-(gradi*parametri.pixelsPerGrado) - (parametri.gradiOffset*parametri.pixelsPerGrado));
	}
	context.stroke();
	if (grande)
		context.font = "10px Lucida Grande";
	else
 	    context.font = "8px Lucida Grande";

	if (parametri.orientation==false)
		context.fillText(gradi, parametri.barragradi.x+(gradi*parametri.pixelsPerGrado) + (parametri.gradiOffset*parametri.pixelsPerGrado),parametri.barragradi.y+parametri.barragradi.height+parametri.dimensioni_marcatore_gradi_grande+25);
	else
		context.fillText(gradi,25+parametri.barragradi.x-parametri.dimensioni_marcatore_gradi_grande+parametri.barragradi.width+(parametri.dimensioni_marcatore_gradi_grande*2), parametri.barragradi.y-(gradi*parametri.pixelsPerGrado) - (parametri.gradiOffset*parametri.pixelsPerGrado));
	
}
function disegnaLayoutTermometro() {
	canvas = document.getElementById(parametri.nomeDivTermometro);
	parametri.context = canvas.getContext("2d");
	parametri.context.fillStyle = "lightblue";
	parametri.context.fillRect(parametri.barragradi.x, parametri.barragradi.y,parametri.barragradi.width , parametri.barragradi.height);
	parametri.context.beginPath();
	parametri.context.arc(parametri.basetermometro.x,parametri.basetermometro.y,parametri.basetermometro.raggio,0,degreesToRadians(360),false);
	parametri.context.fillStyle = "red";
	parametri.context.fill();

	disegnaMarkers();
}

// In base ad un coefficiente dei gradi disegno un rettangolo rosso
function disegnaBarraGradi(gradi) {
	//Ricopro l'eventuale rettangolo rosso dei gradi disegnato al ciclo precedente
	parametri.context.fillStyle = "lightblue";
	if (parametri.orientation==false)
		parametri.context.fillRect(parametri.barragradi.x+parametri.basetermometro.raggio, parametri.barragradi.y,parametri.barragradi.width , parametri.barragradi.height);
	else
		parametri.context.fillRect(parametri.barragradi.x,parametri.barragradi.y-parametri.basetermometro.raggio,parametri.barragradi.width , parametri.barragradi.height);
	
	parametri.context = canvas.getContext("2d");
	parametri.context.fillStyle = "red";

	if (parametri.orientation==false)
		parametri.context.fillRect(parametri.barragradi.x, parametri.barragradi.y, (gradi*parametri.pixelsPerGrado) + (parametri.gradiOffset*parametri.pixelsPerGrado), 20);
	else
		parametri.context.fillRect(parametri.barragradi.x, parametri.barragradi.y,parametri.barragradi.width, -((gradi*parametri.pixelsPerGrado) + (parametri.gradiOffset*parametri.pixelsPerGrado)));

	// Questo serve per cancellare e riscrivere la dicitura con i gradi
	if (parametri.orientation==false) {
		parametri.context.clearRect(parametri.barragradi.x, parametri.barragradi.y+parametri.basetermometro.raggio+10, 650, 40);
		parametri.context.fillText(gradi.toFixed(1)+"° gradi", parametri.barragradi.x, parametri.barragradi.y+parametri.basetermometro.raggio+35);
	}
	else{
		parametri.context.clearRect(parametri.barragradi.x+parametri.barragradi.width+25, parametri.barragradi.y-parametri.basetermometro.raggio, 650, 40);
		parametri.context.fillStyle = "red";
		parametri.context.fillText(gradi.toFixed(1)+"° gradi", parametri.barragradi.x+parametri.barragradi.width+25, parametri.barragradi.y);
	}	
}

//Funzione che viene chiamata automaticamente ad ogni refresh attraverso JSONP
function updateMeteoReadings(sales) {
    var salesDiv = document.getElementById("sales");
    for (var i = 0; i < sales.length; i++) {
            var sale = sales[i];
            salesDiv.innerHTML ="Lettura rilevata il "+sale.data;
            animate(gradiUltimaLettura,sale.temperatura_esterna);
    }
}
/*function updateMeteo(meteo) {
	var salesDiv = document.getElementById("sales");
	for (var i = 0; i < sales.length; i++) {
		var sale = sales[i];
		salesDiv.innerHTML = sale.Temperatura;
		disegnaBarraGradi(sale.temperatura_esterna);
	}
}*/

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
