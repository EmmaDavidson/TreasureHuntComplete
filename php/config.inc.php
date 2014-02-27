<?php
	$username = "virapy_emmad";
	$password = "davidsone1";
	$host = "174.136.15.132";
	$dbname = "virapy_treasurehuntdb";

//FROM = http://www.mybringback.com/tutorial-series/13016/android-mysql-php-json-part-3-working-with-a-remote-server-and-mysql/

	    $options = array(PDO::MYSQL_ATTR_INIT_COMMAND => 'SET NAMES utf8');
	      

	    try
	    {
	        $db = new PDO("mysql:host={$host};dbname={$dbname};charset=utf8", $username, $password, $options);
	    }
	    catch(PDOException $ex)
	    {
	        die("Failed to connect to the database: " . $ex->getMessage());
	    }
	      

	    $db->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
	      

	    $db->setAttribute(PDO::ATTR_DEFAULT_FETCH_MODE, PDO::FETCH_ASSOC);
	      

	    if(function_exists('get_magic_quotes_gpc') && get_magic_quotes_gpc())
	    {
	        function undo_magic_quotes_gpc(&$array)
	        {
	            foreach($array as &$value)
	            {
	                if(is_array($value))
	                {
	                    undo_magic_quotes_gpc($value);
	                }
	                else
	                {
	                    $value = stripslashes($value);
	                }
	            }
	        }
	      
	        undo_magic_quotes_gpc($_POST);
	        undo_magic_quotes_gpc($_GET);
	        undo_magic_quotes_gpc($_COOKIE);
	    }
	      
	   
	    header('Content-Type: text/html; charset=utf-8');

	    session_start();
	 
	    // Note that it is a good practice to NOT end your PHP files with a closing PHP tag.
	    // This prevents trailing newlines on the file from being included in your output,
	    // which can cause problems with redirecting users.
	

?>