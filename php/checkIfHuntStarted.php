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
			$response["result"] = $startTime;
			echo json_encode($response);
		}	
            
	    
	    else
	    {
		$response["success"] = 0;
	        $response["message"] = "Hunt has not yet been started.";
	  	echo json_encode($response);
	    }

?>