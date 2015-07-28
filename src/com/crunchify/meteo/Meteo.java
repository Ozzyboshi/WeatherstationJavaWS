package com.crunchify.meteo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mysql.jdbc.Statement;

import javax.ws.rs.core.MediaType;

@Path("/Readings")
public class Meteo {
	
	private final String user = System.getenv("MYSQL_USER");
	private final String database = System.getenv("MYSQL_DATABASE");
	private final String password = System.getenv("MYSQL_PASSWORD");
	private final String host = System.getenv("MYSQL_HOST");
	
	private Connection getWeatherConnectionObject() throws SQLException {
		return DriverManager.getConnection("jdbc:mysql://"+host+"/" + database+"?user="+user+"&password="+password);
	}
	
	@Path("/lastReading/{f}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getLastReading(@PathParam("f") String f,@DefaultValue("0") @QueryParam("filtro") int filtro) throws JSONException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		JSONObject jsonObject = new JSONObject();
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		Connection db = getWeatherConnectionObject();
		Statement stmt =  (Statement) db.createStatement();
		stmt.executeQuery("SELECT "+getMysqlFields()+" FROM letture order by data desc limit 1");
		ResultSet rs = stmt.getResultSet ();
		while (rs.next ()){
			getFilteredMysqlData(jsonObject,rs,filtro);
		}

		jsonObject.put("day_data", getLastRainfallsAndAvgTempData(10,1,"Europe/Rome",1,null));
		jsonObject.put("last_rainfall_data", getLastRainfallDay());
		return "var lettura = ["+jsonObject.toString()+"];\n"+f+"(lettura);";
    }
	
	@Path("/LastRainfallsAndAvgTemp/{f}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getLastRainfallsAndAvgTemp(@PathParam("f") String function,@DefaultValue("10") @QueryParam("datelength") int f,@DefaultValue("30") @QueryParam("days") int days,@DefaultValue("Europe/Rome") @QueryParam("tz") String tz,@DefaultValue("1") @QueryParam("timeunit") int timeunit,@DefaultValue("") @QueryParam("period") String period) throws JSONException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		JSONArray jsonMainArr=getLastRainfallsAndAvgTempData(f,days,tz,timeunit,period);
		return "var letture = "+jsonMainArr.toString()+";\n"+function+"(letture);";
    }
	public JSONArray getLastRainfallsAndAvgTempData(int length,int days,String tz,int timeunit,String period) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, JSONException {
		String timeunitstr="";
		if (days>360) days=360;
		if (length!=10 && length!=7 && length!=13) length=10;
		if (timeunit==1)
			timeunitstr="day";
		else if (timeunit==2)
			timeunitstr="hour";
		else
			timeunitstr="month";
		
		if (tz.contains("'"))
			tz="Europe/Rome";
		
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		Connection db = getWeatherConnectionObject();
		Statement stmt =  (Statement) db.createStatement();
		if (period==null || period.length()==0)
			stmt.executeQuery("select left(CONVERT_TZ(data,'UTC','"+tz+"'),"+length+") as data ,sum(pioggia_parziale) as pioggia,avg(temperatura_esterna) as temp_esterna_media from meteo.letture where left(CONVERT_TZ(data,'UTC','"+tz+"'),"+length+")>left(now() - interval "+days+" "+timeunitstr+","+length+") group by left(CONVERT_TZ(data,'UTC','"+tz+"'),"+length+") order by data desc");
		else
			stmt.executeQuery("select left(CONVERT_TZ(data,'UTC','"+tz+"'),"+length+") as data ,sum(pioggia_parziale) as pioggia,avg(temperatura_esterna) as temp_esterna_media from meteo.letture where left(CONVERT_TZ(data,'UTC','"+tz+"'),"+days+")=left('"+period+"',"+days+") group by left(CONVERT_TZ(data,'UTC','"+tz+"'),"+length+") order by data desc");
		
		ResultSet rs = stmt.getResultSet ();
		JSONArray jsonMainArr = new JSONArray();
		rs = stmt.getResultSet ();
		while (rs.next ()){
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("data",rs.getString("data"));
			jsonObject.put("pioggia",rs.getString("pioggia"));
			jsonObject.put("temp_esterna_media",rs.getString("temp_esterna_media"));
			jsonMainArr.put(jsonObject);
		}
		db.close();
		return jsonMainArr;
	}
	
	private JSONObject getLastRainfallDay() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, JSONException {
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		Connection db = getWeatherConnectionObject();
		Statement stmt =  (Statement) db.createStatement();
		stmt.executeQuery("select * from (SELECT (@statusPre <> pioggia_totale) AS statusChanged , @statusPre := pioggia_totale,id,data from meteo.letture , (SELECT @statusPre:=NULL) AS d  order by data) as good where statusChanged order by id desc limit 1");
		ResultSet rs = stmt.getResultSet ();
		rs = stmt.getResultSet ();
		JSONObject jsonObject = new JSONObject();
		while (rs.next ()){
			jsonObject.put("data",rs.getString("data"));
		}
		db.close();
		return jsonObject;
	}
	
	
	@Path("/lastReadings/{f}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getLastNReadings(@PathParam("f") String f,@DefaultValue("2") @QueryParam("step") int step) throws JSONException, SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		Connection db = getWeatherConnectionObject();
		
		Statement stmt =  (Statement) db.createStatement();
		stmt.executeQuery("SELECT * FROM letture order by data desc limit "+step);
		ResultSet rs = stmt.getResultSet ();
		JSONArray jsonMainArr = new JSONArray();
		rs.afterLast();
		while (rs.previous ()){
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("id", rs.getString("id"));
			jsonObject.put("data", rs.getString("data"));
			jsonObject.put("umidita_interna", rs.getString("umidita_interna"));
			jsonObject.put("temperatura_interna", rs.getString("temperatura_interna"));
			jsonObject.put("umidita_esterna", rs.getString("umidita_esterna"));
			jsonObject.put("temperatura_esterna", rs.getString("temperatura_esterna"));
			jsonObject.put("pressione", rs.getString("pressione"));
			jsonObject.put("vento_medio", rs.getString("vento_medio"));
			jsonObject.put("vento_raffica", rs.getString("vento_raffica"));
			jsonObject.put("vento_direzione", rs.getString("vento_direzione"));
			jsonObject.put("pioggia_totale", rs.getString("pioggia_totale"));
			jsonObject.put("pioggia_parziale", rs.getString("pioggia_parziale"));
			jsonMainArr.put(jsonObject);
		}
		db.close();
		//return jsonMainArr.toString();
		return "var sales = "+jsonMainArr.toString()+";\n"+f+"(sales);";
	}
	
	@Path("/lastMinReading/{f}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getLastMinReading(@PathParam("f") String f,@DefaultValue("temperatura_esterna") @QueryParam("orderby") String step) throws JSONException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		
		JSONObject jsonObject = new JSONObject();
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		Connection db = getWeatherConnectionObject();
		Statement stmt =  (Statement) db.createStatement();
		stmt.executeQuery("SELECT * FROM letture where left(data,10)=left(now(),10) order by "+step+" limit 1");
		ResultSet rs = stmt.getResultSet ();
		while (rs.next ()){
			getMysqlData(jsonObject,rs);
		}

		return "var sales = ["+jsonObject.toString()+"];\n"+f+"(sales);";
    }
	
	@Path("/lastMaxReading/{f}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getLastMaxReading(@PathParam("f") String f,@DefaultValue("temperatura_esterna") @QueryParam("orderby") String step) throws JSONException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		
		JSONObject jsonObject = new JSONObject();
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		Connection db = getWeatherConnectionObject();
		Statement stmt =  (Statement) db.createStatement();
		stmt.executeQuery("SELECT * FROM letture where left(data,10)=left(now(),10) order by "+step+" desc limit 1");
		ResultSet rs = stmt.getResultSet ();
		while (rs.next ()){
			getMysqlData(jsonObject,rs);
		}
		return "var sales = ["+jsonObject.toString()+"];\n"+f+"(sales);";
    }
	
	@Path("/readingsByDay/{f}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getReadingsByDay(@PathParam("f") String f,@DefaultValue("") @QueryParam("date") String date,@DefaultValue("2") @QueryParam("filtro") int filtro,@DefaultValue("Europe/Rome") @QueryParam("tz") String tz) throws JSONException, SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		if (tz.contains("'"))
			tz="Europe/Rome";
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		Connection db = getWeatherConnectionObject();
		
		Statement stmt =  (Statement) db.createStatement();
		stmt.executeQuery("SELECT "+getMysqlFields()+" FROM letture where left(CONVERT_TZ(data,'UTC','"+tz+"'),10)='"+date+"' order by data desc");
		ResultSet rs = stmt.getResultSet ();
		JSONArray jsonMainArr = new JSONArray();
		rs.afterLast();
		while (rs.previous ()){
			JSONObject jsonObject = new JSONObject();
			getFilteredMysqlData(jsonObject,rs,filtro);
			jsonMainArr.put(jsonObject);
		}
		db.close();
		return "var letture = "+jsonMainArr.toString()+";\n"+f+"(letture);";
	}
	
	
	@Path("/lastWeekReadings/{f}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getLastWeekReadings(@PathParam("f") String f,@DefaultValue("2") @QueryParam("filtro") int filtro) throws JSONException, SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {

		Class.forName("com.mysql.jdbc.Driver").newInstance();
		Connection db = getWeatherConnectionObject();
		
		Statement stmt =  (Statement) db.createStatement();
		stmt.executeQuery("SELECT "+getMysqlFields()+" FROM letture where data between now()-interval 7 day and now() order by data desc");
		ResultSet rs = stmt.getResultSet ();
		JSONArray jsonMainArr = new JSONArray();
		rs.afterLast();
		while (rs.previous ()){
			JSONObject jsonObject = new JSONObject();
			getFilteredMysqlData(jsonObject,rs,filtro);
			jsonMainArr.put(jsonObject);
		}
		db.close();
		return "var sales = "+jsonMainArr.toString()+";\n"+f+"(sales);";
	}
	
	@Path("/last24HReadings/{f}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getLast24HReadings(@PathParam("f") String f,@DefaultValue("2") @QueryParam("filtro") int filtro) throws JSONException, SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		return getLastXHReadings(f, filtro, 24);
	}
	
	@Path("/last48HReadings/{f}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getLast48HReadings(@PathParam("f") String f,@DefaultValue("2") @QueryParam("filtro") int filtro) throws JSONException, SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		return getLastXHReadings(f, filtro, 48);
	}
	
	@Path("/last96HReadings/{f}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getLast96HReadings(@PathParam("f") String f,@DefaultValue("2") @QueryParam("filtro") int filtro) throws JSONException, SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		return getLastXHReadings(f, filtro, 96);
	}
	
	@Path("/last168HReadings/{f}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getLast168HReadings(@PathParam("f") String f,@DefaultValue("2") @QueryParam("filtro") int filtro) throws JSONException, SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		return getLastXHReadings(f, filtro, 168);
	}
	
	@Path("/lastXHReadings/{f}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getLastXHReadings(@PathParam("f") String f,@DefaultValue("2") @QueryParam("filtro") int filtro,@DefaultValue("24") @QueryParam("hours") int nhours) throws JSONException, SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		if (nhours>240)
			nhours=240;
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		Connection db = getWeatherConnectionObject();
		
		Statement stmt =  (Statement) db.createStatement();
		stmt.executeQuery("SELECT "+getMysqlFields()+" FROM letture where data between now()-interval "+nhours+" hour and now() order by data desc");
		ResultSet rs = stmt.getResultSet ();
		JSONArray jsonMainArr = new JSONArray();
		rs.afterLast();
		while (rs.previous ()){
			JSONObject jsonObject = new JSONObject();
			getFilteredMysqlData(jsonObject,rs,filtro);
			jsonMainArr.put(jsonObject);
		}
		db.close();
		return "var sales = "+jsonMainArr.toString()+";\n"+f+"(sales);";
	}
	
	private void getMysqlData(JSONObject jsonObject,ResultSet rs) throws JSONException, SQLException {
		jsonObject.put("id", rs.getString("id"));
		jsonObject.put("data", rs.getString("data"));
		jsonObject.put("umidita_interna", rs.getString("umidita_interna"));
		jsonObject.put("temperatura_interna", rs.getString("temperatura_interna"));
		jsonObject.put("umidita_esterna", rs.getString("umidita_esterna"));
		jsonObject.put("temperatura_esterna", rs.getString("temperatura_esterna"));
		jsonObject.put("pressione", rs.getString("pressione"));
		jsonObject.put("vento_medio", rs.getString("vento_medio"));
		jsonObject.put("vento_raffica", rs.getString("vento_raffica"));
		jsonObject.put("vento_direzione", rs.getString("vento_direzione"));
		jsonObject.put("pioggia_totale", rs.getString("pioggia_totale"));
		jsonObject.put("pioggia_parziale", rs.getString("pioggia_parziale"));
	}
	private void getFilteredMysqlData(JSONObject jsonObject,ResultSet rs,int filtro) throws JSONException, SQLException {
		for (Parameters p : Parameters.values()) {
			if ((filtro&p.getCoefficiente())>0 || filtro==0)
				jsonObject.put(p.getNomecampo(),rs.getString(p.getNomecampo()));
		}
	}
	
	private String getMysqlFields() {
		String output="";
		String virgola="";
		for (Parameters p : Parameters.values()) {
			output+=virgola+p.getMysqlSelectStatement();
			virgola=",";
		}
		return output;
	}
}
