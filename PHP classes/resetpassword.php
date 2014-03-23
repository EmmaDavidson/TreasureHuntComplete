/* The purpose of this php method is to update the login password
for a given treasure hunt. */

<?php

	require("config.inc.php");
	     
 		$query = "UPDATE user SET Password = :Password WHERE userId = :UserId";
	     
	    	$query_params = array(
		':UserId' => $_POST['userid'],
		':Password' => $_POST['password']
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
	   		$response["message"] = "Successfully updated password!";
	    		echo json_encode($response);			
?>