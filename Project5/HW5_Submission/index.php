<?php
header('Content-Type: text/html; charset=utf-8');
include 'SpellCorrector.php';
include 'simple_html_dom.php';

$hasOutput=false;
$output = "";
$results = false;

if (isset($_REQUEST['q'])) {
  $query = $_REQUEST['q'];
}
else {
  $query = false;
}

$original = "";
$suggestion = "";
if ($query) {
  require_once('/Users/weifei/Dropbox/USC/2018Fall/CSCI572/HWs/HW4/solr-php-client/Apache/Solr/Service.php');
  $solr = new Apache_Solr_Service('localhost', 8983, '/solr/myexample/');
  
  if (isset($_REQUEST['sort'])) {
    $choice = $_REQUEST['sort'];
  }
  else {
    $choice = "default";
  }

  if (get_magic_quotes_gpc() == 1) {
    $query = stripslashes($query);
  }

  try {
    if($choice == "default") {
      $rahnkingAlgorithm = array('sort' => '');
    }
    else {
      $rahnkingAlgorithm = array('sort' => 'pageRankFile desc');
    }

    $word = explode(" ", $query);
    for($i = 0; $i < sizeOf($word); $i++) {
      $spellCorrection = SpellCorrector::correct($word[$i]);
      if($original == "") {
        $original = trim($spellCorrection);
      }
      else {
        $original = $original."+".trim($spellCorrection);
      }
      $suggestion = $suggestion." ".trim($spellCorrection);
    }

    $suggestion = str_replace("+", " ", $original);
    $hasOutput = false;

    if(strtolower($query) == strtolower($suggestion)) {
      $results = $solr->search($query, 0, 10, $rahnkingAlgorithm);
    }
    else {
      $hasOutput = true;
      $url = "http://localhost:8888/index.php?q=$original&sort=$choice";
      $output = "Did you mean: <a href='$url'>$suggestion</a>";
      $results = $solr->search($query, 0, 10, $rahnkingAlgorithm);
    }
  }
  catch (Exception $e) {
    die("<html><head><title>SEARCH EXCEPTION</title><body><pre>{$e->__toString()}</pre></body></html>");
  }
}

?>
<html>
  <head>
    <title>CSCI572_HW5_WeiFei</title>
    <link rel="stylesheet" href="http://code.jquery.com/ui/1.11.4/themes/smoothness/jquery-ui.css">
    <script src="http://code.jquery.com/jquery-1.10.2.js"></script>
    <script src="http://code.jquery.com/ui/1.11.4/jquery-ui.js"></script>
  </head>

  <body>
    <div>
      <form  accept-charset="utf-8" method="get">
        <label for="q" style="font-size:30px;font-weight:600">Search Engine with Spell Checking, AutoComplete and Snippets</label>
        <br/><br/>
        <div>
          Search Query: <input id="q" name="q" type="text" value="<?php echo htmlspecialchars($query, ENT_QUOTES, 'utf-8'); ?>"/>
          <br/><br/>
          <div>
            Ranking Method:
            <input id="radio1" type="radio" name='sort' value="default"
              <?php 
                if ($sort != "pagerank") { 
                  echo 'checked = "checked"';
                } 
              ?>> Solr Lucene
            <input id="radio2" type="radio" name='sort' value="pagerank" 
              <?php 
                if (isset($_REQUEST['sort']) && $choice == "pagerank") { 
                  echo 'checked = "checked"';
                } 
              ?>> Page Rank
            <span><button type="submit" style="background:#00b300;height:30px;weight:100px">SUBMIT</button></span>
          </div>
        </div>
      </form>
    </div>

    <script>
      $(function() {
        $("#q").autocomplete({
          source : function(request, response) {
            var curWord = "", prevWords = "";
            var query = $("#q").val().toLowerCase();
            var space = query.lastIndexOf(' ');
            if(query.length - 1 > space && space != -1) {
              curWord = query.substr(space + 1);
              prevWords = query.substr(0, space);
            }
            else {
              curWord=query.substr(0);
            }

            $.ajax({
              url : "http://localhost:8983/solr/myexample/suggest?q=" + curWord + "&wt=json&indent=true",
              success : function(data) {
                var tmp = data.suggest.suggest;
                var tags = tmp[curWord]['suggestions'];
                var autoSuggestions = [];

                for(var i = 0; i < tags.length; i++){
                  if(prevWords == "") {
                    autoSuggestions.push(tags[i]['term']);
                  }
                  else {
                    autoSuggestions.push(prevWords + " " + tags[i]['term']);
                  }
                }
                response(autoSuggestions);
              },
              dataType : 'jsonp',
              jsonp : 'json.wrf'
            });
          },
        })
      });
    </script>

    <?php
    if ($hasOutput){
      echo $output;
    }
    $pre = "";

    if ($results)
    {
      $numOfResults = (int) $results->response->numFound;
    ?>

    <div>
      <b>Search results of <?php echo min(1, $numOfResults); ?> - <?php echo min(10, $numOfResults);?> from <?php echo $numOfResults; ?> Results:</b>
    </div>

    <ul>
      <?php
      $idUrlMapping = array_map('str_getcsv', file('/Users/weifei/Dropbox/USC/2018Fall/CSCI572/HWs/HW4/src/URLtoHTML_nypost.csv'));
        foreach ($results->response->docs as $doc) {
          $title = $doc->title;
          $id = $doc->id;
          $key = str_replace("/Users/weifei/Dropbox/USC/2018Fall/CSCI572/HWs/HW4/data/nypost/", "", $id);
          $description = $doc->og_description;

          if (isset($doc->og_url)) {
            $url = $doc->og_url;
          }
          else {
            foreach($idUrlMapping as $row) {
              if($row[0] == $key) {
                $url = $row[1];
                break;
              }
            }
          }
          
          $snippetText = "/";
          $words = explode(" ", $query);
          foreach($words as $item) {
            $snippetText = $snippetText."(?=.*?\b".$item.".*?)";
          }
          $snippetText = $snippetText."^.*$/i";

          $html = file_get_html($id)->plaintext;
          $sentences = explode(".", $html);
          $snippetContent = "";
          foreach ($sentences as $sentence) {
            if (preg_match($snippetText, $sentence)) {
              $snippetContent = $snippetContent.$sentence;

              if (strlen($snippetContent) > 160) {
                $index = strpos(strtolower($snippetContent), strtolower($words[0]));
                $queryLen = strlen($words[0]);
                $leftLen = min(80, $index);
                $rightLen = min(80, strlen($snippetContent) - $index);
                $snippetContent = substr($snippetContent, $index-$leftLen, $leftLen)." <b>".$words[0]."</b> ".substr($snippetContent, $index+$queryLen, $rightLen-$queryLen);
              }

              foreach ($words as $single_query) {
                $snippetContent = preg_replace("/".$single_query."/i","<b>".$single_query."</b>", $snippetContent);
              }
              break;
            }

          if($snippetContent == "") {
            $curQuery = "";

            if (count($words) > 1) {
              foreach($words as $item) {
                $curQuery = $item;
                $tempText = "/";
                $tempText = $tempText."(?=.*?\b".$item.".*?)"."^.*$/i";

                if(preg_match($tempText, $sentence)) {
                  $snippetContent = $sentence;
                  break;
                }
              }
            }

            if($snippetContent != "") {
              if (strlen($snippetContent) > 160) {
                $index = strpos(strtolower($snippetContent), strtolower($words[0]));
                $queryLen = strlen($words[0]);
                $leftLen = min(80, $index);
                $rightLen = min(80, strlen($snippetContent) - $index);
                $snippetContent = substr($snippetContent, $index - $leftLen, $leftLen)." <b>".$words[0]."</b> ".substr($snippetContent, $index + $queryLen, $rightLen - $queryLen);
              }
              
              foreach($words as $single_query) {
                $snippetContent = preg_replace("/".$single_query."/i","<b>".$single_query."</b>", $snippetContent);
              }
              break;
            }
          }
        }
      ?>

      <li>
        <b>TItle: 
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
        <a href="<?php echo $url ?>"><?php echo $url ?></a>
        <br/>

        <b>ID: </b>
        <?php echo htmlspecialchars($key, ENT_NOQUOTES, 'utf-8'); ?>
        <br/>

        <b>Snippet: </b>
        <?php
          if ($snippetContent != "") {
            echo "...".$snippetContent."...";
          }
          else {
            echo htmlspecialchars($snippetContent, ENT_NOQUOTES, 'utf-8');
          }
        ?>
      </li>
      <br/>

      <?php
        }
      ?>
    </ul>
    <?php
    }
    ?>
  </body>
</html>
