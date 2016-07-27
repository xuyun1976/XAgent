package com.ebay.platform.xagent.test.jdbc;

import java.util.List;

public interface UserManager 
{	
	void insertUser(User user);
	User getUser(String username);
	List<User> getUsers();
}