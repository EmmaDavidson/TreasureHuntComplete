<?php

	require("config.inc.php");

	try {		
		$query = "SELECT * FROM user WHERE Email = :Email";
		$query_params = array(
	        ':Email' => $_POST['email']
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

	    $userId = $stmt->fetch();

            if($userId)
	    {
		$response["success"] = 1;
	        $response["message"] = "Sucessfully returned user id!";
		$response["result"] = $userId;

		echo json_encode($response);
	    }		
	    else
	    {
		$response["success"] = 0;
	        $response["message"] = "No user id was returned.";
	  	echo json_encode($response);
	    }

?>