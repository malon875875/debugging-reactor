package com.example.debuggingreactor;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import reactor.core.publisher.Flux;

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
        Flux.just("A", "B", "C", "D", "F")
                .subscribe(
                        letter -> log.info("new letter" + letter)
                );
    }
}
