const functions = require('firebase-functions');
const cors = require('cors')({ origin: true });
const http = require('request');

const FALLBACK_SERVER = 'http://35.229.74.104:7000/';

exports.get_diff_files = functions.https.onRequest((request, response) => {
  fallback(request, response, 'get_diff_files');
});

exports.get_text_diff = functions.https.onRequest((request, response) => {
  fallback(request, response, 'get_text_diff');
});

function fallback(request, response, command) {
  // Example: http://35.229.74.104:7000/get_text_diff?request=CkEKCVJF
  const fallbackUrl = FALLBACK_SERVER + command + '?request=' + request.query.request;
  cors(request, response, () => {
    http(
      fallbackUrl,
      (error, handler, binaryResponse) => {
        if (error) {
          response.send(error);
        } else {
          response.send(binaryResponse);
        }
      }
    );
  });
}
