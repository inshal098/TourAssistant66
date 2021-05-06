package com.tourassistant.coderoids.interfaces;

import com.google.firebase.firestore.DocumentSnapshot;

public interface onClickListner {
    void onClick(int pos , DocumentSnapshot documentSnapshot);
}
