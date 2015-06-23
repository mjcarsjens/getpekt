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

    public function getUIDbyPhone($country, $phone){
        $result = mysql_query("SELECT UID FROM details WHERE country = '$country' AND phone = '$phone'") or die(mysql_error());
                return mysql_fetch_assoc($result);
        }

        public function createEntry($uid, $country, $phone){
                $result = mysql_query("INSERT INTO details (UID, country, phone) VALUES ('$uid', '$country', '$phone');");
                if (!$result){
                        die(mysql_error());
                        return false;
                }
                else{
                        return true;
                }

        }
		
		public function checkPhone($phone){
                $result = mysql_query("SELECT * FROM details WHERE phone='$phone'");
                if (!$result){
                        die(mysql_error());
                        return false;
                }
                else{
                        return true;
                }

        }

        public function checkUID($uid){
                $result = mysql_query("SELECT * FROM details WHERE UID = '$uid'") or die(mysql_error());
                return mysql_fetch_assoc($result);
        }
}

?>