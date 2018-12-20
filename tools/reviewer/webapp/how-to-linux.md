# How to install required apps on Ubuntu

## Nodejs and npm
To start off, we'll need to get the software packages from our Ubuntu repositories that will allow us to build source packages. The nvm script will leverage these tools to build the necessary components:
```
sudo apt-get update
sudo apt-get install build-essential libssl-dev
```

Download script to install Node Version Manager
```
curl -sL https://raw.githubusercontent.com/creationix/nvm/v0.33.8/install.sh -o install_nvm.sh
```

Run the script
```
bash install_nvm.sh
```

To gain access to the nvm functionality, you'll need to log out and log back in again, or you can source the `~/.profile` file so that your current session knows about the changes:
```
source ~/.profile
```

Check available versions:
```
nvm ls-remote
```

Install and use exact version.
```
nvm install 8.11.4
```

Great! `node` and `npm` are installed successfully.  

To check current used version:
```
node -v
```
```
npm -v
```

## Google protobuf
Download **latest release** from [github](https://github.com/protocolbuffers/protobuf/releases/)  
Example:
```
curl -OL https://github.com/protocolbuffers/protobuf/releases/download/v3.6.1/protoc-3.6.1-linux-x86_64.zip
```

Unzip
```
unzip protoc-3.6.1-linux-x86_64.zip -d protoc3
```

Move protoc to /usr/local/bin/
```
sudo mv protoc3/bin/* /usr/local/bin/
```

Move protoc3/include to /usr/local/include/
```
sudo mv protoc3/include/* /usr/local/include/
```

Done!
