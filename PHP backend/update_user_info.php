<?php
ini_set('display_errors', 1); // for development
error_reporting(E_ALL);
header('Content-Type: application/json');

require 'db_connection.php';

$response = [];

$token = trim($_POST['token'] ?? '');
$full_name = trim($_POST['full_name'] ?? '');
$email = trim($_POST['email'] ?? '');
$phone_number = trim($_POST['phone_number'] ?? '');

if (empty($token) || empty($full_name) || empty($email)) {
    $response = ['success' => false, 'message' => 'Missing fields'];
    echo json_encode($response);
    exit;
}

$stmt = $conn->prepare("SELECT id FROM users WHERE token = ?");
$stmt->bind_param("s", $token);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    $response = ['success' => false, 'message' => 'Invalid token'];
    echo json_encode($response);
    exit;
}

$user = $result->fetch_assoc();
$user_id = $user['id'];

$stmt = $conn->prepare("UPDATE users SET full_name = ?, email = ?, phone_number = ? WHERE id = ?");
$stmt->bind_param("sssi", $full_name, $email, $phone_number, $user_id);

if ($stmt->execute()) {
    $response = ['success' => true, 'message' => 'User updated'];
} else {
    $response = ['success' => false, 'message' => 'Update failed'];
}

echo json_encode($response);
?>
