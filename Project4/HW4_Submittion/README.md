#PHP code execution guide:

Before execute php file, you need to change this parameters to your directory:
1. line 24 (solr server directory): require_once('/Users/weifei/Dropbox/USC/2018Fall/CSCI572/HWs/HW4/solr-php-client/Apache/Solr/Service.php');

2. line 92 (csv file directory):
$idUrlMapping = array_map('str_getcsv', file('/Users/weifei/Dropbox/USC/2018Fall/CSCI572/HWs/HW4/src/URLtoHTML_nypost.csv'));

3. line 99 (nypost html folder directory):
$key = str_replace("/Users/weifei/Dropbox/USC/2018Fall/CSCI572/HWs/HW4/data/nypost/", "", $id);
