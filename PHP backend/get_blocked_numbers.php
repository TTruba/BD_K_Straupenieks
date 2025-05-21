<?php
header('Content-Type: application/json');
include 'db_connection.php';

$token = $_POST['token'] ?? '';

$stmt = $conn->prepare("SELECT id FROM users WHERE token = ?");
$stmt->bind_param("s", $token);
$stmt->execute();
$result = $stmt->get_result();

if ($row = $result->fetch_assoc()) {
    $user_id = $row['id'];

    $query = $conn->prepare("SELECT id, phone_number, block_calls, block_sms FROM blocked_numbers WHERE user_id = ?");
    $query->bind_param("i", $user_id);
    $query->execute();
    $result = $query->get_result();

    $blocked = [];

    while ($r = $result->fetch_assoc()) {
        $blocked[] = $r;
    }

    echo json_encode(['success' => true, 'blocked_numbers' => $blocked]);
} else {
    echo json_encode(['success' => false, 'message' => 'Invalid token']);
}
?>
