# Configurator archetype pattern

## Problem statement

### Level 1

Have you ever bought a car? Well, that is not so common - but maybe you decided to purchase a shiny, new, high-end computer? If you are not an expert that process can induce a serious headache - wanna add backlit keyboard? Here you are, but it is possible only with thousand threaded processor. Maybe black color is something to land on? Surely, but it comes along with way more expensive matte screen and case! How they say - the more options on the table the harder decision to make. These tricks are there to fetch more money from clients. From merchant perspective to achieve this ultimate goal proper set of viable configurations has to be created.

### Level 2

However, conditional includance `(if you choose backlit keyboard then thousand thread processor must be taken)` is not the only possible relation. There is a need to ensure that only keyboard might be taken at a moment `(if you choose backlit keyboard, then usual keyboard and mechanical keyboards cannot be taken)`. Sometimes picking more demanding option requires technical compensation to not break your device, but there are `(if you choose super fast processor then one of several appropriate cooling systems must be taken)`, however still, you cannot have more than one cooling system at a time. Of course many of these rules are applicable in parallel, which means a lot of indirect dependencies and cascading consequences of decisions `(if black color then faster processor must be taken and if faster processor then one of additional cooling systems must be taken)`. What is more - rules tend to change pretty quickly - in ever-evolving, competition world merchants need to continuosly extend their offer with trendy colors and designs, quieter keyboards, better graphic cards and etc. 