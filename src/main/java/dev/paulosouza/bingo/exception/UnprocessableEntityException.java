package dev.paulosouza.bingo.exception;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

import java.net.URI;

public class UnprocessableEntityException extends AbstractThrowableProblem {

    static final URI TYPE = URI.create("https://dev.paulosouza/unprocessable-entity");

    public UnprocessableEntityException(String cause) {
        super(
                TYPE,
                "Unprocessable entity",
                Status.UNPROCESSABLE_ENTITY,
                cause
        );
    }

}
