<?php

	require("config.inc.php");
	     
 		$query = "UPDATE huntparticipants SET starttime = :StartTime WHERE userId = :UserId AND huntId = :HuntId";
	     
	    	$query_params = array(
	 	':StartTime' => $_POST['startTime'],
		':UserId' => $_POST['userId'],
		':HuntId' => $_POST['huntId']
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
	   		$response["message"] = "Successfully updated start time!";
	    		echo json_encode($response);			

?>