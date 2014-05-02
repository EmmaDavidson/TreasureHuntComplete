/* The purpose of this php method is to check whether or not an email address
submitted by a user on the mobile application when resetting their login
password is a valid email address for this application. */

<?php

	require("config.inc.php");	    

	//Grab the user associated with the submmitted email address
	try {
		$query = "SELECT Name, Password, UserId FROM user WHERE email = :Email";
	
		    $query_params = array(
		':Email' => $_POST['email']
	 	 );
	        $stmt   = $db->prepare($query);
	        $result = $stmt->execute($query_params);
	}
	catch (PDOException $ex) {
	        $response["success"] = 0;
	        $response["message"] = "Database Error1. Please Try Again!";
	        die(json_encode($response));
	}
	  
		     		
	$row = $stmt->fetch();

	
	//If the user exists
	if ($row) {
			//Grab their type of role
			$query2  = "SELECT RoleId FROM userrole WHERE userId = :UserId";
			$userIdResult = $row['UserId'];
	    		$query_params2 = array(':UserId' => $userIdResult);

	  		try {
	     				   
	        		$stmt2   = $db->prepare($query2);
	        		$result2 = $stmt2->execute($query_params2);
	    		}
	    		catch (PDOException $ex) {
	       
	        		$response["success"] = 0;
	        		$response["message"] = "Database Error2. Please Try Again!";
	        		die(json_encode($response));
	    		}
				
			$row2 = $stmt2->fetch(PDO::FETCH_ASSOC);
			
			//Check which type of role they are	
			if($row2['RoleId'] == 1)
			{
				$response["success"] = 0;
	   		        $response["message"] = "You cannot reset the password for an administrative email address on this application";
	    			echo json_encode($response);
					
	      		}
			else		
			{	
				//If they are a participant, then retrieve their security question id
				$query2  = "SELECT * FROM usersecurityquestions WHERE userId = :UserId";
					
				$userIdResult = $row['UserId'];
	    			$query_params2 = array(':UserId' => $userIdResult);

				try {
	     				   
	        			$stmt2   = $db->prepare($query2);
	        			$result2 = $stmt2->execute($query_params2);
	    			}
	    			catch (PDOException $ex) {
	       
	        			$response["success"] = 0;
	        			$response["message"] = "Database Error2. Please Try Again!";
	        			die(json_encode($response));
	    			}

				$row3 = $stmt2->fetch();

					
				if($row3)
				{	
					//Retrieve their security question 
					$query2  = "SELECT * FROM securityquestions WHERE SecurityQuestionId = :QuestionId";
						
					$questionIdResult = $row3['SecurityQuestionId'];
					
	    				$query_params2 = array(':QuestionId' => $questionIdResult);

					try {
	     				   
	        				$stmt2   = $db->prepare($query2);
	        				$result2 = $stmt2->execute($query_params2);
	    				 }
	    				catch (PDOException $ex) {
	       
	        				$response["success"] = 0;
	        				$response["message"] = "Database Error2. Please Try Again!";
	        				die(json_encode($response));
	    				}
						
					$question = $stmt2->fetch();
						
					if($question)
					{
						$response["success"] = 1;
	   		        		$response["message"] = "This email address is valid for resetting";
						$response["result"] = $row3;
						$response["securityquestion"] = $question['SecurityQuestion'];
					}	
	
				}
				else
				{
					$response["success"] = 0;
	   		        	$response["message"] = "Could not return the security question details";
					die(json_encode($response));
				}


	    			echo json_encode($response);
			}

	        }
		else
		{
			$response["success"] = 0;
	   		$response["message"] = "Cannot reset password. Email address does not exist.";
	    		echo json_encode($response);
		}
 
	?>

