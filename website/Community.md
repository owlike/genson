---
title: Community
layout: default
jumbotron: true
quick-overview: Get help, report bugs and contribute =)
---

##Mailing list##

Get help using Genson or contribute to the project on our mailing list.

 * [Genson user group](http://groups.google.com/group/genson) : genson@googlegroups.com


##Issue tracker##

If you think you have identified a bug in Genson, the best would be to create an issue and provide a minimal example allowing to reproduce it.
You can also report it on the mailing list.

If you would like to see some new feature in Genson, you have the choice between issue tracker and ML. If you have a quite good idea of what you want
then you can directly open an issue.


* The new [GitHub issue tracker](https://github.com/owlike/genson/issues)
* The [old one on googlecode](http://code.google.com/p/genson/issues/list)

##Contributing##

Of course contributions are very welcome, there is plenty of work to be done :)

If you want to get started
 * <a href="https://www.clahub.com/agreements/owlike/genson">sign the Contributor License Agreement</a>
 * drop an email on the mailing list or to cepoi.eugen@gmail.com to discuss what you want to work on


##Contributors##

{% capture includeGuts %}
{% include contributors.html %} 
{% endcapture %}
{{ includeGuts | replace: '    ', ''}}
