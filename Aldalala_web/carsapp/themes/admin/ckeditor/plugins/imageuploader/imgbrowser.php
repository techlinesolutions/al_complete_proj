<?php error_reporting(0);
if(!isset($_SESSION)){
	session_start();
}	
?>

<!-- Copyright (c) 2015, Fujana Solutions - Moritz Maleck. All rights reserved. -->
<!-- For licensing, see LICENSE.md -->

<?php

// Don't remove the following two rows
$link = "http://$_SERVER[HTTP_HOST]$_SERVER[REQUEST_URI]";
$root = "http://$_SERVER[HTTP_HOST]";

// checking lang value
if(isset($_COOKIE['sy_lang'])) {
    $load_lang_code = $_COOKIE['sy_lang'];
} else {
    $load_lang_code = "en";
}

// including lang files
switch ($load_lang_code) {
    case "en":
        require(__DIR__ . '/lang/en.php');
        break;
    case "pl":
        require(__DIR__ . '/lang/pl.php');
        break;
}

// Including the plugin config file, don't delete the following row!
require(__DIR__ . '/pluginconfig.php');
// Including the functions file, don't delete the following row!
require(__DIR__ . '/function.php');
// Including the check_permission file, don't delete the following row!
require(__DIR__ . '/check_permission.php');

if ($username == "" and $password == "") {
    if(!isset($_SESSION['username'])){
        include(__DIR__ . '/new.php');
        exit;	
    }
} else {
    if(!isset($_SESSION['username'])){
        include(__DIR__ . '/loginindex.php');
        exit;	
    }
}

?>

<!DOCTYPE html>
<html lang="en"
      ondragover="toggleDropzone('show')"
      ondragleave="toggleDropzone('hide')">
<head>
    
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
    <title><?php echo $imagebrowser1; ?> :: Fujana Solutions</title>
    <meta name="author" content="Moritz Maleck">
    <link rel="icon" href="img/cd-ico-browser.ico">
    
    <link rel="stylesheet" href="styles.css">
	<link rel="stylesheet" href="dist/uploadfile.css">
	
	<style>
		.uploadText{ color: white;position: absolute;left: 50px;top: 15px; }
		.uBg { background-color: unset; }
	</style>
    
    <script src="dist/jquery.min.js"></script>
    <script src="dist/plugininfo.js"></script>
    <script src="dist/jquery.lazyload.min.js"></script>
    <script src="dist/js.cookie-2.0.3.min.js"></script>
	<script src="dist/jquery.uploadfile.min.js"></script>
    
    <script src="dist/sweetalert.min.js"></script>
    <link rel="stylesheet" type="text/css" href="dist/sweetalert.css">
    
    <script src="function.js"></script>
    
    <script> 
        // Plugin version
        var currentpluginver = "<?php echo $currentpluginver; ?>";
        // ajax request to register the plugin for better support
        $.ajax({
          method: "POST",
          url: "http://ibm.bplaced.com/imageuploader/register.php",
          data: { root: "<?php echo $root; ?>", link: "<?php echo $link; ?>", ver: ""+ currentpluginver +"" }
        })
		$(document).ready(function() {
			$("#fileuploader").uploadFile({
				url:"php/upload.php",
				method:"post",
				returnType:"json",
				fileName:"myfile",
				onSuccess:function(files,data,xhr,pd)
				{
					location.reload();
				}
			});
		});
    </script>
    
</head>
<body ontouchstart="">
    
<div id="header">
    <img onclick="Cookies.remove('qEditMode');window.close();" src="img/cd-icon-close-grey.png" class="headerIconRight iconHover">
    <img onclick="reloadImages();" src="img/cd-icon-refresh.png" class="headerIconRight iconHover">
    <img onclick="uploadImg();" src="img/cd-icon-upload-grey.png" class="headerIconCenter iconHover">
	<span onclick="uploadImg();" class="headerIconCenter iconHover uploadText">Upload A New Image</span>
    <?php if($show_settings): ?>
    <img onclick="pluginSettings();" src="img/cd-icon-settings.png" class="headerIconRight iconHover" style="display: none;">
    <?php endif; ?>
</div>
    
<div id="editbar">
    <div id="editbarView" onclick="#" class="editbarDiv"><img src="img/cd-icon-images.png" class="editbarIcon editbarIconLeft"><p class="editbarText"><?php echo $buttons1; ?></p></div>
    <a href="#" id="editbarDownload" download><div class="editbarDiv"><img src="img/cd-icon-download.png" class="editbarIcon editbarIconLeft"><p class="editbarText"><?php echo $buttons2; ?></p></div></a>
    <div id="editbarUse" onclick="#" class="editbarDiv"><img src="img/cd-icon-use.png" class="editbarIcon editbarIconLeft"><p class="editbarText"><?php echo $buttons3; ?></p></div>
    <div id="editbarDelete" onclick="#" class="editbarDiv"><img src="img/cd-icon-qtrash.png" class="editbarIcon editbarIconLeft"><p class="editbarText"><?php echo $buttons4; ?></p></div>
    <img onclick="hideEditBar();" src="img/cd-icon-close-black.png" class="editbarIcon editbarIconRight">
</div>
    
<div id="updates" class="popout"></div>
    
<div id="dropzone" class="dropzone" 
     ondragenter="return false;"
     ondragover="return false;"
     ondrop="drop(event)">
    <p>
        <img src="img/cd-icon-upload-big.png"><br>
        <?php echo $imagebrowser4; ?>
    </p>
</div>

<p class="folderInfo"><?php echo $imagebrowser2; ?> <span id="finalcount"></span> <?php echo $imagebrowser3; ?> - <span id="finalsize"></span>
    <?php if($file_style == "block") { ?>
        <img title="List" src="img/cd-icon-list.png" class="headerIcon floatRight" onclick="window.location.href = 'pluginconfig.php?file_style=list';">
    <?php } elseif($file_style == "list") { ?>
        <img title="Block" src="img/cd-icon-block.png" class="headerIcon floatRight" onclick="window.location.href = 'pluginconfig.php?file_style=block';">
        <img title="Quick Edit" id="qEditBtnOpen" src="img/cd-icon-qedit.png" class="headerIcon floatRight" onclick="toogleQEditMode();">
        <img title="Quick Edit" id="qEditBtnDone" src="img/cd-icon-done.png" class="headerIcon floatRight" onclick="toogleQEditMode();">
    <?php } ?>
</p>

<div id="files">
    <?php
    loadImages();
    ?>
</div>

<div id="imageFullSreen" class="lightbox popout">
    <div class="buttonBar">
        <button id="imageFullSreenClose" class="headerBtn" onclick="$('#imageFullSreen').hide(); $('#background').slideUp(250, 'swing');"><img src="img/cd-icon-close.png" class="headerIcon"></button>
        <a href="#" id="imgActionDownload" download><button class="headerBtn"><img src="img/cd-icon-download.png" class="headerIcon"></button></a>
        <button class="headerBtn greenBtn" id="imgActionUse" onclick="#" class="imgActionP"><img src="img/cd-icon-use.png" class="headerIcon"> <?php echo $buttons3; ?></button>
    </div><br><br>
    <img id="imageFSimg" src="#" style="#"><br>
</div>
    
<div id="uploadImgDiv" class="lightbox popout" style="top: 40%;">
    <div class="buttonBar">
        <button class="headerBtn clsbtn" onclick="$('#uploadImgDiv').hide(); $('#background2').slideUp(250, 'swing');"><b>X</b></button>
        <button class="headerBtn greenBtn" name="submit" onclick="$('#uploadImgForm').submit();" style="display: none;"><img src="img/cd-icon-upload.png" class="headerIcon"> <?php echo $buttons7; ?></button>
    </div><br><br><br>
    <form action="imgupload.php" method="post" enctype="multipart/form-data" id="uploadImgForm" onsubmit="return checkUpload();">
        <p class="uploadP"><img src="img/cd-icon-select.png" class="headerIcon"> <?php echo $uploadpanel1; ?></p>
        <input type="file" name="upload" id="upload" onchange="this.form.submit()" style="display:none;">
		<div id="fileuploader"> Upload </div>
        <br><!--<h3 class="settingsh3" style="font-size:12px;font-weight:lighter;"><?php echo $uploadpanel2; ?><br><span style="font-weight:bolder;">"<?php echo $useruploadfolder; ?>"</span> (<?php echo $uploadpanel3; ?>)</h3>-->
        <br>
    </form>
</div>

<?php if($show_settings) { ?>
    <div id="settingsDiv" class="lightbox popout">
        <div class="buttonBar">
            <button class="headerBtn" onclick="$('#settingsDiv').hide(); $('#background3').slideUp(250, 'swing');"><img src="img/cd-icon-close.png" class="headerIcon"></button>
        </div><br><br><br>

        <h3 class="settingsh3"><?php echo $panelsettings1; ?></h3>
        <!--choose existing folder text-->
        <p class="settingsh3 saveUploadPathP"><?php echo $panelsettings2; ?></p>
        <!--editable upload path-->
        <p class="uploadP editable" id="uploadpathEditable"><?php echo $useruploadfolder; ?></p>
        <!--path history-->
        <p class="settingsh3 saveUploadPathP"><?php echo $panelsettings3; ?></p>
        <?php
        pathHistory();
        ?>
        <!--cancel btn-->
        <button class="headerBtn greyBtn saveUploadPathA" id="pathCancel"><?php echo $buttons5; ?></button>
        <!--save btn-->
        <button class="headerBtn saveUploadPathA" onclick="updateImagePath();"><?php echo $buttons6; ?></button><br class="saveUploadPathA">

        <br>
        <!--change language-->
        <p class="uploadP" onclick="openLangPanel();"><img src="img/cd-icon-translate.png" class="headerIcon"> <?php echo $panelsettings20; ?></p>

        <br>
    </div>
<?php } ?>
    
<form id="donate" target="_blank" action="https://www.paypal.com/cgi-bin/webscr" method="post" target="_top">
<input type="hidden" name="cmd" value="_s-xclick">
<input type="hidden" name="hosted_button_id" value="BTEL7F2ZLR3T6">
</form> 
    
<div id="background" class="background" onclick="$('#imageFullSreenClose').trigger('click');"></div>
<div id="background2" class="background uBg" onclick="$('#uploadImgDiv').hide(); $('#background2').slideUp(250, 'swing');"></div>
<div id="background3" class="background" onclick="$('#settingsDiv').hide(); $('#background3').slideUp(250, 'swing');"></div>
<div id="background4" class="background" onclick="$('#setLangDiv').hide(); $('#background4').slideUp(250, 'swing');"></div>

<!--Noscript part if js is disabled-->
<noscript> <div class="noscript"> <div id="folderError" class="noscriptContainer popout"> <b><?php echo $alerts1; ?></b><br><br><?php echo $alerts5; ?> <a href="http://www.enable-javascript.com/" target="_blank"><?php echo $alerts6; ?></a><br><br><?php echo $alerts4; ?> </div></div></noscript>
    
<?php   
// Including the language file, don't delete the following row!
//require(__DIR__ . '/lang/lang.php');
?>

</body>
</html>