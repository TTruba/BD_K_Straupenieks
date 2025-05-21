<?php
header('Content-Type: application/json'); // ✅ Ensure JSON output
require 'db_connection.php';

// Basic error reporting (for debugging, disable in production)
mysqli_report(MYSQLI_REPORT_ERROR | MYSQLI_REPORT_STRICT);

try {
    if (!isset($_POST['phone'])) {
        echo json_encode(["success" => false, "message" => "Missing phone number"]);
        exit;
    }

    $phone = $_POST['phone'];

    $stmt = $conn->prepare("SELECT id FROM scam_numbers WHERE phone_number = ? LIMIT 1");
    $stmt->bind_param("s", $phone);
    $stmt->execute();
    $stmt->store_result();

    if ($stmt->num_rows > 0) {
        echo json_encode(["success" => true, "scam" => true]);
    } else {
        echo json_encode(["success" => true, "scam" => false]);
    }

    $stmt->close();
    $conn->close();
} catch (Exception $e) {
    // ✅ Always return valid JSON, even on error
    echo json_encode([
        "success" => false,
        "error" => "Server error",
        "details" => $e->getMessage()
    ]);
}
?>
