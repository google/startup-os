# Remote Server

# Workflow


## Initial Auth
1. Client authenticates with Google and gets `{code: <ex_code>}` message
1. Client posts `{code: <ex_code>}` message to `/gcode` endpoint
1. Server exchange code for `accessToken`/`refreshToken` pair 
1. `/gcode` endpoint responds with `accessToken`/`refreshToken` pair


## Refreshing token
1. Client posts `refreshToken` to `/refresh` endpoint
1. Server requests new `accessToken` by providing `refreshToken` 
and `clientId`/`clientSecret` pair
1. `/refresh` endpoint responds with new `accessToken`