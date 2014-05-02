//http://www.mybringback.com/tutorial-series/13130/android-mysql-register-and-login-php-scripts/
//http://www.youtube.com/watch?v=tfMGAE_pNU0
//http://www.youtube.com/watch?v=fZw5P_nlkYg

/* The purpose of this php call is to allow a participant to log into the
mobile application with valid details. */
<?php

	    require("config.inc.php");
	 

	    //Check to see if the user exists in the database
	    $query  = " SELECT Name, Password, UserId FROM user WHERE email = :Email";
	
	    $query_params = array(
	        ':Email' => $_POST['email']
	    );

	   try {	        
	        $stmt   = $db->prepare($query);
	        $result = $stmt->execute($query_params);
	    }
	    catch (PDOException $ex) {
	        
	        $response["success"] = 0;
	        $response["message"] = "Database Error1. Please Try Again!";
	        die(json_encode($response));
	    }
	  	     		
	    $row = $stmt->fetch();
		
            //If the user exits
	    if ($row) {
		//Get the type of role for this user
		$query2  = " SELECT RoleId FROM userrole WHERE userId = :UserId";
		$userIdResult = $row['UserId'];
	    	$query_params2 = array(':UserId' => $userIdResult);

	  	 try {
	     				   
	        	$stmt2   = $db->prepare($query2);
	        	$result2 = $stmt2->execute($query_params2);
	    	     }
	    	     catch (PDOException $ex) {
	       
	        	$response["success"] = 0;
	        	$response["message"] = "Database Error1. Please Try Again!";
	        	die(json_encode($response));
	             }
				
	   	$row2 = $stmt2->fetch(PDO::FETCH_ASSOC);

		//Check to see if the user is the correct type of role
	    	if($row2['RoleId'] == 1)
	        {
			$response["success"] = 0;
	       		$response["message"] = "You cannot log into this application with an email address associated with the desktop application!";
	    		echo json_encode($response);					
	    	}
	        else		
	        {
			if($_POST['password'] === $row['Password'])
			{
				$response["success"] = 1;
	   			$response["message"] = "Successfully logged in!";
				$response["results"] = $row;
	    			echo json_encode($response);
			}
			else
			{
				$response["success"] = 0;
	   			$response["message"] = "Login details were incorrect.";
	    			echo json_encode($response);
			}		
	     	}

	  }
	 else
	  {
		$response["success"] = 0;
	   	$response["message"] = "The submitted user details do not exist in our database records!";
	    	echo json_encode($response);
	  }
	    	  
	?>

