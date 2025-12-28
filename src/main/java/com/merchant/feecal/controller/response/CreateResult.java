package com.merchant.feecal.controller.response;

import lombok.Data;

/**
 * @author : kaerKing
 * @date : 2023/10/19
 */

@Data
public class CreateResult<T> {

    private boolean successful;
    private boolean newCreated;
    private T data;

    public static CreateResult fail() {
        CreateResult result = new CreateResult();
        result.setSuccessful(false);
        return result;
    }

    public static CreateResult success() {
        CreateResult result = new CreateResult();
        result.setSuccessful(true);
        return result;
    }

    public CreateResult<T> setData(T data) {
        this.data = data;
        return this;
    }

    public CreateResult<T> setNewCreated(boolean newCreated) {
        this.newCreated = newCreated;
        return this;
    }
}
