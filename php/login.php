//http://www.mybringback.com/tutorial-series/13130/android-mysql-register-and-login-php-scripts/
//http://www.youtube.com/watch?v=tfMGAE_pNU0
//http://www.youtube.com/watch?v=fZw5P_nlkYg
<?php

	require("config.inc.php");
	 
	if (!empty($_POST)) {

	    if (empty($_POST['email']) || empty($_POST['password'])) {
	         
	        $response["success"] = 0;
	        $response["message"] = "Please Enter Both an Email Address and Password.";

	        die(json_encode($response));
	    }

	     
	    $query  = " SELECT Name, Password, UserId FROM user WHERE email = :Email";
	
	    $query_params = array(
	        ':Email' => $_POST['email']
	    );

	   try {
	        // These two statements run the query against your database table.
	        $stmt   = $db->prepare($query);
	        $result = $stmt->execute($query_params);
	    }
	    catch (PDOException $ex) {
	        // For testing, you could use a die and message.
	        //die("Failed to run query: " . $ex->getMessage());
	        
	        $response["success"] = 0;
	        $response["message"] = "Database Error1. Please Try Again!";
	        die(json_encode($response));
	    }
	  	     		
	    	$row = $stmt->fetch();

	    	if ($row) {

				$query2  = " SELECT RoleId FROM userrole WHERE userId = :UserId";
				$userIdResult = $row['UserId'];
	    			$query_params2 = array(':UserId' => $userIdResult);

	  			 try {
	     				   
	        			$stmt2   = $db->prepare($query2);
	        			$result2 = $stmt2->execute($query_params2);
	    				}
	    			catch (PDOException $ex) {
	       
	        			$response["success"] = 0;
	        			$response["message"] = "Database Error1. Please Try Again!";
	        			die(json_encode($response));
	    				}
				
				$row2 = $stmt2->fetch(PDO::FETCH_ASSOC);

				if($row2['RoleId'] == 1)
				{
					$response["success"] = 0;
	   		        	$response["message"] = "You cannot log into this application with this email address";
	    				echo json_encode($response);
					
	      			}
				else		
				{

					if($_POST['password'] === $row['Password'])
					{
						$response["success"] = 1;
	   		        		$response["message"] = "Successfully logged in!";
	    					echo json_encode($response);
					}
					else
					{
						$response["success"] = 0;
	   		        		$response["message"] = "Login details incorrect.";
	    					echo json_encode($response);
					}		
				}

	  		  }
		else
			{
				$response["success"] = 0;
	   		        $response["message"] = "User does not exist.";
	    			echo json_encode($response);
			}
	     
	     
	    //for a php webservice you could do a simple redirect and die.
	    //header("Location: login.php");
	    //die("Redirecting to login.php");
	     
	     
	} else {
	?>
	    <h1>Login</h1>
	    <form action="login.php" method="post">
	        
		Email:<br />
	        <input type="text" name="email" value="" />
	        <br /><br />
	        Password:<br />
	        <input type="password" name="password" value="" />
	        <br /><br />
	        <input type="submit" value="Login" />
	    </form>
	    <?php
	}

	 
	?>

