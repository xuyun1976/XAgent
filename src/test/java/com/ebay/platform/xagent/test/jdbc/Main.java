package com.ebay.platform.xagent.test.jdbc;

import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class Main 
{
  public static void main( String[] args ) throws InterruptedException {

	  
    ApplicationContext ctx = 
        new ClassPathXmlApplicationContext("spring.xml");
    UserManager userManager = 
        (UserManager) ctx.getBean("userManagerImpl");
   
    /*
    User user = new User();
    user.setUsername("johndoe");
    user.setName("John Doe");
   
    userManager.insertUser(user);
   
    System.out.println("User inserted!");
   */
    
    for (int i = 0; i < 10; i++)
    {
    User user = userManager.getUser("johndoe");
   
    System.out.println("\nUser fetched!"
    + "\nId: " + user.getId()
    + "\nUsername: " + user.getUsername()
    + "\nName: " + user.getName());
    
    Thread.sleep(3000);
    }
    
//    System.exit(0);
   /*
    List<User> users = userManager.getUsers();
   
    System.out.println("\nUser list fetched!"
     + "\nUser count: " + users.size());
*/
  }
}
