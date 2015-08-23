package com.crunchify.meteo;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.ozzyboshi.worldmap.ImageSizeDifferentException;
import com.ozzyboshi.worldmap.WorldMapDrawable;
import com.ozzyboshi.worldmap.WorldMapMaker;
import com.ozzyboshi.worldmap.awt.WorldMapAwtDraw;

@Path("/WorldMap")
public class WorldMap {
	@Path("/Image/")
    @GET
    @Produces("image/png")
	
	public Response getImage() {
		
		WorldMapDrawable<Object, Object> image = new WorldMapAwtDraw();
		
		image.setDayImageFile(new File("images/day.png"));
		image.setNightImageFile(new File("images/night.png"));
		try {
			WorldMapMaker maker = new WorldMapMaker(image, true, false);
			maker.BuildMapFromUnixTimestamp(System.currentTimeMillis()/1000L);
			
			
			//System.out.println(output);
			//ImageIO.write(output, "PNG", new File("images/awtoutput2.png"));
		}
		catch (ImageSizeDifferentException e) {
			e.printStackTrace();
		}
		
		// uncomment line below to send non-streamed
	    // return Response.ok(imageData).build();

	    // uncomment line below to send streamed
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BufferedImage output = (BufferedImage) image.getDestination();
		try {
			ImageIO.write( output, "PNG", baos );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			baos.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] imageInByte = baos.toByteArray();
		try {
			baos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return Response.ok(new ByteArrayInputStream(imageInByte)).build();
	}
}
