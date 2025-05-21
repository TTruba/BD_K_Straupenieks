<?php
include("db_connection.php");

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    $token = $_POST["token"];
    $phone_number = $_POST["phone_number"];

    $stmt = $conn->prepare("SELECT id FROM users WHERE token = ?");
    $stmt->bind_param("s", $token);
    $stmt->execute();
    $stmt->bind_result($user_id);
    if ($stmt->fetch()) {
        $stmt->close();

        
        $delete = $conn->prepare("DELETE FROM blocked_numbers WHERE user_id = ? AND phone_number = ?");
        $delete->bind_param("is", $user_id, $phone_number);
        if ($delete->execute()) {
            echo json_encode(["success" => true]);
        } else {
            echo json_encode(["success" => false, "message" => "Failed to remove number"]);
        }
        $delete->close();
    } else {
        echo json_encode(["success" => false, "message" => "Invalid token"]);
    }

    $conn->close();
}
?>
