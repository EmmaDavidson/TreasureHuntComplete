/* The purpose of this php method is to return all of the companies that
exist within the database so that a participant can select a company from a list on screen to view its treasure hunts. */

<?php

	require("config.inc.php");

	try {		
		$query = $db->prepare("SELECT * FROM companydetails");
		$result = $query->execute();
	    }

	catch (PDOException $ex) 
	    {
	        
	        $response["success"] = 0;
	        $response["message"] = "Database Error. Please Try Again!";

	        die(json_encode($response));
	    }

	    $companies = $query->fetchAll();

            if($companies)
	    {
		$response["success"] = 1;
	        $response["message"] = "Sucessfully returned companies!";
		$response["results"] = $companies;

		echo json_encode($response);
	    }		
	    else
	    {
		$response["success"] = 0;
	        $response["message"] = "No companies were returned.";
	  	echo json_encode($response);
	    }

?>