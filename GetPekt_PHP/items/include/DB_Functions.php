<?php

class DB_Functions {

    private $db;

    function __construct() {
        require_once 'DB_Connect.php';
        $this->db = new DB_Connect();
        $this->db->connect();
    }

    function __destruct() {

    }

    public function getItemsByEID($eid){
        $result = mysql_query("SELECT * FROM items WHERE EID = '$eid'") or die(mysql_error());
        	for($i = 0; $array[$i] = mysql_fetch_assoc($result); $i++) ;
         	array_pop($array);
       	    return $array;
   	}

   	public function createEntry($eid, $name, $amount){
   		$itemID = mysql_query("SELECT * FROM items WHERE EID = '$eid'") or die(mysql_error());
        $item = mysql_num_rows($itemID) + 1;
        $result = mysql_query("INSERT INTO items (EID, item, name, amount, selected, selected_by, number) VALUES ('$eid', '$item', '$name', '$amount', '0', NULL, NULL)");
        if (!$result){
         	die(mysql_error());
            return FALSE;
      	}
       	else{
          	return TRUE;
        }
 	}
	
	public function selectEntry($eid, $item, $number, $uid){
		$temp = mysql_query("SELECT * FROM items WHERE EID = '$eid' AND item = '$item'");
		$tempFetch = mysql_fetch_assoc($temp);
		if (mysql_num_rows($temp) != 1 || $tempFetch['selected_by'] != NULL){
			$name = $tempFetch['name'];
			$amount = $tempFetch['amount'];
			$result = mysql_query("INSERT INTO items (EID, item, name, amount, selected, selected_by, number) VALUES ('$eid', '$item', '$name', '$amount', '1', '$uid', '$number')");
			if (!$result){
				die(mysql_error());
				return FALSE;
			}
			else{
				return TRUE;
			}
		}
		else {
			$result = mysql_query("UPDATE items SET selected=1, selected_by='$uid', number='$number' WHERE EID = '$eid' AND item = '$item'");
			if (!$result){
				die(mysql_error());
				return FALSE;
			}
			else{
				return TRUE;
			}
		}
	}

	public function deselectEntry($eid, $item, $uid){
    	$temp = mysql_query("SELECT * FROM items WHERE EID = '$eid' AND item='$item'");
        if (mysql_num_rows($temp) == 1) {
         	$result = mysql_query("UPDATE items SET selected=0, selected_by=NULL, number=NULL WHERE EID='$eid' AND item='$item'");
          	if (!result){
            	die(mysql_error());
             	return FALSE;
       		}
           	else {
             	return TRUE;
           	}
    	}
       	else {
          	$result = mysql_query("DELETE FROM items WHERE EID = '$eid' AND item = '$item' AND selected_by = '$uid'");
            if (!result){
              	die(mysql_error());
              	return FALSE;
       		}
           	else {
             	return TRUE;
        	}
  		}
	}

	public function removeEntry($eid, $item){
    	$result = mysql_query("DELETE FROM items WHERE EID = '$eid' AND item = '$item'");
       	if (!$result){
         	die(mysql_error());
           	return FALSE;
        }
      	else{
         	return TRUE;
       	}
	}
}

?>
