---
title: Metrics
layout: default
menu: true
jumbotron: true
quick-overview: Genson metrics and comparision with other librairies.
---

##Overview##

All the following measures were made with Genson 1.0, Jackson 2.4.0 and Gson 2.2.4 on a Intel core i7 laptop with 2GHz and 4 Gio of RAM.
Genson is compared to Gson and Jackson as one has nice performances when the other is a light library with nice design.
Genson tries to provide a solution that is well balanced between performances/features & ease of use.

Note that in order to make sure Genson benchmarks are not biased we also used datasets coming from the competitors (Gson dataset)
and an external benchmarking project [jvm-serializers](https://github.com/eishay/jvm-serializers).


##Benchmarking methodology

The following benchmarks were run in the same jvm. Before starting to measure performances, each library has been warmed up.
To limit memory usage impact, an explicit call to the garbage collector has been made, followed by sleep of the running thread
for some seconds between each library bench.

###Genson dataset

This benchmark uses fictive data. The goal is to measure de/ser performances with all kind of mixed objects where you have
got a lot of beans, big numbers, many lists, arrays and maps (small and big).
Note that we compare two versions of Genson, the first one uses a custom algorithm allowing to parse doubles really fast,
but with a small precision loss, the other one uses standard Double.parse. In most cases using the custom algorithm should be fine.

<img class="img-responsive" src="{{site.baseurl}}/images/genson-data-bench.png" />


###Gson dataset

Gson also has some benchmarking dataset, that they use to measure their performances. We used it too, so we can see how Genson performs
using competitors datasets.

The following charts represent serialization and deserialization average time in ms of three datasets.
We run 50000 iterations and 50 warmup iterations. The documents can be found [here](https://github.com/owlike/genson/blob/master/genson/src/test/resources/).
Serialization source code is located [here](https://github.com/owlike/genson/blob/master/genson/src/test/java/com/owlike/genson/SerializationBenchmark.java)
 and deserialization [here](https://github.com/owlike/genson/blob/master/genson/src/test/java/com/owlike/genson/DeserializeBenchmark.java).

<img class="img-responsive" src="{{site.baseurl}}/images/gson-data-deser-bench.png" />
<img class="img-responsive" src="{{site.baseurl}}/images/gson-data-ser-bench.png" />


##JVM Serializers Benchmark

This benchmark is based on the [jvm-serializers](https://github.com/eishay/jvm-serializers/wiki/) projet. The chart bellow represents
the result of running it on the [media.1](https://github.com/eishay/jvm-serializers/blob/kannan/tpc/data/media.1.cks) dataset.

The benchmark was run using the default configuration and represents the throughput of de/ser for a test time of 10000.

<img class="img-responsive" src="{{site.baseurl}}/images/jvm-serializers-bench.png" />


##Artifact size##
Last measure that can be very important for mobile developers is the jar size.
Genson weights only 170 KB more than Gson but has greater performances and a wider set of features, and is 4 times smaller than Jackson!

<img class="img-responsive" src="{{site.baseurl}}/images/jar-size.png" />


##Conclusion

When looking at all those benchmarks we can see that Genson and Jackson performances are relatively close. In most cases Jackson is faster
except for the deserialization benchmarks with Gson dataset, where Genson is always faster.
Performances are important, but all three libs provide great performances, at that level other things may be more important.

* What features are provided?
 *Genson comes with many nice features out of the box and they all just work.*

* Is it easy to use, configure and get running?
 *Again, Genson just works, most of the features are centralized through Genson and GensonBuilder classes, and recently more effort has been
 put into reducing the LOC you have to write.*

* Is it easy to download/install?
 *Sure, it is an all in one solution bunlded into a single jar. Easy to get even if you don't use Maven & cie.*

* How can you customize it if something does not exist in the library?
 *Genson provides many extension points, making it easy to plug new components in there.*

