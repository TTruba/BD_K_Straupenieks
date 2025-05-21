<?php
include 'db_connection.php';

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    $username = $_POST["username"];
    $password = $_POST["password"];

    $stmt = $conn->prepare("SELECT id, password FROM users WHERE username = ?");
    $stmt->bind_param("s", $username);
    $stmt->execute();
    $stmt->store_result();

    if ($stmt->num_rows > 0) {
        $stmt->bind_result($id, $hashedPassword);
        $stmt->fetch();

        if (password_verify($password, $hashedPassword)) {
            
            $token = bin2hex(random_bytes(32));

            
            $update_stmt = $conn->prepare("UPDATE users SET token = ? WHERE id = ?");
            $update_stmt->bind_param("si", $token, $id);
            $update_stmt->execute();
            $update_stmt->close();

            echo json_encode(["success" => true, "token" => $token, "username" => $username]);
        } else {
            echo json_encode(["success" => false, "message" => "Invalid credentials"]);
        }
    } else {
        echo json_encode(["success" => false, "message" => "User not found"]);
    }

    $stmt->close();
}
$conn->close();
?>
