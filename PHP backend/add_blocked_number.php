<?php
header('Content-Type: application/json');
error_reporting(0);
ini_set('display_errors', 0);


include 'db_connection.php';

$token = $_POST['token'] ?? '';
$phone_number = $_POST['phone_number'] ?? '';
$block_calls = isset($_POST['block_calls']) ? intval($_POST['block_calls']) : 0;
$block_sms = isset($_POST['block_sms']) ? intval($_POST['block_sms']) : 0;

if (!$block_calls && !$block_sms) {
    echo json_encode(['success' => false, 'message' => 'Must block either call or SMS']);
    exit;
}

if (empty($token) || empty($phone_number)) {
    echo json_encode(['success' => false, 'message' => 'Missing token or phone number']);
    exit;
}


$stmt = $conn->prepare("SELECT id FROM users WHERE token = ?");
$stmt->bind_param("s", $token);
$stmt->execute();
$result = $stmt->get_result();

if ($row = $result->fetch_assoc()) {
    $user_id = $row['id'];

    $insert = $conn->prepare("INSERT INTO blocked_numbers (user_id, phone_number, block_calls, block_sms) VALUES (?, ?, ?, ?)");
    $insert->bind_param("isii", $user_id, $phone_number, $block_calls, $block_sms);

    if ($insert->execute()) {
        echo json_encode(['success' => true]);
    } else {
        echo json_encode(['success' => false, 'message' => 'Failed to save number']);
    }
} else {
    echo json_encode(['success' => false, 'message' => 'Invalid token']);
}
?>
