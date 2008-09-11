if [ -f "$1" ] ; then
cp $1 'v9t9/cpu/Executor$CodeBlock$'$1
FILE='v9t9/cpu/Executor$CodeBlock$'$1
java -cp .:/usr/share/java/bcel-5.1.jar org.apache.bcel.verifier.Verifier $FILE
rm -f 'v9t9/cpu/Executor$CodeBlock$'$1
fi
