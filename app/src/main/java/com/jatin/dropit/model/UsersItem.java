package com.jatin.dropit.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UsersItem{

	@SerializedName("image")
	private String image;

	@SerializedName("name")
	private String name;

	@SerializedName("items")
	private List<String> items;

	public void setImage(String image){
		this.image = image;
	}

	public String getImage(){
		return image;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getName(){
		return name;
	}

	public void setItems(List<String> items){
		this.items = items;
	}

	public List<String> getItems(){
		return items;
	}
}