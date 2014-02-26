<?php

	require("config.inc.php");

	try {		
		$query = "SELECT HuntDescription FROM hunt WHERE HuntId = :huntid";
		$query_params = array(
	        ':huntid' => $_POST['huntId']
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

	    $huntDescription = $stmt->fetch();

            if($huntDescription)
	    {
		$response["success"] = 1;
	        $response["message"] = "Sucessfully returned hunt id!";
		$response["result"] = $huntDescription;

		echo json_encode($response);
	    }		
	    else
	    {
		$response["success"] = 0;
	        $response["message"] = "No hunt descriptions were returned.";
	  	echo json_encode($response);
	    }

?>