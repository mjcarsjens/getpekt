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

    public function getEventsByUID($uid){
        $result = mysql_query("SELECT * FROM events WHERE UID = '$uid'") or die(mysql_error());
                for($i = 0; $array[$i] = mysql_fetch_assoc($result); $i++) ;
        	array_pop($array);
                return $array;
        }

        public function createEntry($uid, $name, $icon, $time, $creator, $eid){
                $result = mysql_query("INSERT INTO events (UID, EID, E_name, E_icon, Creator, updated_at, event_at)
                                                                VALUES ('$uid', '$eid', '$name', '$icon', '$creator', NOW(), '$time');");
                if (!$result){
                        die(mysql_error());
                        return FALSE;
                }
                else{
                        return TRUE;
                }

        }
		
	public function getParticipantsByEID($eid){
		$result = mysql_query("SELECT * FROM events WHERE EID = '$eid'") or die(mysql_error());
            for($i = 0; $array[$i] = mysql_fetch_assoc($result); $i++) ;
        	array_pop($array);
            return $array;
    }
	
	public function removeEvent($eid){
                $result = mysql_query("DELETE FROM events WHERE EID = '$eid'");
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