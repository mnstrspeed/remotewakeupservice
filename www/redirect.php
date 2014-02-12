<?php

$redirect = "";
if (isset($_GET['g'])) {
    $redirect = 'http://goo.gl/'.$_GET['g'];
} else if (count($_GET) > 0) {
    $redirect = 'http://'.reset($_GET);
}

$file = "access_log";
$log = file_exists($file) ? file_get_contents($file) : "";

$time = time();
$host = $_SERVER['REMOTE_ADDR'];
$useragent = $_SERVER['HTTP_USER_AGENT'];

$log .= $time."\t".$host."\t".$useragent."\t".$redirect."\n";
file_put_contents($file, $log);

// Redirect
if ($redirect) {
    header("Location: ".$redirect);
} else {
    print_r($_GET);
    echo "Kk";
}

?>
