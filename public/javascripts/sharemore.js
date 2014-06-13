/**
 * Created by nietaki on 12.06.14.
 */
(function(){

  console.log('sharemore.js')
  var app = angular.module('sharemore', ['angularFileUpload'])

  app.controller('BodyDropController', function($scope, $upload) {

  });
  app.controller('UploadController', function($scope, $http, $upload){
    console.log('uploader')
    $scope.uploadStarted = false

    $http.get('/getIdent').success(function(data,status){
      $scope.ident = data['ident'];
      var websocketURL = data['websocketURL'];
      $scope.downloadURL = data['downloadURL'];

      $scope.onFileSelect = function($files) {
        $scope.uploadStarted = true;
        var file = $files[0] //we want just one
        $scope.upload = $upload.upload({
          url: '/upload/' + $scope.ident,
          method: 'POST',
          file: file
        }).progress(function(evt) {
          console.log('progress: ' + evt.loaded)
        })
      }
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


