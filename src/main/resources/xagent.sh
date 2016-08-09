#!/bin/sh

export PATH=$PATH:$JAVA_HOME/bin:
CLASSPATH=.:$JAVA_HOME/lib/tools.jar
PATH=$JAVA_HOME/bin:$PATH
export JAVA_HOME CLASSPATH PATH


cd ./lib
Lib_name=`ls *.jar *.properties 2>/dev/null`
for i in ${Lib_name};do
        CLASSPATH=$CLASSPATH:./lib/${i}
done

export CLASSPATH

java com.ebay.gpf.paas.FaultInject.util.RSACoder $*

