<?php
include 'db_connection.php';
header('Content-Type: application/json');

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    $full_name = trim($_POST["full_name"] ?? '');
    $username = trim($_POST["username"] ?? '');
    $email = trim($_POST["email"] ?? '');
    $phone = trim($_POST["phone_number"] ?? '');
    $password = password_hash($_POST["password"] ?? '', PASSWORD_BCRYPT);

    if (empty($full_name) || empty($username) || empty($email) || empty($phone) || empty($password)) {
        echo json_encode(["success" => false, "message" => "All fields are required"]);
        exit();
    }

    // Step 1: Check or insert into phone_numbers
    $stmtPhone = $conn->prepare("SELECT id FROM phone_numbers WHERE phone_number = ? LIMIT 1");
    $stmtPhone->bind_param("s", $phone);
    $stmtPhone->execute();
    $result = $stmtPhone->get_result();

    if ($row = $result->fetch_assoc()) {
        $phone_number_id = $row['id'];
    } else {
        $insertPhone = $conn->prepare("INSERT INTO phone_numbers (phone_number) VALUES (?)");
        $insertPhone->bind_param("s", $phone);
        $insertPhone->execute();
        $phone_number_id = $insertPhone->insert_id;
        $insertPhone->close();
    }
    $stmtPhone->close();

    // Step 2: Check if username/email/phone_number_id already used
    $check_user = $conn->prepare("SELECT id FROM users WHERE username = ? OR email = ? OR phone_number_id = ?");
    $check_user->bind_param("ssi", $username, $email, $phone_number_id);
    $check_user->execute();
    $check_user->store_result();

    if ($check_user->num_rows > 0) {
        echo json_encode(["success" => false, "message" => "Username, Email or Phone already exists"]);
        $check_user->close();
        exit();
    }
    $check_user->close();

    // Step 3: Insert into users
    $stmt = $conn->prepare("INSERT INTO users (full_name, username, email, phone_number_id, password) VALUES (?, ?, ?, ?, ?)");
    $stmt->bind_param("sssis", $full_name, $username, $email, $phone_number_id, $password);

    if ($stmt->execute()) {
        echo json_encode(["success" => true, "message" => "Registration successful"]);
    } else {
        echo json_encode(["success" => false, "message" => "Error registering user"]);
    }

    $stmt->close();
}

$conn->close();
