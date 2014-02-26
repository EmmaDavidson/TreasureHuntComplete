<?php

	require("config.inc.php");
	 
	if (!empty($_POST)) {
	     
	    $query  = " SELECT Password FROM hunt WHERE HuntName = :Hunt";
	
	    $query_params = array(
	        ':Hunt' => $_POST['huntname']
	    );

	   try {
	        // These two statements run the query against your database table.
	        $stmt   = $db->prepare($query);
	        $result = $stmt->execute($query_params);
	    }
	    catch (PDOException $ex) {
	        // For testing, you could use a die and message.
	        //die("Failed to run query: " . $ex->getMessage());
	        
	        $response["success"] = 0;
	        $response["message"] = "Database Error1. Please Try Again!";
	        die(json_encode($response));
	    }
	  	     		
	    $row = $stmt->fetch();
	    	if ($row) {

	    			if($_POST['password'] === $row['Password'])
				{
 					$querytwo = "INSERT INTO huntparticipants ( huntid, userid) VALUES ( :HuntId, :UserId ) ";
	     
	    				$query_params_two = array(
					':HuntId' => $_POST['huntid'],
	      		        	  ':UserId' => $_POST['userid']
	 				  );
	     
	   			 try {
	       				 $stmt   = $db->prepare($querytwo);
	       				 $result = $stmt->execute($query_params_two);
	  		                 }
	   		         catch (PDOException $ex) {

	       		         	$response["success"] = 0;
	        		 	$response["message"] = "Database Error2. Please Try Again!";
	        		 	die(json_encode($response));
	    				}				
					
					$response["success"] = 1;
	   		        	$response["message"] = "Successfully registered with the hunt!";
	    				echo json_encode($response);
					
				}
				else
				{
					$response["success"] = 0;
	   		        	$response["message"] = "Login details incorrect.";
	    				echo json_encode($response);
				}
	      
	  		  }
		else

			{
				$response["success"] = 0;
	   		        $response["message"] = "Hunt does not exist. Internal error";
	    			echo json_encode($response);
			}

?>