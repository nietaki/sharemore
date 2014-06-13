/**
 * Created by nietaki on 12.06.14.
 */
(function(){

  console.log('sharemore.js')
  var app = angular.module('sharemore', ['angularFileUpload'])
  app.filter('bytes', function() {
    return function(bytes, precision) {
      if (isNaN(parseFloat(bytes)) || !isFinite(bytes)) return '-';
      if (typeof precision === 'undefined') precision = 1;
      var units = ['bytes', 'kB', 'MB', 'GB', 'TB', 'PB'],
          number = Math.floor(Math.log(bytes) / Math.log(1024));
      return (bytes / Math.pow(1024, Math.floor(number))).toFixed(precision) +  ' ' + units[number];
    }
  });
  app.controller('BodyDropController', function($scope, $upload) {

  });
  app.controller('UploadController', function($scope, $http, $upload){
    console.log('uploader');
    $scope.PRE_UPLOAD = 0;
    $scope.UPLOAD_STARTED = 1;
    $scope.UPLOAD_COMPLETE = 2;
    $scope.UPLOAD_FAILED = 3;
    $scope.uploadState = 0;
    $scope.upload
    $http.get('/getIdent').success(function(data,status){
      $scope.ident = data['ident'];
      var websocketURL = data['websocketURL'];
      $scope.downloadURL = data['downloadURL'];

      $scope.alreadyLoaded = 0;
      $scope.fileSize = 1024;
      $scope.progressStyle = {'width' : '0%'};

      updateProgress = function(loaded, fileSize) {
        $scope.alreadyLoaded = loaded;
        $scope.fileSize = fileSize;
        var procentage = (loaded/fileSize) * 100
        if (procentage > 100)
          procentage = 100
        $scope.progressStyle = {'width': procentage.toString() + '%'};
      };

      $scope.onFileSelect = function($files) {
        $scope.uploadState = $scope.UPLOAD_STARTED
        var file = $files[0];//we want just one
        $scope.upload = $upload.upload({
          url: '/upload/' + $scope.ident,
          method: 'POST',
          file: file
        }).progress(function(evt) {
          console.log(evt)
          updateProgress(evt.loaded, evt.total)
        }).success(function(data, status, headers, config) {
          $scope.uploadState = $scope.UPLOAD_COMPLETE;
        }).error(function(data, status, headers, config) {
          $scope.uploadState = $scope.UPLOAD_FAILED;
        })
      };
      /*
      // old - liteUploader
      $(document).ready(function () {
        $('.fileUpload').liteUploader({
          script: '/upload/' + $scope.ident
        })
            .on('lu:success', function (e, response) {
              console.log('Uploaded!');
              alert('Uploaded!');
            } )
            .on('lu:before', function(e, files){$scope.uploadStarted = true; $scope.$digest(); console.log('before upload started')})
            .on('lu:cancelled', function(e){alert('cancelled')})
            .on('lu:fail', function(e,jqxhr){alert('failed')})
            .on('lu:progress', function(e, procent){
              console.log("progress " + procent.toString())
            });
      });
      */
      ws = new WebSocket(websocketURL)
      ws.onmessage = function(msg){
        console.log(msg)
        //TODO actually parse the message
        //TODO not cancel if successed
        $('.fileUpload').data().liteUploader.cancelUpload()
      }
      ws.onopen = function(evt){console.log('websocket open'); ws.send(JSON.stringify({'a':'b'}))}
      //$('.fileUpload').data().liteUploader.cancelUpload()
      // /success
    }).error(function(data, status){
      alert("could not load the api")
    });

  });
})();


