package com.crunchify.meteo;

public enum Parameters {
	id(1,"id","id"),
	data(2,"data","data"),
	umidita_interna(4,"umidita_interna","umidita_interna"),
	temperatura_interna(8,"temperatura_interna","temperatura_interna"),
	umidita_esterna(16,"umidita_esterna","umidita_esterna"),
	temperatura_esterna(32,"temperatura_esterna","temperatura_esterna"),
	vento_medio(64,"vento_medio","vento_medio"),
	vento_raffica(128,"vento_raffica","vento_raffica"),
	pressione(256,"pressione","pressione + 32.4 as pressione"),
	pioggia_parziale(512,"pioggia_parziale","pioggia_parziale"),
	vento_direzione(1024,"vento_direzione","vento_direzione")
	;
	
	private int coefficiente;
	private String nomecampo;
	private String mysqlSelectStatement;
	
	private Parameters(int coefficiente,String nomecampo,String mysqlSelectStatement) {
		this.coefficiente=coefficiente;
		this.nomecampo=nomecampo;
		this.mysqlSelectStatement=mysqlSelectStatement;
	}

	public int getCoefficiente() {
		return coefficiente;
	}

	public String getNomecampo() {
		return nomecampo;
	}
	
	public String getMysqlSelectStatement() {
		return mysqlSelectStatement;
	}
	
	
}
