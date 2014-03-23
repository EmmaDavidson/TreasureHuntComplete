/* The purpose of this php method is to check whether or not a treasure hunt
has already been started by a given participant i.e. they have already 
pressed the 'Play' button on the Register With Hunt screen for a given
treasure hunt for the first time. */

<?php

	require("config.inc.php");

	try {		
		$query = "SELECT * FROM huntparticipants WHERE HuntId = :huntId AND UserId = :userId AND StartTime != 'null'";
		$query_params = array(
	        ':huntId' => $_POST['huntId'],
		':userId' => $_POST['userId'],
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

	    $startTime = $stmt->fetch();

            
		if($startTime)
		{	
			$response["success"] = 1;
	        	$response["message"] = "Hunt has already been started!";
			$response["results"] = $startTime;
			echo json_encode($response);
		}	
            
	    
	    else
	    {
		$response["success"] = 0;
	        $response["message"] = "Hunt has not yet been started.";
	  	echo json_encode($response);
	    }

?>