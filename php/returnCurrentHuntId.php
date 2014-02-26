<?php

	require("config.inc.php");

	try {		
		$query = "SELECT * FROM hunt WHERE HuntName = :huntname";
		$query_params = array(
	        ':huntname' => $_POST['hunt']
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

	    $huntId = $stmt->fetch();

            if($huntId)
	    {
		$response["success"] = 1;
	        $response["message"] = "Sucessfully returned hunt id!";
		$response["result"] = $huntId;

		echo json_encode($response);
	    }		
	    else
	    {
		$response["success"] = 0;
	        $response["message"] = "No hunts were returned.";
	  	echo json_encode($response);
	    }

?>