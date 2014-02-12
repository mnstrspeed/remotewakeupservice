<?php

include("lib/geoipcity.inc");
include("lib/geoipregionvars.php");

if (isset($_GET['ip'])) {
    $geoip = geoip_open("lib/GeoLiteCity.dat", GEOIP_STANDARD);
    $location = geoip_record_by_addr($geoip, $_GET['ip']);

    echo $location->latitude.",".$location->longitude;
}

?>
