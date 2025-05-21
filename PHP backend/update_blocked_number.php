<?php
include("db_connection.php");

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    $token = $_POST["token"];
    $phone_number = $_POST["phone_number"];
    $block_calls = $_POST["block_calls"];
    $block_sms = $_POST["block_sms"];

    
    $stmt = $conn->prepare("SELECT id FROM users WHERE token = ?");
    $stmt->bind_param("s", $token);
    $stmt->execute();
    $stmt->bind_result($user_id);
    if ($stmt->fetch()) {
        $stmt->close();

        
        $update = $conn->prepare("UPDATE blocked_numbers SET block_calls = ?, block_sms = ? WHERE user_id = ? AND phone_number = ?");
        $update->bind_param("iiis", $block_calls, $block_sms, $user_id, $phone_number);
        if ($update->execute()) {
            echo json_encode(["success" => true]);
        } else {
            echo json_encode(["success" => false, "message" => "Update failed"]);
        }
        $update->close();
    } else {
        echo json_encode(["success" => false, "message" => "Invalid token"]);
    }

    $conn->close();
}
?>
