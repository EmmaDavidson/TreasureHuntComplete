/* The purpose of this php method is to update the database with the current result of a particular participants results of a given treasure hunt
i.e. when the participant has scanned a QR Code for a given hunt their tally and elapsed time are updated in the database. */
<?php

	require("config.inc.php");
	     
 		$query = "UPDATE huntparticipants SET elapsedtime = :ElapsedTime, tally = tally + 1 WHERE huntparticipantid = :HuntParticipantId";
	     
	    	$query_params = array(
	 	':ElapsedTime' => $_POST['timeElapsed'],
		':HuntParticipantId' => $_POST['huntParticipantId']
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
	   		$response["message"] = "Successfully updated results!";
	    		echo json_encode($response);			

?>