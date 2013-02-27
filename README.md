OrderedDouble
=============

This project shows serialization of IEEE Double Precision Floating Point numbers 
into a byte array such that sorting the resulting byte array in most significant 
byte order is equivalent to sorting the double value by floating point comparison order.

Bytes are compared using unsigned byte comparison. When comparing byte arrays, 
ties are broken by length (i.e. 'aaa' < 'aaab').

It handles things like negative infinity, positive infinity, subnormals, 
and negative/positive zero. Furthermore, NULL sorts less than 
any non-null value. 

