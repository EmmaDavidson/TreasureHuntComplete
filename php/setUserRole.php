<?php

	require("config.inc.php");
	 
	if (!empty($_POST)) 
	{
	     
 		$query = "INSERT INTO userrole ( roleid, userid) VALUES ( :RoleId, :UserId ) ";
	     
	    	$query_params = array(
		':RoleId' => $_POST['roleid'],
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
					
			$response["success"] = 1;
	   		$response["message"] = "Successfully saved userrole!";
	    		echo json_encode($response);			
	      
	}

?>