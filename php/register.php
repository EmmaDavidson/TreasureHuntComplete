//http://www.mybringback.com/tutorial-series/13130/android-mysql-register-and-login-php-scripts/
//http://www.youtube.com/watch?v=tfMGAE_pNU0
//http://www.youtube.com/watch?v=fZw5P_nlkYg

<?php

	require("config.inc.php");
	 

	if (!empty($_POST)) {

	    if (empty($_POST['email']) || empty($_POST['password']) || empty($_POST['name'])) {
	        
	        $response["success"] = 0;
	        $response["message"] = "Please Enter a Name, Email Address and Password.";

	        die(json_encode($response));
	    }

	    $query = " SELECT 1 FROM user WHERE email = :Email";
	    $query_params = array(
	        ':Email' => $_POST['email']
	    );
	     	    
	    try {
	       
	        $stmt   = $db->prepare($query);
	        $result = $stmt->execute($query_params);
	    }
	    catch (PDOException $ex) {
	        
	        $response["success"] = 0;
	        $response["message"] = "Database Error1. Please Try Again!";
	        die(json_encode($response));
	    }
	     
	    $row = $stmt->fetch();
	    if ($row) {

	        $response["success"] = 0;
	        $response["message"] = "This Email Address is already in use";
	        die(json_encode($response));
	    }
	     
	    $query = "INSERT INTO user ( name, email, password ) VALUES ( :Name, :Email, :Password ) ";
	     
	    $query_params = array(
		':Name' => $_POST['name'],
	        ':Email' => $_POST['email'],
	        ':Password' => $_POST['password']
	    );
	     
	    try {
	        $stmt   = $db->prepare($query);
	        $result = $stmt->execute($query_params);
	    }
	    catch (PDOException $ex) {

	        $response["success"] = 0;
	        $response["message"] = "Database Error2. Please Try Again!";
	        die(json_encode($response));
	    }

	    $response["success"] = 1;
	    $response["message"] = "Registration successful!";
	    echo json_encode($response);	     	     
	     
	} else {
	?>
	    <h1>Register</h1>
	    <form action="register.php" method="post">
	        
		Name:<br />
		<input type="text" name="name" value="" />
	        <br /><br />
		Email:<br />
	        <input type="text" name="email" value="" />
	        <br /><br />
	        Password:<br />
	        <input type="password" name="password" value="" />
	        <br /><br />
	        <input type="submit" value="Register New User" />
	    </form>
	    <?php
	}
	 
	?>

