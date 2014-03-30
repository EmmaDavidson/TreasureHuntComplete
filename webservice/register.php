/* The purpose of this php method is to allow a new user to register with the mobile application. */
//http://www.mybringback.com/tutorial-series/13130/android-mysql-register-and-login-php-scripts/
//http://www.youtube.com/watch?v=tfMGAE_pNU0
//http://www.youtube.com/watch?v=fZw5P_nlkYg

<?php

	require("config.inc.php");

	    //Selecting the email address passed in to see if it already exists
	    $query = " SELECT 1 FROM user WHERE email = :Email";
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


	    //Check to see if this email address already exists
	    $row = $stmt->fetch();
	    if ($row) {

	        $response["success"] = 0;
	        $response["message"] = "This Email Address is already in use";
	        die(json_encode($response));
	    }
		
	    //Else insert into the database their new details 
	    $query = "INSERT INTO user ( name, email, password ) VALUES ( :Name, :Email, :Password ) ";
	     
	    $query_params = array(
		':Name' => $_POST['name'],
	        ':Email' => $_POST['email'],
	        ':Password' => $_POST['password']
	    );
	     
	    try {
	        $stmt   = $db->prepare($query);
	        $result = $stmt->execute($query_params);
	    }
	    catch (PDOException $ex) {

	        $response["success"] = 0;
	        $response["message"] = "Database Error2. Please Try Again!";
	        die(json_encode($response));
	    }


	    //Grab the user id of this newly added participant
	    $query = " SELECT UserId FROM user WHERE email = :Email";
	    $query_params = array(
	        ':Email' => $_POST['email']
	    );
	     	    
	    try {
	       
	        $stmt   = $db->prepare($query);
	        $result = $stmt->execute($query_params);
	    }
	    catch (PDOException $ex) {
	        
	        $response["success"] = 0;
	        $response["message"] = "Database Error3. Please Try Again!";
	        die(json_encode($response));
	    }

	   $user = $stmt->fetch();

	   //If this participant has been returned	
	  if($user)
	   {
		//Insert into the security questions table their given question and answer
	    	$query = "INSERT INTO usersecurityquestions ( userid, securityquestionid, answer ) VALUES ( :UserId, :SecurityQuestionId, :Answer )";
	     
	   	$query_params = array(
		':UserId' => $user['UserId'],
	        ':SecurityQuestionId' => $_POST['securityQuestionId'],
	        ':Answer' => $_POST['answer']
	    	);
	     
	   	 try {
	        	$stmt   = $db->prepare($query);
	        	$result = $stmt->execute($query_params);
	         }
	    	 catch (PDOException $ex) {

	        	$response["success"] = 0;
	        	$response["message"] = "Database Error4. Please Try Again!";
	        	die(json_encode($response));
	   	 }
		
		$response["result"] = $user;

		//Also save their role in the database as being a participant
		$query = "INSERT INTO userrole ( roleid, userid) VALUES ( 2, :UserId ) ";
	     
	    	$query_params = array(
	        ':UserId' => $user['UserId']
	 	);
	     
	   	 try {
	       		 $stmt   = $db->prepare($query);
	       		 $result = $stmt->execute($query_params);
	             }
	   	catch (PDOException $ex) {

	       		 $response["success"] = 0;
	        	 $response["message"] = "Database Error. Please Try Again!";
	                 die(json_encode($response));
	    	}				
								
	    	$response["success"] = 1;
	    	$response["message"] = "Registration successful!";
		$response["result"] = $user;
	        echo json_encode($response);	     	     
	   }
	?>

