<?php
include("db_connection.php");

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    $token = $_POST["token"];

    $stmt = $conn->prepare("
        SELECT u.full_name, u.username, u.email, p.phone_number
        FROM users u
        LEFT JOIN phone_numbers p ON u.phone_number_id = p.id
        WHERE u.token = ?
    ");
    $stmt->bind_param("s", $token);
    $stmt->execute();
    $stmt->bind_result($full_name, $username, $email, $phone_number);

    if ($stmt->fetch()) {
        echo json_encode([
            "status" => "success",
            "full_name" => $full_name,
            "username" => $username,
            "email" => $email,
            "phone_number" => $phone_number
        ]);
    } else {
        echo json_encode(["status" => "error", "message" => "Invalid token"]);
    }

    $stmt->close();
    $conn->close();
}
