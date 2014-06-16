# gatling-random

Gatling is a stress tool. Development is currently focusing on HTTP support.
Current stable version of Gatling is 1.5.x.

In many cases, we need to randomly generate a large set of data. Gatling provides a data-feed option to achieve that. It supports the data reading from CSV or SQL database. 
However, we want to generate random names, addresses, or other string patterns right in memory, rather than loading from database. 

We write a customized data feed, and leverage log-synth libraries to generate meaningfully random data on the fly. 