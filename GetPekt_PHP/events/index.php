<?php

if (isset($_POST['tag']) && $_POST['tag'] != '') {
	$tag = $_POST['tag'];
	if ($tag == "get"){
		if (isset($_POST['uid']) && $_POST['uid'] != '') {
			$uid = $_POST['uid'];
			require_once 'include/DB_Functions.php';
			$db = new DB_Functions();

			$response = array("error" => FALSE);

			$events = $db->getEventsByUID($uid);
			$counter = 0;
			foreach ($events as $event){
				$response[$counter]["eid"] = $event["EID"];
				$response[$counter]["name"] = $event["E_name"];
				$response[$counter]["icon"] = $event["E_icon"];
				$response[$counter]["creator"] = $event["Creator"];
				$response[$counter]["updated"] = $event["updated_at"];
				$response[$counter]["datetime"] = $event["event_at"];
				$counter++;
			}
			echo json_encode($response);
			} else {
				$response = array("error" => TRUE, "error_msg" => "whatcha doin?");
			}
	}
	else if ($tag=="create"){
		if (isset($_POST['uid']) && $_POST['uid'] != '' && isset($_POST['name']) && $_POST['name'] != '' && isset($_POST['icon']) && $_POST['icon'] != '' && isset($_POST['event']) && $_POST['event'] != '' && isset($_POST['creator']) && $_POST['creator'] != '') {
			$uid = $_POST['uid'];
			$name = $_POST['name'];
			$icon = $_POST['icon'];
			$time = $_POST['event'];
			$creator = $_POST['creator'];
			require_once 'include/DB_Functions.php';
			$db = new DB_Functions();

			$response = array("error" => FALSE);
			$success = $db->createEntry($uid, $name, $icon, $time, $creator);

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