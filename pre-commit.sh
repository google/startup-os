#!/usr/bin/env bash

check_apache_header () {
	HEADER="Copyright $(date +'%Y') The StartupOS Authors."
	if grep -qL "$HEADER" $1; then
		echo -n; # Found header, things are OK
	else
		echo "$(tput setaf 1) License header NOT found in $1 $(tput sgr0)";
	fi
}

check_java_package () {
	PACKAGE="package\ com\.google\.startupos.*;"
	if grep -q "$PACKAGE" $1; then
		echo -n; # Correct package, things are OK
	else
		echo "$(tput setaf 1) Package is NOT correct in $1 $(tput sgr0)";
	fi
}

export -f check_apache_header check_java_package

find . -name '*.java' -exec /usr/bin/env bash -c 'check_apache_header "$0"' {} \;
echo
find . -name '*.java' -exec /usr/bin/env bash -c 'check_java_package "$0"' {} \;
