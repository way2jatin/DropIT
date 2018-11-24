package com.jatin.dropit.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Data{

	@SerializedName("has_more")
	private boolean hasMore;

	@SerializedName("users")
	private List<UsersItem> users;

	public void setHasMore(boolean hasMore){
		this.hasMore = hasMore;
	}

	public boolean isHasMore(){
		return hasMore;
	}

	public void setUsers(List<UsersItem> users){
		this.users = users;
	}

	public List<UsersItem> getUsers(){
		return users;
	}
}