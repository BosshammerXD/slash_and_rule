package io.github.slash_and_rule.Interfaces;

import com.badlogic.gdx.utils.async.AsyncExecutor;
import com.badlogic.gdx.utils.async.AsyncResult;

public interface AsyncLoadable {
    AsyncResult<AsyncLoadable> schedule(AsyncExecutor asyncExecutor);

    void loadDone();
}
