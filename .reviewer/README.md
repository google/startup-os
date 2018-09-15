# Reviewer config
This config contains a global registry of Reviewer instances, as well as configuration for this
specific instance.

# Global Registry
A list of Reviewer instances. List is edited as prototxt and then stored as protobin to avoid
breaking older versions. A test ensures the prototxt and protobin match and prints out the updated
protobin upon change.

# Config for this instance
The config includes:
* Firestore config
* List of repos and their ids
* List of users (TBD)
