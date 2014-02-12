<?php

$file = "access_log";

if (isset($_GET['since'])) {
    $since = $_GET['since'];

    $log = file_exists($file) ? file_get_contents($file) : "";
    foreach (array_reverse(explode("\n", $log)) as $message) {
        $fields = explode("\t", $message);
        if (count($fields) >= 3 && intval($fields[0]) >= intval($since)) {
            echo $message;
            exit(0);
        }
    }
}

echo "0";

?>
