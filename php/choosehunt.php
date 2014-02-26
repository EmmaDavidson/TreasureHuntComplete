<?php

	require("config.inc.php");

	try {		
		$date = date('Y-m-d H:i:s'); 

		$query = $db->prepare("SELECT HuntName FROM hunt WHERE EndDate > '$date'");
		$result = $query->execute();
	    }

	catch (PDOException $ex) 
	    {
	        
	        $response["success"] = 0;
	        $response["message"] = "Database Error. Please Try Again!";

	        die(json_encode($response));
	    }

	    $users = $query->fetchAll(PDO::FETCH_ASSOC);

            if($users)
	    {
		$response["success"] = 1;
	        $response["message"] = "Sucessfully returned hunts!";
		$response["results"] = $users;

		echo json_encode($response);
	    }		
	    else
	    {
		$response["success"] = 0;
	        $response["message"] = "No hunts were returned.";
	  	echo json_encode($response);
	    }

?>