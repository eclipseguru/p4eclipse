#! /bin/bash
#
# The following command line is required to build on macosx due to a p2 bug:
#   https://bugs.eclipse.org/bugs/show_bug.cgi?id=338778
mvn clean package -Djava.vendor.url='http://www.apple.com' -P p4rcp