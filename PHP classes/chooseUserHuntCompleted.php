/* The purpose of this php method is to return all treasure hunts associated with a given participant that are no longer available to play.*/

//http://uk1.php.net/array_push
//http://stackoverflow.com/questions/676677/how-to-add-elements-to-an-empty-array-in-php
//http://stackoverflow.com/questions/3045619/need-to-store-values-from-foreach-loop-into-array
//http://stackoverflow.com/questions/9050685/can-you-append-strings-to-variables-in-php
//http://stackoverflow.com/questions/13170230/php-combine-two-associative-arrays-into-one-array
<?php

	require("config.inc.php");

	try {	
		//Grab all treasure hunt ids associated with the given participant 
		$query = "SELECT HuntId FROM huntparticipants WHERE UserId = :UserId AND StartTime != '0'";
		$query_params = array(
	        ':UserId' => $_POST['userId']
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

	    $hunts = $stmt->fetchAll();

            if($hunts)
	    {
		$response["huntIds"] = $hunts;
		$response["results"] = array();
		//For each hunt id returned
		foreach($hunts as $huntIdReturned)
		{
		    try {	
				//Grab that hunt and only return it as part of a list if it is no longer available to play
				$date = date('Y-m-d H:i:s'); 
				$huntIdReturned = array_shift($huntIdReturned);
				$query2 = "SELECT * FROM hunt WHERE HuntId = :HuntId AND EndDate < '$date'";
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