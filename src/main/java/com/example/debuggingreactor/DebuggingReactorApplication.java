package com.example.debuggingreactor;

import lombok.extern.log4j.Log4j2;
import org.reactivestreams.Publisher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;
import java.util.function.Function;

@Log4j2
@SpringBootApplication
public class DebuggingReactorApplication {

    public static void main(String[] args) {
        // TODO@marcos continue https://youtu.be/0oI_-xBhAK8?t=2244

        SpringApplication.run(DebuggingReactorApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void go() throws Exception {

//        ChoicesEnum choice = ChoicesEnum.OPTION_HANDLE_ERROR_IN_SUBSCRIBER;
//        ChoicesEnum choice = ChoicesEnum.OPTION_HANDLE_ERROR_IN_PUBLISHER;
//        ChoicesEnum choice = ChoicesEnum.OPTION_HANDLE_ERROR_IN_PUBLISHER_CHECKPOINT;
          ChoicesEnum choice = ChoicesEnum.OPTION_HANDLE_ERROR_IN_PUBLISHER_HOOK;

        switch (choice) {
            case OPTION_HANDLE_ERROR_IN_SUBSCRIBER:
                doErrorHandlingOption1();
                break;
            case OPTION_HANDLE_ERROR_IN_PUBLISHER:
                doErrorHandlingOption2();
                break;
            case OPTION_HANDLE_ERROR_IN_PUBLISHER_CHECKPOINT:
                doErrorHandlingOption3();
                break;
            case OPTION_HANDLE_ERROR_IN_PUBLISHER_HOOK:
                /**
                 * hook gives a more useful and comprehensive assembly stacktrace -- cold stream
                 * in real app, add it as early as possible, e.g. Spring Boot main method
                 */
                Hooks.onOperatorDebug();
                doErrorHandlingOption3();
                break;
            default:
                throw new RuntimeException();
        }
    }

    private void doErrorHandlingOption1() {
        // Mono is a Stream of 0 to 1 records - e.g. A
        // Flux is a Stream of 0 to n records - e.g. A, B, C, ..., n
        // Consumer subscribes to Producer of the stream
//        Flux.just("A", "B", "C", "D", "F")
//                .subscribe(new Consumer<String>() {
//                     @Override
//                     public void accept(String letter) {
//                        log.info("new letter" + letter);
//                     }
//                 }
//        );

        // replace Consumer with lambda
//        Flux.just("A", "B", "C", "D", "F")
//                .subscribe(
//                        letter -> log.info("new letter" + letter)
//                );



        /**
         *
         * Option 1: have the error handling of the hot stream in the Subscriber
         *
         * */
        //add error -- keep all letters but F
        //      Flux<String> letters is a cold stream as no subscribers are there - nothing happens
        Flux<String> letters = Flux.just("A", "B", "C", "D", "F")
                .flatMap( letter -> {
                    if (letter.equals("F")) {
                        return Mono.error(new IllegalLetterException());
                    }
                    else {
                        return Mono.just(letter);
                    }
                })
                .log() //log out what the stream is doing once it is hot -- Subscriber(Consumer letter) subscribes to Producer of Stream, onNext(A), new letter A, onNext(B), ..... onError()
                ;

        // Flux<String> letters is a hot stream as subscribers push back
        // subscribe takes parameters:
        //          consumer – the consumer to invoke on each next signal  -- letter -> log.info("new letter" + letter)
        //          errorConsumer – the consumer to invoke on error signal -- this::doPrintError
        Consumer<String> consumer = letter -> log.info("new letter " + letter);
        letters.subscribe(
                //letter -> log.info("new letter" + letter)
                consumer,
//                throwable -> {
//                    log.error(throwable);
//                }
                this::doPrintError
        );
    }

    private void doErrorHandlingOption2() {
        /**
         *
         * Option 2: have the error handling of the hot stream in the Publisher
         *
         * */

        //add error -- keep all letters but F
        //      Flux<String> letters is a cold stream as no subscribers are there - nothing happens
        Flux<String> letters = Flux.just("AA", "BB", "CC", "DD", "F")
                .flatMap( letter -> {
                    if (letter.equals("F")) {
                        return Mono.error(new IllegalLetterException());
                    }
                    else {
                        return Mono.just(letter);
                    }
                })
                .log() //log out what the stream is doing once it is hot -- Subscriber(Consumer letter) subscribes to Producer of Stream, onNext(A), new letter A, onNext(B), ..... onError()
                .doOnError(this::doPrintError)
                ;

        // Flux<String> letters is a hot stream as subscribers push back
        // subscribe takes parameters:
        //          consumer – the consumer to invoke on each next signal  -- letter -> log.info("new letter" + letter)
        //          errorConsumer – the consumer to invoke on error signal -- this::doPrintError
        Consumer<String> consumer = letter -> log.info("new letter " + letter);
        letters.subscribe(consumer); // Subscriber does not have to know how to deal with errors, instead Producer handles errors in doOnError
    }

    private void doErrorHandlingOption3() {
        /**
         *
         * Option 3: have the error handling of the hot stream in the Publisher AND add my own messages in the stream
         *
         *      A) checkpoint message will appear in the log if the previous step in the stream had an error
         *      B) all subsequent checkpoints after that will appear in the log, regardless if they have errors or not
         *
         *      Because error happened at the step before MY CHECKPOINT C, this is printed out regardless if CHECKPOINT D, F, Z have errors or not
         *          Error has been observed at the following site(s):
         *     	        |_ checkpoint ? MY CHECKPOINT C: exception should have been forced at letter C
         *     	        |_ checkpoint ? MY CHECKPOINT D: exception should have been forced at letter D
         *           	|_ checkpoint ? MY CHECKPOINT F: exception should have been forced at letter F
         *              |_ checkpoint ? MY CHECKPOINT Z: NO exception should have been forced
         * */

        //add error -- keep all letters but F
        //      Flux<String> letters is a cold stream as no subscribers are there - nothing happens
        Flux<String> letters = Flux.just("A", "B", "C", "D", "F")
                .checkpoint("MY CHECKPOINT: letters should have been added to the stream", false)
                .flatMap(processMyLetters("C"))
                .checkpoint("MY CHECKPOINT C: exception should have been forced at letter C", false)
                .flatMap(processMyLetters("D"))
                .checkpoint("MY CHECKPOINT D: exception should have been forced at letter D", false)
                .flatMap(processMyLetters("F"))
                .checkpoint("MY CHECKPOINT F: exception should have been forced at letter F", false)
                .flatMap(processMyLetters("Z"))
                .checkpoint("MY CHECKPOINT Z: NO exception should have been forced", false)
                .log() //log out what the stream is doing once it is hot -- Subscriber(Consumer letter) subscribes to Producer of Stream, onNext(A), new letter A, onNext(B), ..... onError()
                .doOnError(this::doPrintError)
                ;

        // Flux<String> letters is a hot stream as subscribers push back
        // subscribe takes parameters:
        //          consumer – the consumer to invoke on each next signal  -- letter -> log.info("new letter" + letter)
        //          errorConsumer – the consumer to invoke on error signal -- this::doPrintError
        Consumer<String> consumer = letter -> log.info("new letter " + letter);
        letters.subscribe(consumer); // Subscriber does not have to know how to deal with errors, instead Producer handles errors in doOnError
    }

    private Function<String, Publisher<? extends String>> processMyLetters(String badLetter) {
        return letter -> {
            if (letter.equals(badLetter)) {
                return Mono.error(new IllegalLetterException(badLetter));
            }
            else {
                return Mono.just(letter);
            }
        };
    }

    private void doPrintError(Throwable t) {
        log.error("OH NOES!");
        log.error(t);
    }

    static class IllegalLetterException extends RuntimeException {
        public IllegalLetterException() {
            super("can't be an F! no F's!");
        }

        public IllegalLetterException(String letter) {
            super("can't be an " + letter + "! no " + letter + "'s!");
        }
    }
}
