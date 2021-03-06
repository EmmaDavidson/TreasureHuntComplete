/* The purpose of this php method is to return a all leader board
results associated with a given treasure hunt. */
<?php

	require("config.inc.php");

	try {		
		//Grab all of the hunt participant details for a given treasure hunt
		$query = "SELECT * FROM huntparticipants WHERE HuntId = :huntId";
		$query_params = array(
	        ':huntId' => $_POST['huntid']
	   	 );

                $stmt   = $db->prepare($query);
	        $result = $stmt->execute($query_params);
	    }

	catch (PDOException $ex) 
	    {
	        
	        $response["success"] = 0;
	        $response["message"] = "Database Error. Please Try Again!";

	        die(json_encode($response));
	    }
	

	$huntResults = $stmt->fetchAll();
           
	if($huntResults)
	   {
		$response["results"] = $huntResults;
		try {		
			//Grab all of the user ids associated with a given treasure hunt
			$query3 = "SELECT UserId FROM huntparticipants WHERE HuntId = :huntId";
			$query_params3 = array(
	   	        ':huntId' => $_POST['huntid']
	   		 );

	                $stmt3   = $db->prepare($query3);
		        $result3 = $stmt3->execute($query_params3);
	    	    }

		catch (PDOException $ex) 
	  	  {
	        
	        	$response["success"] = 0;
	        	$response["message"] = "Database Error. Please Try Again!";

		        die(json_encode($response));
	          }
	
		$UserIdResults = $stmt3->fetchAll();

		if($UserIdResults)
		{
			$response["resultNames"] = array();
			//For each of these user ids
			foreach($UserIdResults as $UserIdReturned)
			{
				try {	
					//Return the associated participant and their details as part of a list of others
					$UserIdReturned = array_shift($UserIdReturned);
					$query2 = "SELECT Name FROM user WHERE UserId = :UserId ";
					$query_params2 = array(
	    			    	':UserId' => $UserIdReturned
	   			 	);			
					$stmt2   = $db->prepare($query2);
					$result2 = $stmt2->execute($query_params2);
				
	 		            }

				catch (PDOException $ex) 
		    		{
	        
	       		        	$response["success"] = 0;
	        			$response["message"] = "Database Error. Please Try Again!";
	
			        	die(json_encode($response));
	    			}

		    		$UserName = $stmt2->fetchAll(PDO::FETCH_ASSOC);
			
				if($UserName)
				{
					$response["success"] = 1;
	        			$response["message"] = "Sucessfully returned leaderboard result names!";
					array_push($response["resultNames"], $UserName);	
				}		
				else
	   			{
					$response["success"] = 0;
	       		 		$response["message"] = "No leaderboard names were returned.";
	  				echo json_encode($response);
	   			}
	   	 	 }

		}

		echo json_encode($response);	
	}
	else
	   {
		$response["success"] = 0;
	        $response["message"] = "No hunt results were returned.";
	  	echo json_encode($response);
	   }

?>