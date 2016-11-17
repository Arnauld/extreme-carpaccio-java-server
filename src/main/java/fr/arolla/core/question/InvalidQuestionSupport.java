package fr.arolla.core.question;


import javax.validation.constraints.NotNull;

public abstract class InvalidQuestionSupport extends QuestionSupport {

    private static class NeedInvalidRequestResponse implements ResponseValidation {

        @Override
        public boolean accepted() {
            return false;
        }

        @Override
        public String diagnostic() {
            return "Invalid request: Should respond HTTP 400";
        }
    }

    @Override
    public boolean isInvalid() {
        return true;
    }

    @Override
    public ResponseValidation accepts(@NotNull Response response) {
        return new NeedInvalidRequestResponse();
    }

}
