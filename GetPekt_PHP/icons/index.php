<?php

if (isset($_POST['tag']) && $_POST['tag'] != '') {
	$tag = $_POST['tag'];
	if ($tag == "get"){
		if (isset($_POST['eid']) && $_POST['eid'] != '') {
			$eid = $_POST['eid'];
			require_once 'include/DB_Functions.php';
			$db = new DB_Functions();

			$response = array("error" => FALSE);

			$blob = $db->getBLOBbyEid($eid);
			$response['blob'] = $blob;
			echo json_encode($response);
			} else {
				$response = array("error" => TRUE, "error_msg" => "whatcha doin?");
			}
	}
	else if ($tag=="create"){
		if (isset($_POST['eid']) && $_POST['eid'] != '' && isset($_POST['blob']) && $_POST['blob'] != '') {
			$eid = $_POST['eid'];
			$blob = $_POST['blob'];
			require_once 'include/DB_Functions.php';
			$db = new DB_Functions();

			$response = array("error" => FALSE);
			$success = $db->createEntry($eid, $blob);

			if (!$success) {
					$response["error"] = TRUE;
			}
			echo json_encode($response);
		}
		else {
			$response = array("error" => TRUE, "error_msg" => "whatcha doin?");
			echo json_encode($response);
		}
	}
	else {
		$response = array("error" => TRUE, "error_msg" => "whatcha doin?");
		echo json_encode($response);
	}
}
else {
	$response= array("error" => TRUE, "error_msg" => "whatcha doin?");
	echo json_encode($response);
}

?>