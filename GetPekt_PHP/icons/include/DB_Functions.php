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

    public function getBLOBbyEid($eid){
        $result = mysql_query("SELECT blob FROM icons WHERE eid = '$eid'") or die(mysql_error());
                return mysql_fetch_assoc($result);
        }

	public function createEntry($eid, $blob){
			$rows = mysql_query("SELECT * FROM icons WHERE eid = '$eid'") or die(mysql_error());
			if (mysql_num_rows($rows) > 0) {
				$result = mysql_query("UPDATE icons SET blob = '$blob' WHERE eid = '$eid'") or die(mysql_error());
				if ($result) {
					return TRUE;
				} else {
					return FALSE;	
				}
			} else {
				$result = mysql_query("INSERT INTO icons (eid, blob) VALUES ('$eid', '$blob');") or die(mysql_error());
				if ($result) {
					return TRUE;
				} else {
					return FALSE;	
				}	
			}
	}
}

?>