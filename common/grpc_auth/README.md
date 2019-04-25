# grpc_auth

This package shows sample gRPC server and client 
using an SSL-encrypted connection and authenticating via
[Firebase Identity Token](https://firebase.google.com/docs/auth/admin/verify-id-tokens).

## Generate certificate and key

```
openssl req -new -newkey rsa:1024 -nodes -out ca.csr -keyout ca.key -subj '/CN=localhost'
openssl x509 -trustout -signkey ca.key -days 365 -req -in ca.csr -out ca.pem
```

# Running server

Supply certificate and key files obtained at the previous step 
and Firebase project ID as command line arguments:


```
bazel run //common/grpc_auth:server -- \
          --certificate_file ca.pem \
          --key_file ca.key \
          --project_id startupos-5f279
```


# Running client

Supply the same certificate as to server, Firebase identity token
and an integer as RPC parameter as command line arguments:


```
bazel run //common/grpc_auth:client -- \
          --certificate_file ca.pem \
          --token $FIREBASE_ID_TOKEN \
          --n 0
```
