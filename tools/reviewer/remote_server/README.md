# Remote Server

# Workflow


## Initial Auth
1. Client authenticates with Google and gets `{code: <exchange_code>}` message
1. Client posts `{code: <exchange_code>}` message to `/gcode` endpoint
1. Server exchange code for `accessToken`/`refreshToken` pair 
1. `/gcode` endpoint responds with `accessToken`/`refreshToken` pair

Note: server has to be secured by HTTPS so code and tokens are secure in transit.

## Refreshing token
1. Client posts `refreshToken` to `/refresh` endpoint
1. Server requests new `accessToken` by providing `refreshToken` 
and `clientId`/`clientSecret` pair
1. `/refresh` endpoint responds with new `accessToken`


# Testing locally

1. Serve `index.html` in this directory locally. The easiest way to do it is using `python`:

`python2 -m SimpleHTTPServer 7000`
-or-
`python3 -m http.server 7000`

Specifying port *is* important because there’s a 
whitelist set in Google Cloud’s console.


2. Go to `localhost:7000`, open Google Chrome Dev Tools, 
authenticate with Google and watch for `{code: <>}` message 
in dev console