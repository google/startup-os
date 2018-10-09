// The script works on firebase server side.

const functions = require('firebase-functions');
const cors = require('cors')({ origin: true });
const http = require('request');

const FALLBACK_SERVER = 'http://35.229.74.104:7000/';

// The code resends received request to fallback server. We need the intermediate link,
// because we can't connect http resources directly from webapp, since
// webapp is served over https.
exports.get_diff_files = functions.https.onRequest((request, response) => {
  fallback(request, response, 'get_diff_files');
});

exports.get_text_diff = functions.https.onRequest((request, response) => {
  fallback(request, response, 'get_text_diff');
});

// Sends http request to fallback server
function fallback(request, response, command) {
  // Example: http://35.229.74.104:7000/get_text_diff?request=CkEKCVJF
  const fallbackUrl = FALLBACK_SERVER + command + '?request=' + request.query.request;

  // cors creates Access-Control-Allow-Origin headers to be able to send requests from localhost
  cors(request, response, () => {
    // Send http request
    http(
      fallbackUrl,
      (error, handler, binaryResponse) => {
        // Fallback server responded
        if (error) {
          response.send(error);
        } else {
          response.send(binaryResponse);
        }
      }
    );
  });
}
