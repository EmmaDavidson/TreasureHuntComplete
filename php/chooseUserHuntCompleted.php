//http://uk1.php.net/array_push
//http://stackoverflow.com/questions/676677/how-to-add-elements-to-an-empty-array-in-php
//http://stackoverflow.com/questions/3045619/need-to-store-values-from-foreach-loop-into-array
//http://stackoverflow.com/questions/9050685/can-you-append-strings-to-variables-in-php
//http://stackoverflow.com/questions/13170230/php-combine-two-associative-arrays-into-one-array
<?php

	require("config.inc.php");

	try {	
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
		foreach($hunts as $huntIdReturned)
		{
			try {	
				$date = date('Y-m-d H:i:s'); 
				$huntIdReturned = array_shift($huntIdReturned);
				$query2 = "SELECT HuntName FROM hunt WHERE HuntId = :HuntId AND EndDate < '$date'";
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

	    		$huntName = $stmt2->fetchAll(PDO::FETCH_ASSOC);
			
			if($huntName)
			{
				$response["success"] = 1;
	        		$response["message"] = "Sucessfully returned hunts!";
				array_push($response["results"], $huntName);	
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