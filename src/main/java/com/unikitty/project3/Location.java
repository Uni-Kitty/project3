package com.unikitty.project3;

public class Location {
	private double latidude;
	private double longitude;
	private String city = null;
	private String region = null;
	private String country = null;
	private int id;
	private String name = null;
	  
	public Location () {}
	
	public Location(double lat, double lon, String cit, String reg, String countri, int id, String name) {
		latidude = lat;
		longitude = lon;
		city = cit;
		region = reg;
		country = countri;
		this.id = id;
		this.name = name;
	}
	
	public double getLatidude() {
		return latidude;
	}

	public void setLatidude(double latidude) {
		this.latidude = latidude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
