/* The purpose of this php method is to return all treasure hunts that are
associated with a given administrator to allow a participant to select one from a list on screen for a given company.*/

<?php

	require("config.inc.php");

	try {		
		//Grab all of the treasure hunt id's associated with this user
		$query = "SELECT HuntId FROM userhunt WHERE UserId = :UserId";
		$query_params = array(
	        ':UserId' => $_POST['administratorId']
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

	    $huntIds = $stmt->fetchAll();

	    //If a list of ids has been found
            if($huntIds)
	    {
		$response["huntIds"] = $huntIds;
		$response["results"] = array();

		//For each of these ids
		foreach($huntIds as $huntIdReturned)
		{
			//Grab the hunt associated with this id that is within a valid date range i.e. it is not out of date.
			try {	$date = date('Y-m-d H:i:s');
				$huntIdReturned = array_shift($huntIdReturned);
				$query2 = "SELECT * FROM hunt WHERE HuntId = :HuntId AND EndDate > '$date' ";
				$query_params2 = array(
	    			    ':HuntId' => $huntIdReturned
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

	    		$huntDetails = $stmt2->fetchAll(PDO::FETCH_ASSOC);
			
			if($huntDetails)
			{
				$response["success"] = 1;
	        		$response["message"] = "Sucessfully returned hunts!";
				array_push($response["results"], $huntDetails);	
			}
	   	 }
		echo json_encode($response);
	    }		
	    else
	    {
		$response["success"] = 0;
	        $response["message"] = "No hunts were returned.";
	  	echo json_encode($response);
	    }

?>