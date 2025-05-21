<?php
$host = "fdb1030.awardspace.net";
$dbname = "4632858_asca";
$user = "4632858_asca";
$pass = "";

$conn = new mysqli($host, $user, $pass, $dbname);

if ($conn->connect_error) {
    die(json_encode(["success" => false, "message" => "Database connection failed"]));
}
?>
