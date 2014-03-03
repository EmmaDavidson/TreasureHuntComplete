package sqlLiteDatabase;

public class Company {
	
	private String companyName;
	private int companyId; //Same as userid
	private String companyPassword;


	  public String getCompanyName() {
	    return companyName;
	  }
	  
	  public void setCompanyName(String name) {
	    this.companyName = name;
	  }
	  
	  public int getCompanyId() {
		    return companyId;
	  }
		  
	  public void setCompanyId(int id) {
		    this.companyId = id;
	  }
	  
	  public String getCompanyPassword()
	  {
		  return companyPassword;
	  }
	  
	  public void setCompanyPassword(String password)
	  {
		   this.companyPassword = password;
	  }
	  
	  
	  
}
