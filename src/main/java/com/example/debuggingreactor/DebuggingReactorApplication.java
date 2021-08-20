package com.example.debuggingreactor;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

@Log4j2
@SpringBootApplication
public class DebuggingReactorApplication {

    public static void main(String[] args) {
        SpringApplication.run(DebuggingReactorApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void go() throws Exception {

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


        //add error -- keep all letters but F
        // Flux<String> letters is a cold stream as no subscribers are there - nothing happens
        Flux<String> letters = Flux.just("A", "B", "C", "D", "F")
                .flatMap( letter -> {
                    if (letter.equals("F")) {
                        return Mono.error(new IllegalLetterException());
                    }
                    else {
                        return Mono.just(letter);
                    }
                });

        // Flux<String> letters is a hot stream as subscribers push back
        Consumer<String> consumer = letter -> log.info("new letter" + letter);
        letters.subscribe(
                letter -> log.info("new letter" + letter)
        );
    }

    static class IllegalLetterException extends RuntimeException {
        public IllegalLetterException() {
            super("can't be an F! no F's!");
        }
    }
}
