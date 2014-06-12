/**
 * Created by nietaki on 12.06.14.
 */
(function(){

  console.log('sharemore.js')
  var app = angular.module('sharemore', [])


  app.controller('UploadController', function($scope, $http){
    console.log('uploader')
    $scope.uploadStarted = false
    $http.get('/getIdent').success(function(data,status){
      var ident = data['ident']
      var websocketURL = data['websocketURL']

      $(document).ready(function () {
        $('.fileUpload').liteUploader({
          script: '/upload/ident'
        })
            .on('lu:success', function (e, response) {
              console.log('Uploaded!');
              alert('Uploaded!');
            } )
            .on('lu:before', function(e, files){$scope.uploadStarted = true; $scope.$digest(); console.log('before upload started')})
            .on('lu:cancelled', function(e){alert('cancelled')})
            .on('lu:fail', function(e,jqxhr){/*alert('failed')*/})
            .on('lu:progress', function(e, procent){
              console.log("progress " + procent.toString())
            });
      });
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


