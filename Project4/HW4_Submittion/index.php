<?php
// make sure browsers see this page as utf-8 encoded HTML
header('Content-Type: text/html; charset=utf-8');

$limit = 10;
$query = isset($_REQUEST['q']) ? $_REQUEST['q'] : false;
$results = false;
$rankAlgorithm = isset($_GET['rankAlgorithm']) ? $_GET['rankAlgorithm'] : false;

$defaultParametersWithSolr = array(
  'fl' => 'title,og_url, og_description, description, id'
);
$rankedParametersWithPageRank = array(
  'fl' => 'title,og_url, og_description, description, id',
  'sort' => 'pageRankFile desc'
);

if ($query)
{
  // The Apache Solr Client library should be on the include path
  // which is usually most easily accomplished by placing in the
  // same directory as this script ( . or current directory is a default
  // php include path entry in the php.ini)
  require_once('/Users/weifei/Dropbox/USC/2018Fall/CSCI572/HWs/HW4/solr-php-client/Apache/Solr/Service.php');

  // create a new solr service instance - host, port, and webapp
  // path (all defaults in this example)
  $solr = new Apache_Solr_Service('localhost', 8983, '/solr/myexample/');

  // if magic quotes is enabled then stripslashes will be needed
  if (get_magic_quotes_gpc() == 1)
  {
    $query = stripslashes($query);
  }

  // in production code you'll always want to use a try /catch for any
  // possible exceptions emitted  by searching (i.e. connection
  // problems or a query parsing error)
  try
  {
    if ($rankAlgorithm == "solr") {
      $results = $solr->search($query, 0, $limit, $defaultParametersWithSolr);
    }
    else {
      $results = $solr->search($query, 0, $limit, $rankedParametersWithPageRank);
    }
  }
  catch (Exception $e)
  {
    // in production you'd probably log or email this error to an admin
    // and then show a special message to the user but for this example
    // we're going to show the full exception
    die("<html><head><title>SEARCH EXCEPTION</title><body><pre>{$e->__toString()}</pre></body></html>");
  }
}
?>

<html>
  <head>
    <title>WeiFei_CSCI572_HW4</title>
  </head>
  <body>
    <div>
      <form  accept-charset="utf-8" method="get">
        <label for="q" style="font-size:30px;font-weight:600">Comparing Search Engine Ranking Algorithms</label>
        <br/><br/>
        <div>
          Search Query: <input id="q" name="q" type="text" value="<?php echo htmlspecialchars($query, ENT_QUOTES, 'utf-8'); ?>"/>
          <br>
          <div>
            Ranking Method:
            <input id="radio1" name="rankAlgorithm" type="radio" value="solr" <?php if($rankAlgorithm != "pagerank") { echo "checked='checked'"; } ?>> Solr Lucene
            <input id="radio2" name="rankAlgorithm" type="radio"  value="pagerank" <?php if($rankAlgorithm == "pagerank") { echo "checked='checked'"; } ?>> Page Rank
            <span><button type="submit" style="background:#00b300;height:30px;weight:100px">SUBMIT</button></span>
          </div>
        </div>
      </form>
      <p style="font-weight:600">Search Results:</p>
    </div>

<?php
// display results
if ($results)
{
  $total = (int) $results->response->numFound;
  $start = min(1, $total);
  $end = min($limit, $total);
?>
  <div>Results <?php echo $start; ?> - <?php echo $end;?> of <?php echo $total; ?>:</div>
  <ol>
<?php
  $idUrlMapping = array_map('str_getcsv', file('/Users/weifei/Dropbox/USC/2018Fall/CSCI572/HWs/HW4/src/URLtoHTML_nypost.csv'));

  // iterate result documents
  foreach ($results->response->docs as $doc)
  {
    $title = $doc->title;
    $id = $doc->id;
    $key = str_replace("/Users/weifei/Dropbox/USC/2018Fall/CSCI572/HWs/HW4/data/nypost/", "", $id);
    $description = $doc->og_description;

    if (isset($doc->og_url)) {
        $url = $doc->og_url;
    }
    else {
      foreach($idUrlMapping as $row)
      {
        if($row[0] == $key)
        {
          $url = $row[1];
          break;
        }
      }
    }
    ?>

    <li>
      <b>Title:
        <a href="<?php echo $url ?>">
          <?php
            if (isset($doc->title)) {
              echo htmlspecialchars($doc->title, ENT_NOQUOTES, 'utf-8');
            }
            else {
              echo "N/A";
            }
          ?>
        </a>
      </b>
      <br/>

      <b>Url: </b>
      <a href="<?php echo $url ?>"><?php echo $url ?>
      </a>
      <br/>

      <b>ID: </b>
      <?php echo $key ?>
      <br>

      <b>Description: </b>
      <?php
        if (isset($doc->og_description)) {
          echo htmlspecialchars($doc->og_description, ENT_NOQUOTES, 'utf-8');
        }
        else if (isset($doc->description)) {
          echo htmlspecialchars($doc->description, ENT_NOQUOTES, 'utf-8');
        }
        else{
          echo "N/A";
        }
      ?>
    </li>
    <br/>
<?php
  }
?>
  </ol>
<?php
}
?>
  </body>
</html>
