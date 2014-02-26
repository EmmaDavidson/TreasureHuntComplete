<?php

	require("config.inc.php");

	try {		
		$query = "SELECT HuntParticipantId FROM huntparticipants WHERE HuntId = :huntid AND UserId = :userid";
		$query_params = array(
	        ':huntid' => $_POST['huntId'],
		':userid' => $_POST['userId'],
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

	    $huntParticipantId = $stmt->fetch();

            if($huntParticipantId)
	    {
		$response["success"] = 1;
	        $response["message"] = "Sucessfully returned hunt participant id!";
		$response["result"] = $huntParticipantId;

		echo json_encode($response);
	    }		
	    else
	    {
		$response["success"] = 0;
	        $response["message"] = "Hunt participant id was not returned.";
	  	echo json_encode($response);
	    }

?>