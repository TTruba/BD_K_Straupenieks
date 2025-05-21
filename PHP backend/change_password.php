<?php
include("db_connection.php");

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    $token = $_POST["token"];
    $current_password = $_POST["current_password"];
    $new_password = $_POST["new_password"];

    
    $stmt = $conn->prepare("SELECT id, password FROM users WHERE token = ?");
    $stmt->bind_param("s", $token);
    $stmt->execute();
    $stmt->bind_result($user_id, $stored_hashed_password);

    if ($stmt->fetch()) {
        $stmt->close();

        
        if (password_verify($current_password, $stored_hashed_password)) {
            
            $new_hashed = password_hash($new_password, PASSWORD_DEFAULT);

            
            $update = $conn->prepare("UPDATE users SET password = ? WHERE id = ?");
            $update->bind_param("si", $new_hashed, $user_id);
            if ($update->execute()) {
                echo json_encode(["success" => true, "message" => "Password updated successfully"]);
            } else {
                echo json_encode(["success" => false, "message" => "Failed to update password"]);
            }
            $update->close();
        } else {
            echo json_encode(["success" => false, "message" => "Incorrect current password"]);
        }
    } else {
        echo json_encode(["success" => false, "message" => "Invalid token"]);
    }

    $conn->close();
}
?>
