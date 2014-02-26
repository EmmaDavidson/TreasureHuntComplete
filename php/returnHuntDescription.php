<?php

	require("config.inc.php");

	try {		
		$query = "SELECT HuntDescription FROM hunt WHERE HuntName = :huntname";
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

	    $huntDescription = $stmt->fetch();

            if($huntDescription )
	    {
		$response["success"] = 1;
	        $response["message"] = "Sucessfully returned hunt description!";
		$response["description"] = $huntDescription;


		echo json_encode($response);
	    }		
	    else
	    {
		$response["success"] = 0;
	        $response["message"] = "Hunt description was not returned.";
	  	echo json_encode($response);
	    }

?>