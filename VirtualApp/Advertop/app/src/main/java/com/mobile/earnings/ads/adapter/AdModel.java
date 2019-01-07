package com.mobile.earnings.ads.adapter;



public class AdModel{

	private int    id;
	private int    icon;
	private String title;
	private String description;

	public AdModel(int id, int icon, String title, String description){
		this.id = id;
		this.icon = icon;
		this.title = title;
		this.description = description;
	}

	public int getId(){
		return id;
	}

	public void setId(int id){
		this.id = id;
	}

	public String getTitle(){
		return title;
	}

	public void setTitle(String title){
		this.title = title;
	}

	public int getIcon(){
		return icon;
	}

	public void setIcon(int icon){
		this.icon = icon;
	}

	public String getDescription(){
		return description;
	}

	public void setDescription(String description){
		this.description = description;
	}
}
