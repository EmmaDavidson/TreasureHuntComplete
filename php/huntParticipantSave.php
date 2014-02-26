<?php

	require("config.inc.php");
	 
 		$query = "INSERT INTO huntparticipants ( huntid, userid) VALUES ( :HuntId , :UserId) ";
	     
	    	$query_params = array(
	 	':HuntId' => $_POST['huntid'],
	        ':UserId' => $_POST['userid']
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
		try {		
			$query2 = "SELECT HuntParticipantId FROM huntparticipants WHERE HuntId = :huntid AND UserId = :UserId";
			$query_params2 = array(
	        	':huntid' => $_POST['huntid'],
			':UserId' => $_POST['userid']
	   	 	);

	                $stmt2   = $db->prepare($query2);
		        $result2 = $stmt2->execute($query_params2);
	   	    }

		catch (PDOException $ex) 
	    	   {
	        
	       		$response["success"] = 0;
	        	$response["message"] = "Database Error. Please Try Again!";

	       		 die(json_encode($response));
	    	    }	
			
		$huntParticipantId = $stmt2->fetch();	
					
		if($huntParticipantId)
		    { 
			$response["success"] = 1;
	        	$response["message"] = "Sucessfully returned hunt participant id and saved user for this hunt!";
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