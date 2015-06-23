<?php

if (isset($_POST['tag']) && $_POST['tag'] != '') {
        $tag = $_POST['tag'];
        if ($tag == "get"){
                if (isset($_POST['phone']) && $_POST['phone'] != '') {
                        $phone = $_POST['phone'];
                        require_once 'include/DB_Functions.php';
                        $db = new DB_Functions();

                        $response = array("error" => TRUE);

                        $uid = $db->getUIDbyPhone($phone);

                                                if ($uid != NULL){
                                                                $response["error"] = FALSE;
                                                                $response["uid"] = $uid;
                                                }
                        echo json_encode($response);
				} else if (isset($_POST['uid']) && $_POST['uid'] != ''{
					$uid = $_POST['uid'];	
				}
                } else {
                        $response = array("error" => TRUE);
						require_once 'include/DB_Functions.php';
                        $db = new DB_Functions();

                        $response = array("error" => TRUE);

                        $phone = $db->getPhoneByUID($uid);

                                                if ($phone != NULL){
                                                                $response["error"] = FALSE;
                                                                $response["phone"] = $phone;
                                                }
                        echo json_encode($response);
                }
        }
        else if ($tag=="create"){
                if (isset($_POST['uid']) && $_POST['uid'] != '' && isset($_POST['country']) && $_POST['country'] != '' && isset($_POST['phone']) && $_POST['phone'] != '') {
                                            $uid = $_POST['uid'];
                                                $country = $_POST['country'];
                                                $phone = $_POST['phone'];
                        require_once 'include/DB_Functions.php';
                        $db = new DB_Functions();

                            $response = array("error" => FALSE);
                                                $success = $db->createEntry($uid, $country, $phone);

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
        else if ($tag=="check"){
                if (isset($_POST['uid']) && $_POST['uid'] != ''){
                        $uid = $_POST['uid'];
                        $response = array("error" => FALSE);
                        require_once 'include/DB_Functions.php';
                        $db = new DB_Functions();
                        $result = $db->checkUID($uid);
                        if ($result == NULL){
                                $response["error"] = TRUE;
                        }
						else {
							$response["result"] = $result;
						}
                        echo json_encode($response);
                }
                else {
                        $response = array("error" => TRUE);
                        echo json_encode($response);
                }
		}
		
        else {
                $response = array("error" => TRUE);
                echo json_encode($response);
        }
}
else {
        $response= array("error" => TRUE);
        echo json_encode($response);
}

?>