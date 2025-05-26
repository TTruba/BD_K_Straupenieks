<?php
header('Content-Type: application/json');


require_once 'db_connection.php';

$response = ['success' => false];

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $phone = trim($_POST['phone'] ?? '');
    $username = trim($_POST['username'] ?? '');

    if (!empty($username) && !empty($phone)) {
        $userStmt = $conn->prepare("SELECT id FROM users WHERE username = ? LIMIT 1");
        $userStmt->bind_param("s", $username);
        $userStmt->execute();
        $userResult = $userStmt->get_result();

        if ($userRow = $userResult->fetch_assoc()) {
            $user_id = $userRow['id'];
            $stmt = $conn->prepare("SELECT * FROM blocked_numbers WHERE user_id = ? AND phone_number = ? AND block_calls = 1 LIMIT 1");
            $stmt->bind_param("is", $user_id, $phone);
            $stmt->execute();
            $result = $stmt->get_result();

            $response['success'] = true;
            $response['blocked'] = $result->num_rows > 0;
        } else {
            $response['error'] = 'User not found';
        }
    } else {
        $response['error'] = 'Missing username or phone';
    }
} else {
    $response['error'] = 'Invalid request method';
}

echo json_encode($response);
