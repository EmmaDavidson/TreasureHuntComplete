<?php

	require("config.inc.php");
	
 	try {
	    	$query = " SELECT 1 FROM huntparticipants WHERE userid = :UserId AND huntid = :HuntId";
	    	$query_params = array(
	        	':UserId' => $_POST['userId'],
			':HuntId' => $_POST['huntId']
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

	    if ($row) {

	        $response["success"] = 1;
	        $response["message"] = "This hunt has already been registered to";
	        die(json_encode($response));
	    }
	    else
	    {
	    	$response["success"] = 0;
	    	$response["message"] = "The user has not registered for this hunt";
	    	echo json_encode($response);	     	     
		
            }	   
	
?>
	    

