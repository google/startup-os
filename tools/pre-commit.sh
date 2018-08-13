#!/usr/bin/env bash

# Pre-commit hook for StartupOS
# Run it before committing to perform several quality checks
# so you won't fail early on review
# Either do it manually or by creating a symlink
# To do this, execute, from repo root
# ln -s $(pwd)/tools/pre-commit.sh $(pwd)/.git/hooks/pre-commit

rm -f /tmp/check_apache_header
rm -f /tmp/check_java_package
RESULT=0

check_apache_header () {
	HEADER="Copyright $(date +'%Y') The StartupOS Authors."
	if grep -qL "$HEADER" $1; then
		echo -n; # Found header, things are OK
	else
		echo "$(tput setaf 1) License header NOT found in $1 $(tput sgr0)";
		touch /tmp/check_apache_header
	fi
}

check_java_package () {
	PACKAGE="package\ com\.google\.startupos.*;"
	if grep -q "$PACKAGE" $1; then
		echo -n; # Correct package, things are OK
	else
		echo "$(tput setaf 1) Package is NOT correct in $1 $(tput sgr0)";
		touch /tmp/check_java_package
	fi
}

export -f check_apache_header check_java_package

find . -name '*.java' -exec /usr/bin/env bash -c 'check_apache_header "$0"' {} \;
find . -name '*.java' -exec /usr/bin/env bash -c 'check_java_package "$0"' {} \;
echo


if [[ -f /tmp/check_apache_header ]]; then
	echo "$(tput setaf 1)[!] Fix license header problems$(tput sgr0)";
	RESULT=1
fi

if [[ -f /tmp/check_java_package ]]; then
	echo "$(tput setaf 1)[!] Fix java package problems$(tput sgr0)";
	RESULT=2
fi

exit $RESULT
