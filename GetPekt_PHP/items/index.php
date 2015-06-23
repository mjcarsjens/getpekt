<?php

if (isset($_POST['tag']) && $_POST['tag'] != '') {
	$tag = $_POST['tag'];
	if ($tag == "get"){
		if (isset($_POST['eid']) && $_POST['eid'] != '' && $_POST['uid'] && $_POST['uid'] != '') {
			$uid = $_POST['uid'];
			$eid = $_POST['eid'];
			require_once 'include/DB_Functions.php';
			$db = new DB_Functions();
			$response = array("error" => FALSE);
			$items = $db->getItemsByEID($eid);
			$counter = 0;
			foreach ($items as $item){
				$bool = TRUE;
				for ($i = 0; $i < $counter; $i++){
					if ($response[$i]["item"] == $item["item"]){
						if ($response[$i]["selected_by"] != $uid) {
							$response[$i]["item"] = $item["item"];
							$response[$i]["name"] = $item["name"];
							$response[$i]["amount"] = $item["amount"];
							$response[$i]["selected"] = $item["selected"];
							$response[$i]["selected_by"] = $item["selected_by"];
							$response[$i]["number"] = $item["number"];
						}
						$bool = FALSE;
						break;
					}
				}
				if($bool){
					$response[$counter]["item"] = $item["item"];
					$response[$counter]["name"] = $item["name"];
					$response[$counter]["amount"] = $item["amount"];
					$response[$counter]["selected"] = $item["selected"];
					$response[$counter]["selected_by"] = $item["selected_by"];
					$response[$counter]["number"] = $item["number"];
					$counter++;
				}
			}
			echo json_encode($response);
		} else {
			$response = array("error" => TRUE, "error_msg" => "whatcha doin?");
		}
	}
    else if ($tag=="create"){
   		if (isset($_POST['eid']) && $_POST['eid'] != '' && isset($_POST['name']) && $_POST['name'] != '' && isset($_POST['amount']) && $_POST['amount'] != '') {
        	$eid = $_POST['eid'];
            $name = $_POST['name'];
            $amount = $_POST['amount'];
            require_once 'include/DB_Functions.php';
            $db = new DB_Functions();
            $response = array("error" => FALSE);
            $success = $db->createEntry($eid, $name, $amount);
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
    else if ($tag=="select"){
    	if (isset($_POST['eid']) && $_POST['eid'] != '' && isset($_POST['item']) && $_POST['item'] != '' && isset($_POST['number']) && $_POST['number'] != '' && isset($_POST['uid']) && $_POST['uid'] != "")  {
        	$eid = $_POST['eid'];
            $item = $_POST['item'];
            $number = $_POST['number'];
            $uid = $_POST['uid'];
            require_once 'include/DB_Functions.php';
            $db = new DB_Functions();
			$response = array("error" => FALSE);
            $success = $db->selectEntry($eid, $item, $number, $uid);
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
	else if ($tag=="deselect"){
    	if (isset($_POST['eid']) && $_POST['eid'] != '' && isset($_POST['item']) && $_POST['item'] != "" && isset($_POST['uid']) && $_POST['uid'] != ""){
        	$eid=$_POST['eid'];
            $item=$_POST['item'];
            $uid=$_POST['uid'];
            require_once 'include/DB_Functions.php';
            $db = new DB_Functions();
            $response = array("error" => FALSE);
            $success = $db->deselectEntry($eid, $item, $uid);
            if (!$success){
             	$response["error"] = true;
            }
            echo json_encode($response);
		}
		else {
         	$response = array("error" => TRUE);
           	echo json_encode($response);
    	}
	}
   	else if ($tag=="remove"){
    	if (isset($_POST['eid']) && $_POST['eid'] != '' && isset($_POST['item']) && $_POST['item'] != ''){
         	$eid=$_POST['eid'];
            $item=$_POST['item'];
            require_once 'include/DB_Functions.php';
            $db = new DB_Functions();
            $success = $db->removeEntry($eid, $item);
            $response = array("error" => FALSE);
            if (!$success) {
             	$response["error"] = TRUE;
            }
            echo json_encode($response);
      	}
     	else {
        	$response = array("error" => TRUE);
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

