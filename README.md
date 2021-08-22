# debugging-reactor
In this video we look at how to debug Reactor Applications

https://www.youtube.com/watch?v=0oI_-xBhAK8

also, look up Reactor Debug Agent 
* Reactor Tools https://github.com/reactor/reactor-tools 
* Download Reactor Tools as explained in https://projectreactor.io/docs/core/release/reference/index.html#reactor-tools-debug

## summary
* use [Logging a sequence](https://projectreactor.io/docs/core/release/reference/index.html#_logging_a_sequence)
  * traces and logs events in an asynchronous sequence
* use [Checkpoints](https://projectreactor.io/docs/core/release/reference/index.html#checkpoint-alternative)
  * checkpoint message will appear in the log if the previous step in the stream had an error
  * all subsequent checkpoints after that will appear in the log, regardless if they have errors or not
* use [Reactor Debug Agent](https://projectreactor.io/docs/core/release/reference/index.html#reactor-tools-debug)
  * Reactor Debug Agent preprocesses the application's code before the application starts up
  * it gives a more useful and comprehensive stacktrace
    
