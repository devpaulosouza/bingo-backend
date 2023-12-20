package dev.paulosouza.bingo.exception;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

import java.net.URI;

public class UnprocessableEntityException extends AbstractThrowableProblem {

    static final URI TYPE = URI.create("https://company.seurole.com.br/not-found");

    public UnprocessableEntityException(String cause) {
        super(
                TYPE,
                "Unprocessable entity",
                Status.UNPROCESSABLE_ENTITY,
                cause
        );
    }

}
