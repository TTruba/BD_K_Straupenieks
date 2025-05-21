<?php
header('Content-Type: application/json');
require_once 'db_connection.php';

$response = ['success' => false];

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $phone = trim($_POST['phone'] ?? '');
    $picked_up = isset($_POST['picked_up']) ? intval($_POST['picked_up']) : 0;
    $denied = isset($_POST['denied']) ? intval($_POST['denied']) : 0;
    $duration = isset($_POST['duration']) ? intval($_POST['duration']) : 0;
    $source = trim($_POST['source'] ?? 'analyzed');
    $call_time = trim($_POST['call_time'] ?? date('Y-m-d H:i:s'));

    if (!empty($phone)) {
        // Step 1: Resolve phone_number_id
        $stmt = $conn->prepare("SELECT id FROM phone_numbers WHERE phone_number = ? LIMIT 1");
        $stmt->bind_param("s", $phone);
        $stmt->execute();
        $result = $stmt->get_result();

        if ($row = $result->fetch_assoc()) {
            $phone_number_id = $row['id'];
        } else {
            $insert = $conn->prepare("INSERT INTO phone_numbers (phone_number) VALUES (?)");
            $insert->bind_param("s", $phone);
            $insert->execute();
            $phone_number_id = $insert->insert_id;
            $insert->close();
        }
        $stmt->close();

        // Step 2: Get user ID if this phone belongs to a registered user
        $caller_id = null;
        $userStmt = $conn->prepare("SELECT id FROM users WHERE phone_number_id = ? LIMIT 1");
        $userStmt->bind_param("i", $phone_number_id);
        $userStmt->execute();
        $userResult = $userStmt->get_result();
        if ($userRow = $userResult->fetch_assoc()) {
            $caller_id = $userRow['id'];
        }
        $userStmt->close();

        // Step 3: Insert into call_log
        $insertLog = $conn->prepare("
            INSERT INTO call_log (phone_number_id, caller_id, call_time, picked_up, denied, duration, source)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        ");
        $insertLog->bind_param("iisiiss", $phone_number_id, $caller_id, $call_time, $picked_up, $denied, $duration, $source);
        $insertLog->execute();
        $logSaved = $insertLog->affected_rows > 0;
        $insertLog->close();

        // Step 4: Count recent calls from this number in the last hour
        $countStmt = $conn->prepare("
            SELECT COUNT(*) AS count FROM call_log 
            WHERE phone_number_id = ? AND created_at >= (NOW() - INTERVAL 1 HOUR)
        ");
        $countStmt->bind_param("i", $phone_number_id);
        $countStmt->execute();
        $countResult = $countStmt->get_result()->fetch_assoc();
        $count = $countResult['count'];
        $countStmt->close();

        // Final response
        $response['success'] = true;
        $response['log_saved'] = $logSaved;
        $response['blocked'] = $count >= 100;
        $response['call_count'] = $count;
        $response['caller_id'] = $caller_id;
        $response['phone_number_id'] = $phone_number_id;
    } else {
        $response['error'] = 'Missing phone number';
    }
} else {
    $response['error'] = 'Invalid request method';
}

echo json_encode($response);
