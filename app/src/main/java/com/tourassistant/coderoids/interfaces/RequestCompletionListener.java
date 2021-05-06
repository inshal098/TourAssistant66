package com.tourassistant.coderoids.interfaces;

public interface RequestCompletionListener {
    void onListFilteredCompletion(boolean status);
    void onAllUsersCompletion(boolean status);
}
