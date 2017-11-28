function getMoments() {
  $.get("GetMoments?id=57c9cc91e4b0330271ec0367&days=2", formatCloud, 'json')
  .fail(function(err) {
    console.log('an error occurred getting moments:', err);
    $("#cloud").html(err.responseText);
  }).
  always(function() {
    //console.log('done');
  });
}

function formatCloud(data) {
  //console.log('in formatCloud:', data);
  var cloudData = [];
  for (var i = 0; i < data.moments.length; i++) {
    var entry = {};
    entry.text = data.moments[i].label;
    entry.weight = data.moments[i].score;
    entry.html = {'class':'cloudEntry','onclick':"goToWS('" + data.spaceId + "','" + data.moments[i].keyMessageId + "');"};
    cloudData.push(entry);
  }
  //console.log('cloud data:', cloudData);
  $('#cloud').jQCloud(cloudData);
}

function goToWS(spaceId, messageId) {
	window.open('https://workspace.ibm.com/space/' + spaceId + '#message=' + messageId);
}